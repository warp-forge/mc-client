package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public class TradeTrigger extends SimpleCriterionTrigger {
   public Codec codec() {
      return TradeTrigger.TriggerInstance.CODEC;
   }

   public void trigger(final ServerPlayer player, final AbstractVillager villager, final ItemStack itemStack) {
      LootContext villagerContext = EntityPredicate.createContext(player, villager);
      this.trigger(player, (t) -> t.matches(villagerContext, itemStack));
   }

   public static record TriggerInstance(Optional player, Optional villager, Optional item) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(TriggerInstance::villager), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)).apply(i, TriggerInstance::new));

      public static Criterion tradedWithVillager() {
         return CriteriaTriggers.TRADE.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static Criterion tradedWithVillager(final EntityPredicate.Builder player) {
         return CriteriaTriggers.TRADE.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player)), Optional.empty(), Optional.empty()));
      }

      public boolean matches(final LootContext villager, final ItemStack itemStack) {
         if (this.villager.isPresent() && !((ContextAwarePredicate)this.villager.get()).matches(villager)) {
            return false;
         } else {
            return !this.item.isPresent() || ((ItemPredicate)this.item.get()).test((ItemInstance)itemStack);
         }
      }

      public void validate(final ValidationContextSource validator) {
         SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
         Validatable.validate(validator.entityContext(), "villager", this.villager);
      }
   }
}
