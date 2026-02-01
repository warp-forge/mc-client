package net.minecraft.world.attribute;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public interface LerpFunction {
   static LerpFunction ofFloat() {
      return Mth::lerp;
   }

   static LerpFunction ofDegrees(final float maxDelta) {
      return (alpha, from, to) -> {
         float delta = Mth.wrapDegrees(to - from);
         return Math.abs(delta) >= maxDelta ? to : from + alpha * delta;
      };
   }

   static LerpFunction ofConstant() {
      return (alpha, from, to) -> from;
   }

   static LerpFunction ofStep(final float threshold) {
      return (alpha, from, to) -> alpha >= threshold ? to : from;
   }

   static LerpFunction ofColor() {
      return ARGB::srgbLerp;
   }

   Object apply(float alpha, Object from, Object to);
}
