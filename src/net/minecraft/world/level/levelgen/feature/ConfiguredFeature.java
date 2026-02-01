package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ConfiguredFeature(Feature feature, FeatureConfiguration config) {
   public static final Codec DIRECT_CODEC;
   public static final Codec CODEC;
   public static final Codec LIST_CODEC;

   public boolean place(final WorldGenLevel level, final ChunkGenerator chunkGenerator, final RandomSource random, final BlockPos origin) {
      return this.feature.place(this.config, level, chunkGenerator, random, origin);
   }

   public Stream getFeatures() {
      return Stream.concat(Stream.of(this), this.config.getFeatures());
   }

   public String toString() {
      String var10000 = String.valueOf(this.feature);
      return "Configured: " + var10000 + ": " + String.valueOf(this.config);
   }

   static {
      DIRECT_CODEC = BuiltInRegistries.FEATURE.byNameCodec().dispatch((f) -> f.feature, Feature::configuredCodec);
      CODEC = RegistryFileCodec.create(Registries.CONFIGURED_FEATURE, DIRECT_CODEC);
      LIST_CODEC = RegistryCodecs.homogeneousList(Registries.CONFIGURED_FEATURE, DIRECT_CODEC);
   }
}
