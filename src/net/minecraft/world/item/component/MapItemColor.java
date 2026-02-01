package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MapItemColor(int rgb) {
   public static final Codec CODEC;
   public static final StreamCodec STREAM_CODEC;
   public static final MapItemColor DEFAULT;

   static {
      CODEC = Codec.INT.xmap(MapItemColor::new, MapItemColor::rgb);
      STREAM_CODEC = ByteBufCodecs.INT.map(MapItemColor::new, MapItemColor::rgb);
      DEFAULT = new MapItemColor(4603950);
   }
}
