package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jspecify.annotations.Nullable;

public class BehaviorBuilder implements App {
   private final TriggerWithResult trigger;

   public static BehaviorBuilder unbox(final App box) {
      return (BehaviorBuilder)box;
   }

   public static Instance instance() {
      return new Instance();
   }

   public static OneShot create(final Function builder) {
      final TriggerWithResult<E, Trigger<E>> resolvedBuilder = get((App)builder.apply(instance()));
      return new OneShot() {
         public boolean trigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
            Trigger<E> trigger = (Trigger)resolvedBuilder.tryTrigger(level, body, timestamp);
            return trigger == null ? false : trigger.trigger(level, body, timestamp);
         }

         public Set getRequiredMemories() {
            return resolvedBuilder.memories();
         }

         public String debugString() {
            return "OneShot[" + resolvedBuilder.debugString() + "]";
         }

         public String toString() {
            return this.debugString();
         }
      };
   }

   public static OneShot sequence(final Trigger first, final OneShot second) {
      final OneShot<E> wrapped = create((Function)((i) -> i.group(i.ifTriggered(first)).apply(i, (var1) -> {
            Objects.requireNonNull(second);
            return second::trigger;
         })));
      return new OneShot() {
         public boolean trigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
            return wrapped.trigger(level, body, timestamp);
         }

         public Set getRequiredMemories() {
            Set<MemoryModuleType<?>> memories = new HashSet();
            memories.addAll(wrapped.getRequiredMemories());
            memories.addAll(second.getRequiredMemories());
            return memories;
         }

         public String debugString() {
            return "OneShot[stuff]";
         }

         public String toString() {
            return this.debugString();
         }
      };
   }

   public static OneShot triggerIf(final Predicate predicate, final OneShot behavior) {
      return sequence(triggerIf(predicate), behavior);
   }

   public static OneShot triggerIf(final Predicate predicate) {
      return create((Function)((i) -> i.point((Trigger)(level, body, timestamp) -> predicate.test(body))));
   }

   public static OneShot triggerIf(final BiPredicate predicate) {
      return create((Function)((i) -> i.point((Trigger)(level, body, timestamp) -> predicate.test(level, body))));
   }

   private static TriggerWithResult get(final App box) {
      return unbox(box).trigger;
   }

   private BehaviorBuilder(final TriggerWithResult trigger) {
      this.trigger = trigger;
   }

   private static BehaviorBuilder create(final TriggerWithResult instanceFactory) {
      return new BehaviorBuilder(instanceFactory);
   }

   public static final class Mu implements K1 {
   }

   private static final class PureMemory extends BehaviorBuilder {
      private PureMemory(final MemoryCondition condition) {
         super(new TriggerWithResult() {
            public @Nullable MemoryAccessor tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
               Brain<?> brain = body.getBrain();
               Optional<Value> value = brain.getMemoryInternal(condition.memory());
               return value == null ? null : condition.createAccessor(brain, value);
            }

            public Set memories() {
               return Set.of(condition.memory());
            }

            public String debugString() {
               return "M[" + String.valueOf(condition) + "]";
            }

            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   private static final class Constant extends BehaviorBuilder {
      private Constant(final Object a) {
         this(a, () -> "C[" + String.valueOf(a) + "]");
      }

      private Constant(final Object a, final Supplier debugString) {
         super(new TriggerWithResult() {
            public Object tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
               return a;
            }

            public Set memories() {
               return Set.of();
            }

            public String debugString() {
               return (String)debugString.get();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   private static final class TriggerWrapper extends BehaviorBuilder {
      private TriggerWrapper(final Trigger dependentTrigger) {
         super(new TriggerWithResult() {
            public @Nullable Unit tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
               return dependentTrigger.trigger(level, body, timestamp) ? Unit.INSTANCE : null;
            }

            public Set memories() {
               return Set.of();
            }

            public String debugString() {
               return "T[" + String.valueOf(dependentTrigger) + "]";
            }
         });
      }
   }

   public static final class Instance implements Applicative {
      public Optional tryGet(final MemoryAccessor box) {
         return OptionalBox.unbox(box.value());
      }

      public Object get(final MemoryAccessor box) {
         return IdF.get(box.value());
      }

      public BehaviorBuilder registered(final MemoryModuleType memory) {
         return new PureMemory(new MemoryCondition.Registered(memory));
      }

      public BehaviorBuilder present(final MemoryModuleType memory) {
         return new PureMemory(new MemoryCondition.Present(memory));
      }

      public BehaviorBuilder absent(final MemoryModuleType memory) {
         return new PureMemory(new MemoryCondition.Absent(memory));
      }

      public BehaviorBuilder ifTriggered(final Trigger dependentTrigger) {
         return new TriggerWrapper(dependentTrigger);
      }

      public BehaviorBuilder point(final Object a) {
         return new Constant(a);
      }

      public BehaviorBuilder point(final Supplier debugString, final Object a) {
         return new Constant(a, debugString);
      }

      public Function lift1(final App function) {
         return (a) -> {
            final TriggerWithResult<E, A> aTrigger = BehaviorBuilder.get(a);
            final TriggerWithResult<E, Function<A, R>> fTrigger = BehaviorBuilder.get(function);
            return BehaviorBuilder.create(new TriggerWithResult() {
               {
                  Objects.requireNonNull(Instance.this);
               }

               public Object tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
                  A ra = (A)aTrigger.tryTrigger(level, body, timestamp);
                  if (ra == null) {
                     return null;
                  } else {
                     Function<A, R> rf = (Function)fTrigger.tryTrigger(level, body, timestamp);
                     return rf == null ? null : rf.apply(ra);
                  }
               }

               public Set memories() {
                  Set<MemoryModuleType<?>> memories = new HashSet();
                  memories.addAll(aTrigger.memories());
                  memories.addAll(fTrigger.memories());
                  return memories;
               }

               public String debugString() {
                  String var10000 = fTrigger.debugString();
                  return var10000 + " * " + aTrigger.debugString();
               }

               public String toString() {
                  return this.debugString();
               }
            });
         };
      }

      public BehaviorBuilder map(final Function func, final App ts) {
         final TriggerWithResult<E, T> tTrigger = BehaviorBuilder.get(ts);
         return BehaviorBuilder.create(new TriggerWithResult() {
            {
               Objects.requireNonNull(Instance.this);
            }

            public Object tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
               T t = (T)tTrigger.tryTrigger(level, body, timestamp);
               return t == null ? null : func.apply(t);
            }

            public Set memories() {
               return tTrigger.memories();
            }

            public String debugString() {
               String var10000 = tTrigger.debugString();
               return var10000 + ".map[" + String.valueOf(func) + "]";
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public BehaviorBuilder ap2(final App func, final App a, final App b) {
         final TriggerWithResult<E, A> aTrigger = BehaviorBuilder.get(a);
         final TriggerWithResult<E, B> bTrigger = BehaviorBuilder.get(b);
         final TriggerWithResult<E, BiFunction<A, B, R>> fTrigger = BehaviorBuilder.get(func);
         return BehaviorBuilder.create(new TriggerWithResult() {
            {
               Objects.requireNonNull(Instance.this);
            }

            public Object tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
               A ra = (A)aTrigger.tryTrigger(level, body, timestamp);
               if (ra == null) {
                  return null;
               } else {
                  B rb = (B)bTrigger.tryTrigger(level, body, timestamp);
                  if (rb == null) {
                     return null;
                  } else {
                     BiFunction<A, B, R> fr = (BiFunction)fTrigger.tryTrigger(level, body, timestamp);
                     return fr == null ? null : fr.apply(ra, rb);
                  }
               }
            }

            public Set memories() {
               Set<MemoryModuleType<?>> memories = new HashSet();
               memories.addAll(aTrigger.memories());
               memories.addAll(bTrigger.memories());
               memories.addAll(fTrigger.memories());
               return memories;
            }

            public String debugString() {
               String var10000 = fTrigger.debugString();
               return var10000 + " * " + aTrigger.debugString() + " * " + bTrigger.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public BehaviorBuilder ap3(final App func, final App t1, final App t2, final App t3) {
         final TriggerWithResult<E, T1> t1Trigger = BehaviorBuilder.get(t1);
         final TriggerWithResult<E, T2> t2Trigger = BehaviorBuilder.get(t2);
         final TriggerWithResult<E, T3> t3Trigger = BehaviorBuilder.get(t3);
         final TriggerWithResult<E, Function3<T1, T2, T3, R>> fTrigger = BehaviorBuilder.get(func);
         return BehaviorBuilder.create(new TriggerWithResult() {
            {
               Objects.requireNonNull(Instance.this);
            }

            public Object tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
               T1 r1 = (T1)t1Trigger.tryTrigger(level, body, timestamp);
               if (r1 == null) {
                  return null;
               } else {
                  T2 r2 = (T2)t2Trigger.tryTrigger(level, body, timestamp);
                  if (r2 == null) {
                     return null;
                  } else {
                     T3 r3 = (T3)t3Trigger.tryTrigger(level, body, timestamp);
                     if (r3 == null) {
                        return null;
                     } else {
                        Function3<T1, T2, T3, R> rf = (Function3)fTrigger.tryTrigger(level, body, timestamp);
                        return rf == null ? null : rf.apply(r1, r2, r3);
                     }
                  }
               }
            }

            public Set memories() {
               Set<MemoryModuleType<?>> memories = new HashSet();
               memories.addAll(t1Trigger.memories());
               memories.addAll(t2Trigger.memories());
               memories.addAll(t3Trigger.memories());
               memories.addAll(fTrigger.memories());
               return memories;
            }

            public String debugString() {
               String var10000 = fTrigger.debugString();
               return var10000 + " * " + t1Trigger.debugString() + " * " + t2Trigger.debugString() + " * " + t3Trigger.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public BehaviorBuilder ap4(final App func, final App t1, final App t2, final App t3, final App t4) {
         final TriggerWithResult<E, T1> t1Trigger = BehaviorBuilder.get(t1);
         final TriggerWithResult<E, T2> t2Trigger = BehaviorBuilder.get(t2);
         final TriggerWithResult<E, T3> t3Trigger = BehaviorBuilder.get(t3);
         final TriggerWithResult<E, T4> t4Trigger = BehaviorBuilder.get(t4);
         final TriggerWithResult<E, Function4<T1, T2, T3, T4, R>> fTrigger = BehaviorBuilder.get(func);
         return BehaviorBuilder.create(new TriggerWithResult() {
            {
               Objects.requireNonNull(Instance.this);
            }

            public Object tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp) {
               T1 r1 = (T1)t1Trigger.tryTrigger(level, body, timestamp);
               if (r1 == null) {
                  return null;
               } else {
                  T2 r2 = (T2)t2Trigger.tryTrigger(level, body, timestamp);
                  if (r2 == null) {
                     return null;
                  } else {
                     T3 r3 = (T3)t3Trigger.tryTrigger(level, body, timestamp);
                     if (r3 == null) {
                        return null;
                     } else {
                        T4 r4 = (T4)t4Trigger.tryTrigger(level, body, timestamp);
                        if (r4 == null) {
                           return null;
                        } else {
                           Function4<T1, T2, T3, T4, R> rf = (Function4)fTrigger.tryTrigger(level, body, timestamp);
                           return rf == null ? null : rf.apply(r1, r2, r3, r4);
                        }
                     }
                  }
               }
            }

            public Set memories() {
               Set<MemoryModuleType<?>> memories = new HashSet();
               memories.addAll(t1Trigger.memories());
               memories.addAll(t2Trigger.memories());
               memories.addAll(t3Trigger.memories());
               memories.addAll(t4Trigger.memories());
               memories.addAll(fTrigger.memories());
               return memories;
            }

            public String debugString() {
               String var10000 = fTrigger.debugString();
               return var10000 + " * " + t1Trigger.debugString() + " * " + t2Trigger.debugString() + " * " + t3Trigger.debugString() + " * " + t4Trigger.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      private static final class Mu implements Applicative.Mu {
      }
   }

   private interface TriggerWithResult {
      @Nullable Object tryTrigger(final ServerLevel level, final LivingEntity body, final long timestamp);

      Set memories();

      String debugString();
   }
}
