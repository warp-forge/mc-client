package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger {
   public Codec codec() {
      return BrewedPotionTrigger.TriggerInstance.CODEC;
   }

   public void trigger(final ServerPlayer player, final Holder potion) {
      this.trigger(player, (t) -> t.matches(potion));
   }

   public static record TriggerInstance(Optional player, Optional potion) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), Potion.CODEC.optionalFieldOf("potion").forGetter(TriggerInstance::potion)).apply(i, TriggerInstance::new));

      public static Criterion brewedPotion() {
         return CriteriaTriggers.BREWED_POTION.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public boolean matches(final Holder potion) {
         return !this.potion.isPresent() || ((Holder)this.potion.get()).equals(potion);
      }
   }
}
