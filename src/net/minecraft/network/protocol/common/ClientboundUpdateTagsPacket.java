package net.minecraft.network.protocol.common;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket implements Packet {
   public static final StreamCodec STREAM_CODEC = Packet.codec(ClientboundUpdateTagsPacket::write, ClientboundUpdateTagsPacket::new);
   private final Map tags;

   public ClientboundUpdateTagsPacket(final Map tags) {
      this.tags = tags;
   }

   private ClientboundUpdateTagsPacket(final FriendlyByteBuf input) {
      this.tags = input.readMap(FriendlyByteBuf::readRegistryKey, TagNetworkSerialization.NetworkPayload::read);
   }

   private void write(final FriendlyByteBuf output) {
      output.writeMap(this.tags, FriendlyByteBuf::writeResourceKey, (buffer, value) -> value.write(buffer));
   }

   public PacketType type() {
      return CommonPacketTypes.CLIENTBOUND_UPDATE_TAGS;
   }

   public void handle(final ClientCommonPacketListener listener) {
      listener.handleUpdateTags(this);
   }

   public Map getTags() {
      return this.tags;
   }
}
