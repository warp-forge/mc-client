package net.minecraft.world.attribute;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import org.jspecify.annotations.Nullable;

public final class EnvironmentAttributeMap {
   public static final EnvironmentAttributeMap EMPTY = new EnvironmentAttributeMap(Map.of());
   public static final Codec CODEC = Codec.lazyInitialized(() -> Codec.dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(Entry::createCodec)).xmap(EnvironmentAttributeMap::new, (v) -> v.entries));
   public static final Codec NETWORK_CODEC;
   public static final Codec CODEC_ONLY_POSITIONAL;
   private final Map entries;

   private static EnvironmentAttributeMap filterSyncable(final EnvironmentAttributeMap attributes) {
      return new EnvironmentAttributeMap(Map.copyOf(Maps.filterKeys(attributes.entries, EnvironmentAttribute::isSyncable)));
   }

   private EnvironmentAttributeMap(final Map entries) {
      this.entries = entries;
   }

   public static Builder builder() {
      return new Builder();
   }

   public @Nullable Entry get(final EnvironmentAttribute attribute) {
      return (Entry)this.entries.get(attribute);
   }

   public Object applyModifier(final EnvironmentAttribute attribute, final Object baseValue) {
      Entry<Value, ?> entry = this.get(attribute);
      return entry != null ? entry.applyModifier(baseValue) : baseValue;
   }

   public boolean contains(final EnvironmentAttribute attribute) {
      return this.entries.containsKey(attribute);
   }

   public Set keySet() {
      return this.entries.keySet();
   }

   public boolean equals(final Object obj) {
      if (obj == this) {
         return true;
      } else {
         boolean var10000;
         if (obj instanceof EnvironmentAttributeMap) {
            EnvironmentAttributeMap attributes = (EnvironmentAttributeMap)obj;
            if (this.entries.equals(attributes.entries)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   public int hashCode() {
      return this.entries.hashCode();
   }

   public String toString() {
      return this.entries.toString();
   }

   static {
      NETWORK_CODEC = CODEC.xmap(EnvironmentAttributeMap::filterSyncable, EnvironmentAttributeMap::filterSyncable);
      CODEC_ONLY_POSITIONAL = CODEC.validate((map) -> {
         List<EnvironmentAttribute<?>> illegalAttributes = map.keySet().stream().filter((attribute) -> !attribute.isPositional()).toList();
         return !illegalAttributes.isEmpty() ? DataResult.error(() -> "The following attributes cannot be positional: " + String.valueOf(illegalAttributes)) : DataResult.success(map);
      });
   }

   public static record Entry(Object argument, AttributeModifier modifier) {
      private static Codec createCodec(final EnvironmentAttribute attribute) {
         Codec<Entry<Value, ?>> fullCodec = attribute.type().modifierCodec().dispatch("modifier", Entry::modifier, Util.memoize((Function)((modifier) -> createFullCodec(attribute, modifier))));
         return Codec.either(attribute.valueCodec(), fullCodec).xmap((either) -> (Entry)either.map((value) -> new Entry(value, AttributeModifier.override()), (e) -> e), (entry) -> entry.modifier == AttributeModifier.override() ? Either.left(entry.argument()) : Either.right(entry));
      }

      private static MapCodec createFullCodec(final EnvironmentAttribute attribute, final AttributeModifier modifier) {
         return RecordCodecBuilder.mapCodec((i) -> i.group(modifier.argumentCodec(attribute).fieldOf("argument").forGetter(Entry::argument)).apply(i, (value) -> new Entry(value, modifier)));
      }

      public Object applyModifier(final Object subject) {
         return this.modifier.apply(subject, this.argument);
      }
   }

   public static class Builder {
      private final Map entries = new HashMap();

      private Builder() {
      }

      public Builder putAll(final EnvironmentAttributeMap map) {
         this.entries.putAll(map.entries);
         return this;
      }

      public Builder modify(final EnvironmentAttribute attribute, final AttributeModifier modifier, final Object value) {
         attribute.type().checkAllowedModifier(modifier);
         this.entries.put(attribute, new Entry(value, modifier));
         return this;
      }

      public Builder set(final EnvironmentAttribute attribute, final Object value) {
         return this.modify(attribute, AttributeModifier.override(), value);
      }

      public EnvironmentAttributeMap build() {
         return this.entries.isEmpty() ? EnvironmentAttributeMap.EMPTY : new EnvironmentAttributeMap(Map.copyOf(this.entries));
      }
   }
}
