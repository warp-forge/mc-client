package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger {
   public Codec codec() {
      return UsedTotemTrigger.TriggerInstance.CODEC;
   }

   public void trigger(final ServerPlayer player, final ItemStack itemStack) {
      this.trigger(player, (t) -> t.matches(itemStack));
   }

   public static record TriggerInstance(Optional player, Optional item) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)).apply(i, TriggerInstance::new));

      public static Criterion usedTotem(final ItemPredicate item) {
         return CriteriaTriggers.USED_TOTEM.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(item)));
      }

      public static Criterion usedTotem(final HolderGetter items, final ItemLike itemlike) {
         return CriteriaTriggers.USED_TOTEM.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(items, itemlike).build())));
      }

      public boolean matches(final ItemStack itemStack) {
         return this.item.isEmpty() || ((ItemPredicate)this.item.get()).test((ItemInstance)itemStack);
      }
   }
}
