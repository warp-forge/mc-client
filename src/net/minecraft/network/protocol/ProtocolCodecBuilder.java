package net.minecraft.network.protocol;

import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;

public class ProtocolCodecBuilder {
   private final IdDispatchCodec.Builder dispatchBuilder = IdDispatchCodec.builder(Packet::type);
   private final PacketFlow flow;

   public ProtocolCodecBuilder(final PacketFlow flow) {
      this.flow = flow;
   }

   public ProtocolCodecBuilder add(final PacketType type, final StreamCodec serializer) {
      if (type.flow() != this.flow) {
         String var10002 = String.valueOf(type);
         throw new IllegalArgumentException("Invalid packet flow for packet " + var10002 + ", expected " + this.flow.name());
      } else {
         this.dispatchBuilder.add(type, serializer);
         return this;
      }
   }

   public StreamCodec build() {
      return this.dispatchBuilder.build();
   }
}
