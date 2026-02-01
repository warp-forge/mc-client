package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundSpectateEntityPacket(int entityId) implements Packet {
   public static final StreamCodec STREAM_CODEC;

   public PacketType type() {
      return GamePacketTypes.SERVERBOUND_SPECTATE_ENTITY;
   }

   public void handle(final ServerGamePacketListener listener) {
      listener.handleSpectateEntity(this);
   }

   static {
      STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundSpectateEntityPacket::entityId, ServerboundSpectateEntityPacket::new);
   }
}
