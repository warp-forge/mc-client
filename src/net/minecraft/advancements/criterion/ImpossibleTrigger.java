package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public class ImpossibleTrigger implements CriterionTrigger {
   public void addPlayerListener(final PlayerAdvancements player, final CriterionTrigger.Listener listener) {
   }

   public void removePlayerListener(final PlayerAdvancements player, final CriterionTrigger.Listener listener) {
   }

   public void removePlayerListeners(final PlayerAdvancements player) {
   }

   public Codec codec() {
      return ImpossibleTrigger.TriggerInstance.CODEC;
   }

   public static record TriggerInstance() implements CriterionTriggerInstance {
      public static final Codec CODEC = MapCodec.unitCodec(new TriggerInstance());

      public void validate(final ValidationContextSource validator) {
      }
   }
}
