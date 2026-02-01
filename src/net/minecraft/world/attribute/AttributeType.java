package net.minecraft.world.attribute;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public record AttributeType(Codec valueCodec, Map modifierLibrary, Codec modifierCodec, LerpFunction keyframeLerp, LerpFunction stateChangeLerp, LerpFunction spatialLerp, LerpFunction partialTickLerp) {
   public static AttributeType ofInterpolated(final Codec valueCodec, final Map modifierLibrary, final LerpFunction lerp) {
      return ofInterpolated(valueCodec, modifierLibrary, lerp, lerp);
   }

   public static AttributeType ofInterpolated(final Codec valueCodec, final Map modifierLibrary, final LerpFunction lerp, final LerpFunction partialTickLerp) {
      return new AttributeType(valueCodec, modifierLibrary, createModifierCodec(modifierLibrary), lerp, lerp, lerp, partialTickLerp);
   }

   public static AttributeType ofNotInterpolated(final Codec valueCodec, final Map modifierLibrary) {
      return new AttributeType(valueCodec, modifierLibrary, createModifierCodec(modifierLibrary), LerpFunction.ofStep(1.0F), LerpFunction.ofStep(0.0F), LerpFunction.ofStep(0.5F), LerpFunction.ofStep(0.0F));
   }

   public static AttributeType ofNotInterpolated(final Codec valueCodec) {
      return ofNotInterpolated(valueCodec, Map.of());
   }

   private static Codec createModifierCodec(final Map modifiers) {
      ImmutableBiMap<AttributeModifier.OperationId, AttributeModifier<Value, ?>> modifierLookup = ImmutableBiMap.builder().put(AttributeModifier.OperationId.OVERRIDE, AttributeModifier.override()).putAll(modifiers).buildOrThrow();
      Codec var10000 = AttributeModifier.OperationId.CODEC;
      Objects.requireNonNull(modifierLookup);
      Function var10001 = modifierLookup::get;
      ImmutableBiMap var10002 = modifierLookup.inverse();
      Objects.requireNonNull(var10002);
      return ExtraCodecs.idResolverCodec(var10000, var10001, var10002::get);
   }

   public void checkAllowedModifier(final AttributeModifier modifier) {
      if (modifier != AttributeModifier.override() && !this.modifierLibrary.containsValue(modifier)) {
         String var10002 = String.valueOf(modifier);
         throw new IllegalArgumentException("Modifier " + var10002 + " is not valid for " + String.valueOf(this));
      }
   }

   public String toString() {
      return Util.getRegisteredName(BuiltInRegistries.ATTRIBUTE_TYPE, this);
   }
}
