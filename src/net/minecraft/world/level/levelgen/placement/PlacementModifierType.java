package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface PlacementModifierType {
   PlacementModifierType BLOCK_PREDICATE_FILTER = register("block_predicate_filter", BlockPredicateFilter.CODEC);
   PlacementModifierType RARITY_FILTER = register("rarity_filter", RarityFilter.CODEC);
   PlacementModifierType SURFACE_RELATIVE_THRESHOLD_FILTER = register("surface_relative_threshold_filter", SurfaceRelativeThresholdFilter.CODEC);
   PlacementModifierType SURFACE_WATER_DEPTH_FILTER = register("surface_water_depth_filter", SurfaceWaterDepthFilter.CODEC);
   PlacementModifierType BIOME_FILTER = register("biome", BiomeFilter.CODEC);
   PlacementModifierType COUNT = register("count", CountPlacement.CODEC);
   PlacementModifierType NOISE_BASED_COUNT = register("noise_based_count", NoiseBasedCountPlacement.CODEC);
   PlacementModifierType NOISE_THRESHOLD_COUNT = register("noise_threshold_count", NoiseThresholdCountPlacement.CODEC);
   PlacementModifierType COUNT_ON_EVERY_LAYER = register("count_on_every_layer", CountOnEveryLayerPlacement.CODEC);
   PlacementModifierType ENVIRONMENT_SCAN = register("environment_scan", EnvironmentScanPlacement.CODEC);
   PlacementModifierType HEIGHTMAP = register("heightmap", HeightmapPlacement.CODEC);
   PlacementModifierType HEIGHT_RANGE = register("height_range", HeightRangePlacement.CODEC);
   PlacementModifierType IN_SQUARE = register("in_square", InSquarePlacement.CODEC);
   PlacementModifierType RANDOM_OFFSET = register("random_offset", RandomOffsetPlacement.CODEC);
   PlacementModifierType FIXED_PLACEMENT = register("fixed_placement", FixedPlacement.CODEC);

   MapCodec codec();

   private static PlacementModifierType register(final String id, final MapCodec codec) {
      return (PlacementModifierType)Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, (String)id, (PlacementModifierType)() -> codec);
   }
}
