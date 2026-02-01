package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface StructurePlacementType {
   StructurePlacementType RANDOM_SPREAD = register("random_spread", RandomSpreadStructurePlacement.CODEC);
   StructurePlacementType CONCENTRIC_RINGS = register("concentric_rings", ConcentricRingsStructurePlacement.CODEC);

   MapCodec codec();

   private static StructurePlacementType register(final String id, final MapCodec codec) {
      return (StructurePlacementType)Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, (String)id, (StructurePlacementType)() -> codec);
   }
}
