package net.minecraft.world.level.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import java.util.Collections;
import java.util.SequencedSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class FuelValues {
   private final Object2IntSortedMap values;

   private FuelValues(final Object2IntSortedMap values) {
      this.values = values;
   }

   public boolean isFuel(final ItemStack itemStack) {
      return this.values.containsKey(itemStack.getItem());
   }

   public SequencedSet fuelItems() {
      return Collections.unmodifiableSequencedSet(this.values.keySet());
   }

   public int burnDuration(final ItemStack itemStack) {
      return itemStack.isEmpty() ? 0 : this.values.getInt(itemStack.getItem());
   }

   public static FuelValues vanillaBurnTimes(final HolderLookup.Provider registries, final FeatureFlagSet enabledFeatures) {
      return vanillaBurnTimes(registries, enabledFeatures, 200);
   }

   public static FuelValues vanillaBurnTimes(final HolderLookup.Provider registries, final FeatureFlagSet enabledFeatures, final int baseUnit) {
      return (new Builder(registries, enabledFeatures)).add((ItemLike)Items.LAVA_BUCKET, baseUnit * 100).add((ItemLike)Blocks.COAL_BLOCK, baseUnit * 8 * 10).add((ItemLike)Items.BLAZE_ROD, baseUnit * 12).add((ItemLike)Items.COAL, baseUnit * 8).add((ItemLike)Items.CHARCOAL, baseUnit * 8).add(ItemTags.LOGS, baseUnit * 3 / 2).add(ItemTags.BAMBOO_BLOCKS, baseUnit * 3 / 2).add(ItemTags.PLANKS, baseUnit * 3 / 2).add((ItemLike)Blocks.BAMBOO_MOSAIC, baseUnit * 3 / 2).add(ItemTags.WOODEN_STAIRS, baseUnit * 3 / 2).add((ItemLike)Blocks.BAMBOO_MOSAIC_STAIRS, baseUnit * 3 / 2).add(ItemTags.WOODEN_SLABS, baseUnit * 3 / 4).add((ItemLike)Blocks.BAMBOO_MOSAIC_SLAB, baseUnit * 3 / 4).add(ItemTags.WOODEN_TRAPDOORS, baseUnit * 3 / 2).add(ItemTags.WOODEN_PRESSURE_PLATES, baseUnit * 3 / 2).add(ItemTags.WOODEN_SHELVES, baseUnit * 3 / 2).add(ItemTags.WOODEN_FENCES, baseUnit * 3 / 2).add(ItemTags.FENCE_GATES, baseUnit * 3 / 2).add((ItemLike)Blocks.NOTE_BLOCK, baseUnit * 3 / 2).add((ItemLike)Blocks.BOOKSHELF, baseUnit * 3 / 2).add((ItemLike)Blocks.CHISELED_BOOKSHELF, baseUnit * 3 / 2).add((ItemLike)Blocks.LECTERN, baseUnit * 3 / 2).add((ItemLike)Blocks.JUKEBOX, baseUnit * 3 / 2).add((ItemLike)Blocks.CHEST, baseUnit * 3 / 2).add((ItemLike)Blocks.TRAPPED_CHEST, baseUnit * 3 / 2).add((ItemLike)Blocks.CRAFTING_TABLE, baseUnit * 3 / 2).add((ItemLike)Blocks.DAYLIGHT_DETECTOR, baseUnit * 3 / 2).add(ItemTags.BANNERS, baseUnit * 3 / 2).add((ItemLike)Items.BOW, baseUnit * 3 / 2).add((ItemLike)Items.FISHING_ROD, baseUnit * 3 / 2).add((ItemLike)Blocks.LADDER, baseUnit * 3 / 2).add(ItemTags.SIGNS, baseUnit).add(ItemTags.HANGING_SIGNS, baseUnit * 4).add((ItemLike)Items.WOODEN_SHOVEL, baseUnit).add((ItemLike)Items.WOODEN_SWORD, baseUnit).add((ItemLike)Items.WOODEN_SPEAR, baseUnit).add((ItemLike)Items.WOODEN_HOE, baseUnit).add((ItemLike)Items.WOODEN_AXE, baseUnit).add((ItemLike)Items.WOODEN_PICKAXE, baseUnit).add(ItemTags.WOODEN_DOORS, baseUnit).add(ItemTags.BOATS, baseUnit * 6).add(ItemTags.WOOL, baseUnit / 2).add(ItemTags.WOODEN_BUTTONS, baseUnit / 2).add((ItemLike)Items.STICK, baseUnit / 2).add(ItemTags.SAPLINGS, baseUnit / 2).add((ItemLike)Items.BOWL, baseUnit / 2).add(ItemTags.WOOL_CARPETS, 1 + baseUnit / 3).add((ItemLike)Blocks.DRIED_KELP_BLOCK, 1 + baseUnit * 20).add((ItemLike)Items.CROSSBOW, baseUnit * 3 / 2).add((ItemLike)Blocks.BAMBOO, baseUnit / 4).add((ItemLike)Blocks.DEAD_BUSH, baseUnit / 2).add((ItemLike)Blocks.SHORT_DRY_GRASS, baseUnit / 2).add((ItemLike)Blocks.TALL_DRY_GRASS, baseUnit / 2).add((ItemLike)Blocks.SCAFFOLDING, baseUnit / 4).add((ItemLike)Blocks.LOOM, baseUnit * 3 / 2).add((ItemLike)Blocks.BARREL, baseUnit * 3 / 2).add((ItemLike)Blocks.CARTOGRAPHY_TABLE, baseUnit * 3 / 2).add((ItemLike)Blocks.FLETCHING_TABLE, baseUnit * 3 / 2).add((ItemLike)Blocks.SMITHING_TABLE, baseUnit * 3 / 2).add((ItemLike)Blocks.COMPOSTER, baseUnit * 3 / 2).add((ItemLike)Blocks.AZALEA, baseUnit / 2).add((ItemLike)Blocks.FLOWERING_AZALEA, baseUnit / 2).add((ItemLike)Blocks.MANGROVE_ROOTS, baseUnit * 3 / 2).add((ItemLike)Blocks.LEAF_LITTER, baseUnit / 2).remove(ItemTags.NON_FLAMMABLE_WOOD).build();
   }

   public static class Builder {
      private final HolderLookup items;
      private final FeatureFlagSet enabledFeatures;
      private final Object2IntSortedMap values = new Object2IntLinkedOpenHashMap();

      public Builder(final HolderLookup.Provider registries, final FeatureFlagSet enabledFeatures) {
         this.items = registries.lookupOrThrow(Registries.ITEM);
         this.enabledFeatures = enabledFeatures;
      }

      public FuelValues build() {
         return new FuelValues(this.values);
      }

      public Builder remove(final TagKey tag) {
         this.values.keySet().removeIf((item) -> item.builtInRegistryHolder().is(tag));
         return this;
      }

      public Builder add(final TagKey tag, final int time) {
         this.items.get(tag).ifPresent((items) -> {
            for(Holder item : items) {
               this.putInternal(time, (Item)item.value());
            }

         });
         return this;
      }

      public Builder add(final ItemLike itemLike, final int time) {
         Item item = itemLike.asItem();
         this.putInternal(time, item);
         return this;
      }

      private void putInternal(final int time, final Item item) {
         if (item.isEnabled(this.enabledFeatures)) {
            this.values.put(item, time);
         }

      }
   }
}
