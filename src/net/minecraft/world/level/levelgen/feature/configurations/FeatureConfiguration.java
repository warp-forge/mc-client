package net.minecraft.world.level.levelgen.feature.configurations;

import java.util.stream.Stream;

public interface FeatureConfiguration {
   NoneFeatureConfiguration NONE = NoneFeatureConfiguration.INSTANCE;

   default Stream getFeatures() {
      return Stream.empty();
   }
}
