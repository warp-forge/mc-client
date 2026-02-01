package net.minecraft.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import net.minecraft.nbt.Tag;

public class EncoderCache {
   private final LoadingCache cache;

   public EncoderCache(final int maximumSize) {
      this.cache = CacheBuilder.newBuilder().maximumSize((long)maximumSize).concurrencyLevel(1).softValues().build(new CacheLoader() {
         {
            Objects.requireNonNull(EncoderCache.this);
         }

         public DataResult load(final Key key) {
            return key.resolve();
         }
      });
   }

   public Codec wrap(final Codec codec) {
      return new Codec() {
         {
            Objects.requireNonNull(EncoderCache.this);
         }

         public DataResult decode(final DynamicOps ops, final Object input) {
            return codec.decode(ops, input);
         }

         public DataResult encode(final Object input, final DynamicOps ops, final Object prefix) {
            return ((DataResult)EncoderCache.this.cache.getUnchecked(new Key(codec, input, ops))).map((value) -> {
               if (value instanceof Tag tag) {
                  return tag.copy();
               } else {
                  return value;
               }
            });
         }
      };
   }

   private static record Key(Codec codec, Object value, DynamicOps ops) {
      public DataResult resolve() {
         return this.codec.encodeStart(this.ops, this.value);
      }

      public boolean equals(final Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof Key)) {
            return false;
         } else {
            Key<?, ?> key = (Key)obj;
            return this.codec == key.codec && this.value.equals(key.value) && this.ops.equals(key.ops);
         }
      }

      public int hashCode() {
         int result = System.identityHashCode(this.codec);
         result = 31 * result + this.value.hashCode();
         result = 31 * result + this.ops.hashCode();
         return result;
      }
   }
}
