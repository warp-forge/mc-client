package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public record StructureSet(List structures, StructurePlacement placement) {
   public static final Codec DIRECT_CODEC = RecordCodecBuilder.create((i) -> i.group(StructureSet.StructureSelectionEntry.CODEC.listOf().fieldOf("structures").forGetter(StructureSet::structures), StructurePlacement.CODEC.fieldOf("placement").forGetter(StructureSet::placement)).apply(i, StructureSet::new));
   public static final Codec CODEC;

   public StructureSet(final Holder singleEntry, final StructurePlacement placement) {
      this(List.of(new StructureSelectionEntry(singleEntry, 1)), placement);
   }

   public static StructureSelectionEntry entry(final Holder structure, final int weight) {
      return new StructureSelectionEntry(structure, weight);
   }

   public static StructureSelectionEntry entry(final Holder structure) {
      return new StructureSelectionEntry(structure, 1);
   }

   static {
      CODEC = RegistryFileCodec.create(Registries.STRUCTURE_SET, DIRECT_CODEC);
   }

   public static record StructureSelectionEntry(Holder structure, int weight) {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(Structure.CODEC.fieldOf("structure").forGetter(StructureSelectionEntry::structure), ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(StructureSelectionEntry::weight)).apply(i, StructureSelectionEntry::new));
   }
}
