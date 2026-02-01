package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
   public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(PlacedFeature.CODEC.fieldOf("feature_true").forGetter((c) -> c.featureTrue), PlacedFeature.CODEC.fieldOf("feature_false").forGetter((c) -> c.featureFalse)).apply(i, RandomBooleanFeatureConfiguration::new));
   public final Holder featureTrue;
   public final Holder featureFalse;

   public RandomBooleanFeatureConfiguration(final Holder featureTrue, final Holder featureFalse) {
      this.featureTrue = featureTrue;
      this.featureFalse = featureFalse;
   }

   public Stream getFeatures() {
      return Stream.concat(((PlacedFeature)this.featureTrue.value()).getFeatures(), ((PlacedFeature)this.featureFalse.value()).getFeatures());
   }
}
