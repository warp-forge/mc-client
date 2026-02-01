package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends IntrinsicHolderTagsProvider {
   public FluidTagsProvider(final PackOutput output, final CompletableFuture lookupProvider) {
      super(output, Registries.FLUID, lookupProvider, (e) -> e.builtInRegistryHolder().key());
   }

   protected void addTags(final HolderLookup.Provider registries) {
      this.tag(FluidTags.WATER).add((Object[])(Fluids.WATER, Fluids.FLOWING_WATER));
      this.tag(FluidTags.LAVA).add((Object[])(Fluids.LAVA, Fluids.FLOWING_LAVA));
      this.tag(FluidTags.SUPPORTS_SUGAR_CANE_ADJACENTLY).addTag(FluidTags.WATER);
      this.tag(FluidTags.SUPPORTS_LILY_PAD).add((Object)Fluids.WATER);
      this.tag(FluidTags.SUPPORTS_FROGSPAWN).add((Object)Fluids.WATER);
      this.tag(FluidTags.BUBBLE_COLUMN_CAN_OCCUPY).add((Object)Fluids.WATER);
   }
}
