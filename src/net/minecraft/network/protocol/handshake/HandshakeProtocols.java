package net.minecraft.network.protocol.handshake;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.SimpleUnboundProtocol;

public class HandshakeProtocols {
   public static final SimpleUnboundProtocol SERVERBOUND_TEMPLATE;
   public static final ProtocolInfo SERVERBOUND;

   static {
      SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(ConnectionProtocol.HANDSHAKING, (builder) -> builder.addPacket(HandshakePacketTypes.CLIENT_INTENTION, ClientIntentionPacket.STREAM_CODEC));
      SERVERBOUND = SERVERBOUND_TEMPLATE.bind(FriendlyByteBuf::new);
   }
}
