package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;

public abstract class IntProvider {
   private static final Codec CONSTANT_OR_DISPATCH_CODEC;
   public static final Codec CODEC;
   public static final Codec NON_NEGATIVE_CODEC;
   public static final Codec POSITIVE_CODEC;

   public static Codec codec(final int minValue, final int maxValue) {
      return validateCodec(minValue, maxValue, CODEC);
   }

   public static Codec validateCodec(final int minValue, final int maxValue, final Codec codec) {
      return codec.validate((value) -> validate(minValue, maxValue, value));
   }

   private static DataResult validate(final int minValue, final int maxValue, final IntProvider value) {
      if (value.getMinValue() < minValue) {
         return DataResult.error(() -> "Value provider too low: " + minValue + " [" + value.getMinValue() + "-" + value.getMaxValue() + "]");
      } else {
         return value.getMaxValue() > maxValue ? DataResult.error(() -> "Value provider too high: " + maxValue + " [" + value.getMinValue() + "-" + value.getMaxValue() + "]") : DataResult.success(value);
      }
   }

   public abstract int sample(final RandomSource random);

   public abstract int getMinValue();

   public abstract int getMaxValue();

   public abstract IntProviderType getType();

   static {
      CONSTANT_OR_DISPATCH_CODEC = Codec.either(Codec.INT, BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec));
      CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap((either) -> (IntProvider)either.map(ConstantInt::of, (f) -> f), (f) -> f.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)f).getValue()) : Either.right(f));
      NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
      POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);
   }
}
