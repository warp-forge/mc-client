package net.minecraft.world.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryMap;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Brain {
   private static final int SCHEDULE_UPDATE_DELAY = 20;
   private final Map memories = Maps.newHashMap();
   private final Map sensors = Maps.newLinkedHashMap();
   private final Map availableBehaviorsByPriority = Maps.newTreeMap();
   private @Nullable EnvironmentAttribute schedule;
   private final Map activityRequirements = Maps.newHashMap();
   private final Map activityMemoriesToEraseWhenStopped = Maps.newHashMap();
   private Set coreActivities = Sets.newHashSet();
   private final Set activeActivities = Sets.newHashSet();
   private Activity defaultActivity;
   private long lastScheduleUpdate;

   public static Provider provider(final Collection sensorTypes) {
      return new Provider(ImmutableList.of(), sensorTypes, (var0) -> List.of());
   }

   public static Provider provider(final Collection sensorTypes, final ActivitySupplier activities) {
      return new Provider(ImmutableList.of(), sensorTypes, activities);
   }

   /** @deprecated */
   @Deprecated
   public static Provider provider(final Collection memoryTypes, final Collection sensorTypes, final ActivitySupplier activities) {
      return new Provider(memoryTypes, sensorTypes, activities);
   }

   @VisibleForTesting
   protected Brain(final Collection memoryTypes, final Collection sensorTypes, final List activities, final MemoryMap memories) {
      this.defaultActivity = Activity.IDLE;
      this.lastScheduleUpdate = -9999L;

      for(MemoryModuleType memoryType : memoryTypes) {
         this.memories.put(memoryType, Optional.empty());
      }

      for(SensorType sensorType : sensorTypes) {
         this.sensors.put(sensorType, sensorType.create());
      }

      for(Sensor sensor : this.sensors.values()) {
         for(MemoryModuleType type : sensor.requires()) {
            this.memories.put(type, Optional.empty());
         }
      }

      for(ActivityData activity : activities) {
         this.addActivity(activity.activityType(), activity.behaviorPriorityPairs(), activity.conditions(), activity.memoriesToEraseWhenStopped());
      }

      for(MemoryMap.Value memory : memories) {
         this.setMemoryInternal(memory);
      }

      this.setCoreActivities(ImmutableSet.of(Activity.CORE));
      this.useDefaultActivity();
   }

   public Brain() {
      this.defaultActivity = Activity.IDLE;
      this.lastScheduleUpdate = -9999L;
      this.setCoreActivities(ImmutableSet.of(Activity.CORE));
      this.useDefaultActivity();
   }

   public Packed pack() {
      return new Packed(MemoryMap.of(this.memories.entrySet().stream().filter((entry) -> ((MemoryModuleType)entry.getKey()).getCodec().isPresent()).flatMap((entry) -> ((Optional)entry.getValue()).map((value) -> MemoryMap.Value.createUnchecked((MemoryModuleType)entry.getKey(), value)).stream())));
   }

   public boolean hasMemoryValue(final MemoryModuleType type) {
      return this.checkMemory(type, MemoryStatus.VALUE_PRESENT);
   }

   public void clearMemories() {
      this.memories.keySet().forEach((key) -> this.memories.put(key, Optional.empty()));
   }

   public void eraseMemory(final MemoryModuleType type) {
      this.setMemory(type, Optional.empty());
   }

   public void setMemory(final MemoryModuleType type, final @Nullable Object value) {
      this.setMemory(type, Optional.ofNullable(value));
   }

   public void setMemoryWithExpiry(final MemoryModuleType type, final Object value, final long timeToLive) {
      this.setMemoryInternal(type, Optional.of(ExpirableValue.of(value, timeToLive)));
   }

   public void setMemory(final MemoryModuleType type, final Optional optionalValue) {
      this.setMemoryInternal(type, optionalValue.map(ExpirableValue::of));
   }

   private void setMemoryInternal(final MemoryModuleType type, final Optional optionalExpirableValue) {
      if (this.memories.containsKey(type)) {
         if (optionalExpirableValue.isPresent() && this.isEmptyCollection(((ExpirableValue)optionalExpirableValue.get()).getValue())) {
            this.eraseMemory(type);
         } else {
            this.memories.put(type, optionalExpirableValue);
         }
      }

   }

   private void setMemoryInternal(final MemoryMap.Value value) {
      this.setMemoryInternal(value.type(), Optional.of(value.value()));
   }

   public Optional getMemory(final MemoryModuleType type) {
      Optional<? extends ExpirableValue<?>> expirableValue = (Optional)this.memories.get(type);
      if (expirableValue == null) {
         throw new IllegalStateException("Unregistered memory fetched: " + String.valueOf(type));
      } else {
         return expirableValue.map(ExpirableValue::getValue);
      }
   }

   public @Nullable Optional getMemoryInternal(final MemoryModuleType type) {
      Optional<? extends ExpirableValue<?>> expirableValue = (Optional)this.memories.get(type);
      return expirableValue == null ? null : expirableValue.map(ExpirableValue::getValue);
   }

   public long getTimeUntilExpiry(final MemoryModuleType type) {
      Optional<? extends ExpirableValue<?>> memory = (Optional)this.memories.get(type);
      return (Long)memory.map(ExpirableValue::getTimeToLive).orElse(0L);
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public Map getMemories() {
      return this.memories;
   }

   public boolean isMemoryValue(final MemoryModuleType memoryType, final Object value) {
      return !this.hasMemoryValue(memoryType) ? false : this.getMemory(memoryType).filter((memory) -> memory.equals(value)).isPresent();
   }

   public boolean checkMemory(final MemoryModuleType type, final MemoryStatus status) {
      Optional<? extends ExpirableValue<?>> optionalExpirableValue = (Optional)this.memories.get(type);
      if (optionalExpirableValue == null) {
         return false;
      } else {
         return status == MemoryStatus.REGISTERED || status == MemoryStatus.VALUE_PRESENT && optionalExpirableValue.isPresent() || status == MemoryStatus.VALUE_ABSENT && optionalExpirableValue.isEmpty();
      }
   }

   public void setSchedule(final EnvironmentAttribute schedule) {
      this.schedule = schedule;
   }

   public void setCoreActivities(final Set activities) {
      this.coreActivities = activities;
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public Set getActiveActivities() {
      return this.activeActivities;
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public List getRunningBehaviors() {
      List<BehaviorControl<? super E>> runningBehaviours = new ObjectArrayList();

      for(Map behavioursByActivities : this.availableBehaviorsByPriority.values()) {
         for(Set behaviors : behavioursByActivities.values()) {
            for(BehaviorControl behavior : behaviors) {
               if (behavior.getStatus() == Behavior.Status.RUNNING) {
                  runningBehaviours.add(behavior);
               }
            }
         }
      }

      return runningBehaviours;
   }

   public void useDefaultActivity() {
      this.setActiveActivity(this.defaultActivity);
   }

   public Optional getActiveNonCoreActivity() {
      for(Activity activity : this.activeActivities) {
         if (!this.coreActivities.contains(activity)) {
            return Optional.of(activity);
         }
      }

      return Optional.empty();
   }

   public void setActiveActivityIfPossible(final Activity activity) {
      if (this.activityRequirementsAreMet(activity)) {
         this.setActiveActivity(activity);
      } else {
         this.useDefaultActivity();
      }

   }

   private void setActiveActivity(final Activity activity) {
      if (!this.isActive(activity)) {
         this.eraseMemoriesForOtherActivitesThan(activity);
         this.activeActivities.clear();
         this.activeActivities.addAll(this.coreActivities);
         this.activeActivities.add(activity);
      }
   }

   private void eraseMemoriesForOtherActivitesThan(final Activity activity) {
      for(Activity oldActivity : this.activeActivities) {
         if (oldActivity != activity) {
            Set<MemoryModuleType<?>> memoryModuleTypes = (Set)this.activityMemoriesToEraseWhenStopped.get(oldActivity);
            if (memoryModuleTypes != null) {
               for(MemoryModuleType memoryModuleType : memoryModuleTypes) {
                  this.eraseMemory(memoryModuleType);
               }
            }
         }
      }

   }

   public void updateActivityFromSchedule(final EnvironmentAttributeSystem environmentAttributes, final long gameTime, final Vec3 pos) {
      if (gameTime - this.lastScheduleUpdate > 20L) {
         this.lastScheduleUpdate = gameTime;
         Activity scheduledActivity = this.schedule != null ? (Activity)environmentAttributes.getValue(this.schedule, pos) : Activity.IDLE;
         if (!this.activeActivities.contains(scheduledActivity)) {
            this.setActiveActivityIfPossible(scheduledActivity);
         }
      }

   }

   public void setActiveActivityToFirstValid(final List activities) {
      for(Activity activity : activities) {
         if (this.activityRequirementsAreMet(activity)) {
            this.setActiveActivity(activity);
            break;
         }
      }

   }

   public void setDefaultActivity(final Activity activity) {
      this.defaultActivity = activity;
   }

   public void addActivity(final Activity activity, final ImmutableList behaviorPriorityPairs, final Set conditions, final Set memoriesToEraseWhenStopped) {
      this.activityRequirements.put(activity, conditions);
      if (!memoriesToEraseWhenStopped.isEmpty()) {
         this.activityMemoriesToEraseWhenStopped.put(activity, memoriesToEraseWhenStopped);
      }

      UnmodifiableIterator var5 = behaviorPriorityPairs.iterator();

      while(var5.hasNext()) {
         Pair<Integer, ? extends BehaviorControl<? super E>> pair = (Pair)var5.next();
         BehaviorControl<? super E> behavior = (BehaviorControl)pair.getSecond();

         for(MemoryModuleType requiredMemory : behavior.getRequiredMemories()) {
            if (!this.memories.containsKey(requiredMemory)) {
               this.memories.put(requiredMemory, Optional.empty());
            }
         }

         ((Set)((Map)this.availableBehaviorsByPriority.computeIfAbsent((Integer)pair.getFirst(), (key) -> Maps.newHashMap())).computeIfAbsent(activity, (key) -> Sets.newLinkedHashSet())).add(behavior);
      }

   }

   @VisibleForTesting
   public void removeAllBehaviors() {
      this.availableBehaviorsByPriority.clear();
   }

   public boolean isActive(final Activity activity) {
      return this.activeActivities.contains(activity);
   }

   public void tick(final ServerLevel level, final LivingEntity body) {
      this.forgetOutdatedMemories();
      this.tickSensors(level, body);
      this.startEachNonRunningBehavior(level, body);
      this.tickEachRunningBehavior(level, body);
   }

   private void tickSensors(final ServerLevel level, final LivingEntity body) {
      for(Sensor sensor : this.sensors.values()) {
         sensor.tick(level, body);
      }

   }

   private void forgetOutdatedMemories() {
      for(Map.Entry entry : this.memories.entrySet()) {
         if (((Optional)entry.getValue()).isPresent()) {
            ExpirableValue<?> memory = (ExpirableValue)((Optional)entry.getValue()).get();
            if (memory.hasExpired()) {
               this.eraseMemory((MemoryModuleType)entry.getKey());
            }

            memory.tick();
         }
      }

   }

   public void stopAll(final ServerLevel level, final LivingEntity body) {
      long timestamp = body.level().getGameTime();

      for(BehaviorControl behavior : this.getRunningBehaviors()) {
         behavior.doStop(level, body, timestamp);
      }

   }

   private void startEachNonRunningBehavior(final ServerLevel level, final LivingEntity body) {
      long time = level.getGameTime();

      for(Map behavioursByActivities : this.availableBehaviorsByPriority.values()) {
         for(Map.Entry behavioursForActivity : behavioursByActivities.entrySet()) {
            Activity activity = (Activity)behavioursForActivity.getKey();
            if (this.activeActivities.contains(activity)) {
               for(BehaviorControl behavior : (Set)behavioursForActivity.getValue()) {
                  if (behavior.getStatus() == Behavior.Status.STOPPED) {
                     behavior.tryStart(level, body, time);
                  }
               }
            }
         }
      }

   }

   private void tickEachRunningBehavior(final ServerLevel level, final LivingEntity body) {
      long timestamp = level.getGameTime();

      for(BehaviorControl behavior : this.getRunningBehaviors()) {
         behavior.tickOrStop(level, body, timestamp);
      }

   }

   private boolean activityRequirementsAreMet(final Activity activity) {
      if (!this.activityRequirements.containsKey(activity)) {
         return false;
      } else {
         for(Pair memoryRequirement : (Set)this.activityRequirements.get(activity)) {
            MemoryModuleType<?> memoryType = (MemoryModuleType)memoryRequirement.getFirst();
            MemoryStatus memoryStatus = (MemoryStatus)memoryRequirement.getSecond();
            if (!this.checkMemory(memoryType, memoryStatus)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean isEmptyCollection(final Object object) {
      return object instanceof Collection && ((Collection)object).isEmpty();
   }

   public boolean isBrainDead() {
      return this.memories.isEmpty() && this.sensors.isEmpty() && this.availableBehaviorsByPriority.isEmpty();
   }

   public static final class Provider {
      private final Collection memoryTypes;
      private final Collection sensorTypes;
      private final ActivitySupplier activities;

      private Provider(final Collection memoryTypes, final Collection sensorTypes, final ActivitySupplier activities) {
         this.memoryTypes = memoryTypes;
         this.sensorTypes = sensorTypes;
         this.activities = activities;
      }

      public Brain makeBrain(final LivingEntity body, final Packed packed) {
         List<ActivityData<E>> activities = this.activities.createActivities(body);
         return new Brain(this.memoryTypes, this.sensorTypes, activities, packed.memories);
      }
   }

   public static record Packed(MemoryMap memories) {
      public static final Packed EMPTY;
      public static final Codec CODEC;

      static {
         EMPTY = new Packed(MemoryMap.EMPTY);
         CODEC = RecordCodecBuilder.create((i) -> i.group(MemoryMap.CODEC.fieldOf("memories").forGetter(Packed::memories)).apply(i, Packed::new));
      }
   }

   @FunctionalInterface
   public interface ActivitySupplier {
      List createActivities(LivingEntity body);
   }
}
