package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record ClientboundMoveVehiclePacket(Vec3 position, float yRot, float xRot) implements Packet {
   public static final StreamCodec STREAM_CODEC;

   public static ClientboundMoveVehiclePacket fromEntity(final Entity entity) {
      return new ClientboundMoveVehiclePacket(entity.position(), entity.getYRot(), entity.getXRot());
   }

   public PacketType type() {
      return GamePacketTypes.CLIENTBOUND_MOVE_VEHICLE;
   }

   public void handle(final ClientGamePacketListener listener) {
      listener.handleMoveVehicle(this);
   }

   static {
      STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, ClientboundMoveVehiclePacket::position, ByteBufCodecs.FLOAT, ClientboundMoveVehiclePacket::yRot, ByteBufCodecs.FLOAT, ClientboundMoveVehiclePacket::xRot, ClientboundMoveVehiclePacket::new);
   }
}
