package net.minecraft.network.protocol.status;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundStatusRequestPacket implements Packet {
   public static final ServerboundStatusRequestPacket INSTANCE = new ServerboundStatusRequestPacket();
   public static final StreamCodec STREAM_CODEC;

   private ServerboundStatusRequestPacket() {
   }

   public PacketType type() {
      return StatusPacketTypes.SERVERBOUND_STATUS_REQUEST;
   }

   public void handle(final ServerStatusPacketListener listener) {
      listener.handleStatusRequest(this);
   }

   static {
      STREAM_CODEC = StreamCodec.unit(INSTANCE);
   }
}
