package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.Objects;
import java.util.function.Function;

public interface BoundedFloatFunction {
   BoundedFloatFunction IDENTITY = createUnlimited((input) -> input);

   float apply(final Object c);

   float minValue();

   float maxValue();

   static BoundedFloatFunction createUnlimited(final Float2FloatFunction function) {
      return new BoundedFloatFunction() {
         public float apply(final Float aFloat) {
            return (Float)function.apply(aFloat);
         }

         public float minValue() {
            return Float.NEGATIVE_INFINITY;
         }

         public float maxValue() {
            return Float.POSITIVE_INFINITY;
         }
      };
   }

   default BoundedFloatFunction comap(final Function function) {
      return new BoundedFloatFunction() {
         {
            Objects.requireNonNull(BoundedFloatFunction.this);
         }

         public float apply(final Object c2) {
            return BoundedFloatFunction.this.apply(function.apply(c2));
         }

         public float minValue() {
            return BoundedFloatFunction.this.minValue();
         }

         public float maxValue() {
            return BoundedFloatFunction.this.maxValue();
         }
      };
   }
}
