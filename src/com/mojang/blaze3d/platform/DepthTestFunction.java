package com.mojang.blaze3d.platform;

public enum DepthTestFunction {
   NO_DEPTH_TEST,
   EQUAL_DEPTH_TEST,
   LEQUAL_DEPTH_TEST,
   LESS_DEPTH_TEST,
   GREATER_DEPTH_TEST;

   // $FF: synthetic method
   private static DepthTestFunction[] $values() {
      return new DepthTestFunction[]{NO_DEPTH_TEST, EQUAL_DEPTH_TEST, LEQUAL_DEPTH_TEST, LESS_DEPTH_TEST, GREATER_DEPTH_TEST};
   }
}
