package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;

public class ClientboundUpdateAdvancementsPacket implements Packet {
   public static final StreamCodec STREAM_CODEC = Packet.codec(ClientboundUpdateAdvancementsPacket::write, ClientboundUpdateAdvancementsPacket::new);
   private final boolean reset;
   private final List added;
   private final Set removed;
   private final Map progress;
   private final boolean showAdvancements;

   public ClientboundUpdateAdvancementsPacket(final boolean reset, final Collection newAdvancements, final Set removedAdvancements, final Map progress, final boolean showAdvancements) {
      this.reset = reset;
      this.added = List.copyOf(newAdvancements);
      this.removed = Set.copyOf(removedAdvancements);
      this.progress = Map.copyOf(progress);
      this.showAdvancements = showAdvancements;
   }

   private ClientboundUpdateAdvancementsPacket(final RegistryFriendlyByteBuf input) {
      this.reset = input.readBoolean();
      this.added = (List)AdvancementHolder.LIST_STREAM_CODEC.decode(input);
      this.removed = (Set)input.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readIdentifier);
      this.progress = input.readMap(FriendlyByteBuf::readIdentifier, AdvancementProgress::fromNetwork);
      this.showAdvancements = input.readBoolean();
   }

   private void write(final RegistryFriendlyByteBuf output) {
      output.writeBoolean(this.reset);
      AdvancementHolder.LIST_STREAM_CODEC.encode(output, this.added);
      output.writeCollection(this.removed, FriendlyByteBuf::writeIdentifier);
      output.writeMap(this.progress, FriendlyByteBuf::writeIdentifier, (buffer, value) -> value.serializeToNetwork(buffer));
      output.writeBoolean(this.showAdvancements);
   }

   public PacketType type() {
      return GamePacketTypes.CLIENTBOUND_UPDATE_ADVANCEMENTS;
   }

   public void handle(final ClientGamePacketListener listener) {
      listener.handleUpdateAdvancementsPacket(this);
   }

   public List getAdded() {
      return this.added;
   }

   public Set getRemoved() {
      return this.removed;
   }

   public Map getProgress() {
      return this.progress;
   }

   public boolean shouldReset() {
      return this.reset;
   }

   public boolean shouldShowAdvancements() {
      return this.showAdvancements;
   }
}
