package net.minecraft.network.protocol.common.custom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.Identifier;

public interface CustomPacketPayload {
   Type type();

   static StreamCodec codec(final StreamMemberEncoder writer, final StreamDecoder reader) {
      return StreamCodec.ofMember(writer, reader);
   }

   static Type createType(final String id) {
      return new Type(Identifier.withDefaultNamespace(id));
   }

   static StreamCodec codec(final FallbackProvider fallback, final List types) {
      final Map<Identifier, StreamCodec<? super B, ? extends CustomPacketPayload>> idToType = (Map)types.stream().collect(Collectors.toUnmodifiableMap((t) -> t.type().id(), TypeAndCodec::codec));
      return new StreamCodec() {
         private StreamCodec findCodec(final Identifier typeId) {
            StreamCodec<? super B, ? extends CustomPacketPayload> codec = (StreamCodec)idToType.get(typeId);
            return codec != null ? codec : fallback.create(typeId);
         }

         private void writeCap(final FriendlyByteBuf output, final Type type, final CustomPacketPayload payload) {
            output.writeIdentifier(type.id());
            StreamCodec<B, T> codec = this.findCodec(type.id);
            codec.encode(output, payload);
         }

         public void encode(final FriendlyByteBuf output, final CustomPacketPayload value) {
            this.writeCap(output, value.type(), value);
         }

         public CustomPacketPayload decode(final FriendlyByteBuf input) {
            Identifier identifier = input.readIdentifier();
            return (CustomPacketPayload)this.findCodec(identifier).decode(input);
         }
      };
   }

   public static record Type(Identifier id) {
   }

   public static record TypeAndCodec(Type type, StreamCodec codec) {
   }

   public interface FallbackProvider {
      StreamCodec create(Identifier typeId);
   }
}
