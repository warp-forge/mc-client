package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public class BlankFormat implements NumberFormat {
   public static final BlankFormat INSTANCE = new BlankFormat();
   public static final NumberFormatType TYPE = new NumberFormatType() {
      private static final MapCodec CODEC;
      private static final StreamCodec STREAM_CODEC;

      public MapCodec mapCodec() {
         return CODEC;
      }

      public StreamCodec streamCodec() {
         return STREAM_CODEC;
      }

      static {
         CODEC = MapCodec.unit(BlankFormat.INSTANCE);
         STREAM_CODEC = StreamCodec.unit(BlankFormat.INSTANCE);
      }
   };

   private BlankFormat() {
   }

   public MutableComponent format(final int value) {
      return Component.empty();
   }

   public NumberFormatType type() {
      return TYPE;
   }
}
