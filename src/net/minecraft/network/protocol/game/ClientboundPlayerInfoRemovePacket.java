package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPlayerInfoRemovePacket(List profileIds) implements Packet {
   public static final StreamCodec STREAM_CODEC = Packet.codec(ClientboundPlayerInfoRemovePacket::write, ClientboundPlayerInfoRemovePacket::new);

   private ClientboundPlayerInfoRemovePacket(final FriendlyByteBuf input) {
      this(input.readList(UUIDUtil.STREAM_CODEC));
   }

   private void write(final FriendlyByteBuf output) {
      output.writeCollection(this.profileIds, UUIDUtil.STREAM_CODEC);
   }

   public PacketType type() {
      return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_REMOVE;
   }

   public void handle(final ClientGamePacketListener listener) {
      listener.handlePlayerInfoRemove(this);
   }
}
