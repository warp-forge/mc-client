package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger {
   void addPlayerListener(final PlayerAdvancements player, final Listener listener);

   void removePlayerListener(final PlayerAdvancements player, final Listener listener);

   void removePlayerListeners(final PlayerAdvancements player);

   Codec codec();

   default Criterion createCriterion(final CriterionTriggerInstance instance) {
      return new Criterion(this, instance);
   }

   public static record Listener(CriterionTriggerInstance trigger, AdvancementHolder advancement, String criterion) {
      public void run(final PlayerAdvancements player) {
         player.award(this.advancement, this.criterion);
      }
   }
}
