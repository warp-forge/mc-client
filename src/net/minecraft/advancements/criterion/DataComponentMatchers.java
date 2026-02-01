package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.network.codec.StreamCodec;

public record DataComponentMatchers(DataComponentExactPredicate exact, Map partial) implements Predicate {
   public static final DataComponentMatchers ANY;
   public static final MapCodec CODEC;
   public static final StreamCodec STREAM_CODEC;

   public boolean test(final DataComponentGetter values) {
      if (!this.exact.test(values)) {
         return false;
      } else {
         for(DataComponentPredicate predicate : this.partial.values()) {
            if (!predicate.matches(values)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isEmpty() {
      return this.exact.isEmpty() && this.partial.isEmpty();
   }

   static {
      ANY = new DataComponentMatchers(DataComponentExactPredicate.EMPTY, Map.of());
      CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(DataComponentExactPredicate.CODEC.optionalFieldOf("components", DataComponentExactPredicate.EMPTY).forGetter(DataComponentMatchers::exact), DataComponentPredicate.CODEC.optionalFieldOf("predicates", Map.of()).forGetter(DataComponentMatchers::partial)).apply(i, DataComponentMatchers::new));
      STREAM_CODEC = StreamCodec.composite(DataComponentExactPredicate.STREAM_CODEC, DataComponentMatchers::exact, DataComponentPredicate.STREAM_CODEC, DataComponentMatchers::partial, DataComponentMatchers::new);
   }

   public static class Builder {
      private DataComponentExactPredicate exact;
      private final ImmutableMap.Builder partial;

      private Builder() {
         this.exact = DataComponentExactPredicate.EMPTY;
         this.partial = ImmutableMap.builder();
      }

      public static Builder components() {
         return new Builder();
      }

      public Builder any(final DataComponentType type) {
         DataComponentPredicate.AnyValueType predicateType = DataComponentPredicate.AnyValueType.create(type);
         this.partial.put(predicateType, predicateType.predicate());
         return this;
      }

      public Builder partial(final DataComponentPredicate.Type type, final DataComponentPredicate predicate) {
         this.partial.put(type, predicate);
         return this;
      }

      public Builder exact(final DataComponentExactPredicate exact) {
         this.exact = exact;
         return this;
      }

      public DataComponentMatchers build() {
         return new DataComponentMatchers(this.exact, this.partial.buildOrThrow());
      }
   }
}
