package net.minecraft.world.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClockState(long totalTicks, boolean paused) {
   public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(Codec.LONG.fieldOf("total_ticks").forGetter(ClockState::totalTicks), Codec.BOOL.optionalFieldOf("paused", false).forGetter(ClockState::paused)).apply(i, ClockState::new));
   public static final StreamCodec STREAM_CODEC;

   static {
      STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_LONG, ClockState::totalTicks, ByteBufCodecs.BOOL, ClockState::paused, ClockState::new);
   }
}
