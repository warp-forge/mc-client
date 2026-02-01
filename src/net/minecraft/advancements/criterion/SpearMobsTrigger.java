package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class SpearMobsTrigger extends SimpleCriterionTrigger {
   public Codec codec() {
      return SpearMobsTrigger.TriggerInstance.CODEC;
   }

   public void trigger(final ServerPlayer player, final int number) {
      this.trigger(player, (t) -> t.matches(number));
   }

   public static record TriggerInstance(Optional player, Optional count) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(TriggerInstance::count)).apply(i, TriggerInstance::new));

      public static Criterion spearMobs(final int requiredCount) {
         return CriteriaTriggers.SPEAR_MOBS_TRIGGER.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(requiredCount)));
      }

      public boolean matches(final int requiredCount) {
         return this.count.isEmpty() || requiredCount >= (Integer)this.count.get();
      }
   }
}
