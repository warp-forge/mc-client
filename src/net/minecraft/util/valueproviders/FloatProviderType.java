package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface FloatProviderType {
   FloatProviderType CONSTANT = register("constant", ConstantFloat.CODEC);
   FloatProviderType UNIFORM = register("uniform", UniformFloat.CODEC);
   FloatProviderType CLAMPED_NORMAL = register("clamped_normal", ClampedNormalFloat.CODEC);
   FloatProviderType TRAPEZOID = register("trapezoid", TrapezoidFloat.CODEC);

   MapCodec codec();

   static FloatProviderType register(final String id, final MapCodec codec) {
      return (FloatProviderType)Registry.register(BuiltInRegistries.FLOAT_PROVIDER_TYPE, (String)id, (FloatProviderType)() -> codec);
   }
}
