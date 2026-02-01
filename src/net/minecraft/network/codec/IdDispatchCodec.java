package net.minecraft.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.VarInt;

public class IdDispatchCodec implements StreamCodec {
   private static final int UNKNOWN_TYPE = -1;
   private final Function typeGetter;
   private final List byId;
   private final Object2IntMap toId;

   private IdDispatchCodec(final Function typeGetter, final List byId, final Object2IntMap toId) {
      this.typeGetter = typeGetter;
      this.byId = byId;
      this.toId = toId;
   }

   public Object decode(final ByteBuf input) {
      int id = VarInt.read(input);
      if (id >= 0 && id < this.byId.size()) {
         Entry<B, V, T> entry = (Entry)this.byId.get(id);

         try {
            return entry.serializer.decode(input);
         } catch (Exception e) {
            if (e instanceof DontDecorateException) {
               throw e;
            } else {
               throw new DecoderException("Failed to decode packet '" + String.valueOf(entry.type) + "'", e);
            }
         }
      } else {
         throw new DecoderException("Received unknown packet id " + id);
      }
   }

   public void encode(final ByteBuf output, final Object value) {
      T type = (T)this.typeGetter.apply(value);
      int id = this.toId.getOrDefault(type, -1);
      if (id == -1) {
         throw new EncoderException("Sending unknown packet '" + String.valueOf(type) + "'");
      } else {
         VarInt.write(output, id);
         Entry<B, V, T> entry = (Entry)this.byId.get(id);

         try {
            StreamCodec<? super B, V> codec = entry.serializer;
            codec.encode(output, value);
         } catch (Exception e) {
            if (e instanceof DontDecorateException) {
               throw e;
            } else {
               throw new EncoderException("Failed to encode packet '" + String.valueOf(type) + "'", e);
            }
         }
      }
   }

   public static Builder builder(final Function typeGetter) {
      return new Builder(typeGetter);
   }

   public static class Builder {
      private final List entries = new ArrayList();
      private final Function typeGetter;

      private Builder(final Function typeGetter) {
         this.typeGetter = typeGetter;
      }

      public Builder add(final Object type, final StreamCodec serializer) {
         this.entries.add(new Entry(serializer, type));
         return this;
      }

      public IdDispatchCodec build() {
         Object2IntOpenHashMap<T> toId = new Object2IntOpenHashMap();
         toId.defaultReturnValue(-2);

         for(Entry entry : this.entries) {
            int id = toId.size();
            int previous = toId.putIfAbsent(entry.type, id);
            if (previous != -2) {
               throw new IllegalStateException("Duplicate registration for type " + String.valueOf(entry.type));
            }
         }

         return new IdDispatchCodec(this.typeGetter, List.copyOf(this.entries), toId);
      }
   }

   private static record Entry(StreamCodec serializer, Object type) {
   }

   public interface DontDecorateException {
   }
}
