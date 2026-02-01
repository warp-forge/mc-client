package net.minecraft.network.protocol.status;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.RegistryOps;

public record ClientboundStatusResponsePacket(ServerStatus status) implements Packet {
   private static final RegistryOps OPS;
   public static final StreamCodec STREAM_CODEC;

   public PacketType type() {
      return StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE;
   }

   public void handle(final ClientStatusPacketListener listener) {
      listener.handleStatusResponse(this);
   }

   static {
      OPS = RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE);
      STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.lenientJson(32767).apply(ByteBufCodecs.fromCodec((DynamicOps)OPS, (Codec)ServerStatus.CODEC)), ClientboundStatusResponsePacket::status, ClientboundStatusResponsePacket::new);
   }
}
