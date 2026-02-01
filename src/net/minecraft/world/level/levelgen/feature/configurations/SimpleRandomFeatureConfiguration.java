package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec CODEC;
   public final HolderSet features;

   public SimpleRandomFeatureConfiguration(final HolderSet features) {
      this.features = features;
   }

   public Stream getFeatures() {
      return this.features.stream().flatMap((f) -> ((PlacedFeature)f.value()).getFeatures());
   }

   static {
      CODEC = ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC).fieldOf("features").xmap(SimpleRandomFeatureConfiguration::new, (c) -> c.features).codec();
   }
}
