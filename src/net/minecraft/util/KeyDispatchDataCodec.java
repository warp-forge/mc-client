package net.minecraft.util;

import com.mojang.serialization.MapCodec;

public record KeyDispatchDataCodec(MapCodec codec) {
   public static KeyDispatchDataCodec of(final MapCodec codec) {
      return new KeyDispatchDataCodec(codec);
   }
}
