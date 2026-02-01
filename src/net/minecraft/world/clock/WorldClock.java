package net.minecraft.world.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;

public record WorldClock() {
   public static final Codec CODEC;
   public static final StreamCodec STREAM_CODEC;
   public static final Codec DIRECT_CODEC;

   static {
      CODEC = RegistryFixedCodec.create(Registries.WORLD_CLOCK);
      STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WORLD_CLOCK);
      DIRECT_CODEC = MapCodec.unitCodec(WorldClock::new);
   }
}
