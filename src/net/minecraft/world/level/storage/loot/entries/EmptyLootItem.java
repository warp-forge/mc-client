package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
   public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> singletonFields(i).apply(i, EmptyLootItem::new));

   private EmptyLootItem(final int weight, final int quality, final List conditions, final List functions) {
      super(weight, quality, conditions, functions);
   }

   public MapCodec codec() {
      return MAP_CODEC;
   }

   public void createItemStack(final Consumer output, final LootContext context) {
   }

   public static LootPoolSingletonContainer.Builder emptyItem() {
      return simpleBuilder(EmptyLootItem::new);
   }
}
