package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface IntProviderType {
   IntProviderType CONSTANT = register("constant", ConstantInt.CODEC);
   IntProviderType UNIFORM = register("uniform", UniformInt.CODEC);
   IntProviderType BIASED_TO_BOTTOM = register("biased_to_bottom", BiasedToBottomInt.CODEC);
   IntProviderType CLAMPED = register("clamped", ClampedInt.CODEC);
   IntProviderType WEIGHTED_LIST = register("weighted_list", WeightedListInt.CODEC);
   IntProviderType CLAMPED_NORMAL = register("clamped_normal", ClampedNormalInt.CODEC);

   MapCodec codec();

   static IntProviderType register(final String id, final MapCodec codec) {
      return (IntProviderType)Registry.register(BuiltInRegistries.INT_PROVIDER_TYPE, (String)id, (IntProviderType)() -> codec);
   }
}
