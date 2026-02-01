package net.minecraft.client.renderer.block.model;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface VariantMutator extends UnaryOperator {
   VariantProperty X_ROT = Variant::withXRot;
   VariantProperty Y_ROT = Variant::withYRot;
   VariantProperty Z_ROT = Variant::withZRot;
   VariantProperty MODEL = Variant::withModel;
   VariantProperty UV_LOCK = Variant::withUvLock;

   default VariantMutator then(final VariantMutator other) {
      return (variant) -> (Variant)other.apply((Variant)this.apply(variant));
   }

   @FunctionalInterface
   public interface VariantProperty {
      Variant apply(Variant input, Object value);

      default VariantMutator withValue(final Object value) {
         return (variant) -> this.apply(variant, value);
      }
   }
}
