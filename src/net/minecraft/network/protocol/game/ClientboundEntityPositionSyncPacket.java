package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;

public record ClientboundEntityPositionSyncPacket(int id, PositionMoveRotation values, boolean onGround) implements Packet {
   public static final StreamCodec STREAM_CODEC;

   public static ClientboundEntityPositionSyncPacket of(final Entity entity) {
      return new ClientboundEntityPositionSyncPacket(entity.getId(), new PositionMoveRotation(entity.trackingPosition(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()), entity.onGround());
   }

   public PacketType type() {
      return GamePacketTypes.CLIENTBOUND_ENTITY_POSITION_SYNC;
   }

   public void handle(final ClientGamePacketListener listener) {
      listener.handleEntityPositionSync(this);
   }

   static {
      STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundEntityPositionSyncPacket::id, PositionMoveRotation.STREAM_CODEC, ClientboundEntityPositionSyncPacket::values, ByteBufCodecs.BOOL, ClientboundEntityPositionSyncPacket::onGround, ClientboundEntityPositionSyncPacket::new);
   }
}
