package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class FossilFeatureConfiguration implements FeatureConfiguration {
   public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(Identifier.CODEC.listOf().fieldOf("fossil_structures").forGetter((t) -> t.fossilStructures), Identifier.CODEC.listOf().fieldOf("overlay_structures").forGetter((t) -> t.overlayStructures), StructureProcessorType.LIST_CODEC.fieldOf("fossil_processors").forGetter((t) -> t.fossilProcessors), StructureProcessorType.LIST_CODEC.fieldOf("overlay_processors").forGetter((t) -> t.overlayProcessors), Codec.intRange(0, 7).fieldOf("max_empty_corners_allowed").forGetter((t) -> t.maxEmptyCornersAllowed)).apply(i, FossilFeatureConfiguration::new));
   public final List fossilStructures;
   public final List overlayStructures;
   public final Holder fossilProcessors;
   public final Holder overlayProcessors;
   public final int maxEmptyCornersAllowed;

   public FossilFeatureConfiguration(final List fossilStructures, final List overlayStructures, final Holder fossilProcessors, final Holder overlayProcessors, final int maxEmptyCornersAllowed) {
      if (fossilStructures.isEmpty()) {
         throw new IllegalArgumentException("Fossil structure lists need at least one entry");
      } else if (fossilStructures.size() != overlayStructures.size()) {
         throw new IllegalArgumentException("Fossil structure lists must be equal lengths");
      } else {
         this.fossilStructures = fossilStructures;
         this.overlayStructures = overlayStructures;
         this.fossilProcessors = fossilProcessors;
         this.overlayProcessors = overlayProcessors;
         this.maxEmptyCornersAllowed = maxEmptyCornersAllowed;
      }
   }
}
