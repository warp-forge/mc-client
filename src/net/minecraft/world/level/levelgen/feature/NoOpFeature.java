package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class NoOpFeature extends Feature {
   public NoOpFeature(final Codec codec) {
      super(codec);
   }

   public boolean place(final FeaturePlaceContext context) {
      return true;
   }
}
