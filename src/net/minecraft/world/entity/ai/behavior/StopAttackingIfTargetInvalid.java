package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAttackingIfTargetInvalid {
   private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

   public static BehaviorControl create(final TargetErasedCallback onTargetErased) {
      return create((level, entity) -> false, onTargetErased, true);
   }

   public static BehaviorControl create(final StopAttackCondition stopAttackingWhen) {
      return create(stopAttackingWhen, (level, body, target) -> {
      }, true);
   }

   public static BehaviorControl create() {
      return create((level, entity) -> false, (level, body, target) -> {
      }, true);
   }

   public static BehaviorControl create(final StopAttackCondition stopAttackingWhen, final TargetErasedCallback onTargetErased, final boolean canGrowTiredOfTryingToReachTarget) {
      return BehaviorBuilder.create((Function)((i) -> i.group(i.present(MemoryModuleType.ATTACK_TARGET), i.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(i, (attackTarget, cantReachSince) -> (level, body, timestamp) -> {
               LivingEntity target = (LivingEntity)i.get(attackTarget);
               if (body.canAttack(target) && (!canGrowTiredOfTryingToReachTarget || !isTiredOfTryingToReachTarget(body, i.tryGet(cantReachSince))) && target.isAlive() && target.level() == body.level() && !stopAttackingWhen.test(level, target)) {
                  return true;
               } else {
                  onTargetErased.accept(level, body, target);
                  attackTarget.erase();
                  return true;
               }
            })));
   }

   private static boolean isTiredOfTryingToReachTarget(final LivingEntity body, final Optional cantReachSince) {
      return cantReachSince.isPresent() && body.level().getGameTime() - (Long)cantReachSince.get() > 200L;
   }

   @FunctionalInterface
   public interface StopAttackCondition {
      boolean test(ServerLevel level, LivingEntity target);
   }

   @FunctionalInterface
   public interface TargetErasedCallback {
      void accept(ServerLevel level, Object body, LivingEntity target);
   }
}
