package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomSelectorFeature extends Feature {
   public SimpleRandomSelectorFeature(final Codec codec) {
      super(codec);
   }

   public boolean place(final FeaturePlaceContext context) {
      RandomSource random = context.random();
      SimpleRandomFeatureConfiguration config = (SimpleRandomFeatureConfiguration)context.config();
      WorldGenLevel level = context.level();
      BlockPos origin = context.origin();
      ChunkGenerator chunkGenerator = context.chunkGenerator();
      int index = random.nextInt(config.features.size());
      PlacedFeature feature = (PlacedFeature)config.features.get(index).value();
      return feature.place(level, chunkGenerator, random, origin);
   }
}
