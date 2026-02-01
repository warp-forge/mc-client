package net.minecraft.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.VisibleForDebug;

public class ExpirableValue {
   private final Object value;
   private long timeToLive;

   public ExpirableValue(final Object value, final long timeToLive) {
      this.value = value;
      this.timeToLive = timeToLive;
   }

   public void tick() {
      if (this.canExpire()) {
         --this.timeToLive;
      }

   }

   public static ExpirableValue of(final Object value) {
      return new ExpirableValue(value, Long.MAX_VALUE);
   }

   public static ExpirableValue of(final Object value, final long ticksUntilExpiry) {
      return new ExpirableValue(value, ticksUntilExpiry);
   }

   public long getTimeToLive() {
      return this.timeToLive;
   }

   public Object getValue() {
      return this.value;
   }

   public boolean hasExpired() {
      return this.timeToLive <= 0L;
   }

   public String toString() {
      String var10000 = String.valueOf(this.value);
      return var10000 + (this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
   }

   @VisibleForDebug
   public boolean canExpire() {
      return this.timeToLive != Long.MAX_VALUE;
   }

   public static Codec codec(final Codec valueCodec) {
      return RecordCodecBuilder.create((i) -> i.group(valueCodec.fieldOf("value").forGetter((v) -> v.value), Codec.LONG.lenientOptionalFieldOf("ttl").forGetter((v) -> v.canExpire() ? Optional.of(v.timeToLive) : Optional.empty())).apply(i, (value, ttl) -> new ExpirableValue(value, (Long)ttl.orElse(Long.MAX_VALUE))));
   }
}
