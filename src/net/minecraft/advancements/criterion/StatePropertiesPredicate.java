package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public record StatePropertiesPredicate(List properties) {
   private static final Codec PROPERTIES_CODEC;
   public static final Codec CODEC;
   public static final StreamCodec STREAM_CODEC;

   public boolean matches(final StateDefinition definition, final StateHolder state) {
      for(PropertyMatcher matcher : this.properties) {
         if (!matcher.match(definition, state)) {
            return false;
         }
      }

      return true;
   }

   public boolean matches(final BlockState state) {
      return this.matches(state.getBlock().getStateDefinition(), state);
   }

   public boolean matches(final FluidState state) {
      return this.matches(state.getType().getStateDefinition(), state);
   }

   public Optional checkState(final StateDefinition states) {
      for(PropertyMatcher property : this.properties) {
         Optional<String> unknownProperty = property.checkState(states);
         if (unknownProperty.isPresent()) {
            return unknownProperty;
         }
      }

      return Optional.empty();
   }

   static {
      PROPERTIES_CODEC = Codec.unboundedMap(Codec.STRING, StatePropertiesPredicate.ValueMatcher.CODEC).xmap((map) -> map.entrySet().stream().map((entry) -> new PropertyMatcher((String)entry.getKey(), (ValueMatcher)entry.getValue())).toList(), (properties) -> (Map)properties.stream().collect(Collectors.toMap(PropertyMatcher::name, PropertyMatcher::valueMatcher)));
      CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
      STREAM_CODEC = StatePropertiesPredicate.PropertyMatcher.STREAM_CODEC.apply(ByteBufCodecs.list()).map(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
   }

   private static record PropertyMatcher(String name, ValueMatcher valueMatcher) {
      public static final StreamCodec STREAM_CODEC;

      public boolean match(final StateDefinition definition, final StateHolder state) {
         Property<?> property = definition.getProperty(this.name);
         return property != null && this.valueMatcher.match(state, property);
      }

      public Optional checkState(final StateDefinition states) {
         Property<?> property = states.getProperty(this.name);
         return property != null ? Optional.empty() : Optional.of(this.name);
      }

      static {
         STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PropertyMatcher::name, StatePropertiesPredicate.ValueMatcher.STREAM_CODEC, PropertyMatcher::valueMatcher, PropertyMatcher::new);
      }
   }

   private interface ValueMatcher {
      Codec CODEC = Codec.either(StatePropertiesPredicate.ExactMatcher.CODEC, StatePropertiesPredicate.RangedMatcher.CODEC).xmap(Either::unwrap, (matcher) -> {
         if (matcher instanceof ExactMatcher exact) {
            return Either.left(exact);
         } else if (matcher instanceof RangedMatcher ranged) {
            return Either.right(ranged);
         } else {
            throw new UnsupportedOperationException();
         }
      });
      StreamCodec STREAM_CODEC = ByteBufCodecs.either(StatePropertiesPredicate.ExactMatcher.STREAM_CODEC, StatePropertiesPredicate.RangedMatcher.STREAM_CODEC).map(Either::unwrap, (matcher) -> {
         if (matcher instanceof ExactMatcher exact) {
            return Either.left(exact);
         } else if (matcher instanceof RangedMatcher ranged) {
            return Either.right(ranged);
         } else {
            throw new UnsupportedOperationException();
         }
      });

      boolean match(StateHolder state, Property property);
   }

   private static record ExactMatcher(String value) implements ValueMatcher {
      public static final Codec CODEC;
      public static final StreamCodec STREAM_CODEC;

      public boolean match(final StateHolder state, final Property property) {
         T actualValue = (T)state.getValue(property);
         Optional<T> typedExpected = property.getValue(this.value);
         return typedExpected.isPresent() && actualValue.compareTo((Comparable)typedExpected.get()) == 0;
      }

      static {
         CODEC = Codec.STRING.xmap(ExactMatcher::new, ExactMatcher::value);
         STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ExactMatcher::new, ExactMatcher::value);
      }
   }

   private static record RangedMatcher(Optional minValue, Optional maxValue) implements ValueMatcher {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(Codec.STRING.optionalFieldOf("min").forGetter(RangedMatcher::minValue), Codec.STRING.optionalFieldOf("max").forGetter(RangedMatcher::maxValue)).apply(i, RangedMatcher::new));
      public static final StreamCodec STREAM_CODEC;

      public boolean match(final StateHolder state, final Property property) {
         T value = (T)state.getValue(property);
         if (this.minValue.isPresent()) {
            Optional<T> typedMinValue = property.getValue((String)this.minValue.get());
            if (typedMinValue.isEmpty() || value.compareTo((Comparable)typedMinValue.get()) < 0) {
               return false;
            }
         }

         if (this.maxValue.isPresent()) {
            Optional<T> typedMaxValue = property.getValue((String)this.maxValue.get());
            if (typedMaxValue.isEmpty() || value.compareTo((Comparable)typedMaxValue.get()) > 0) {
               return false;
            }
         }

         return true;
      }

      static {
         STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), RangedMatcher::minValue, ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), RangedMatcher::maxValue, RangedMatcher::new);
      }
   }

   public static class Builder {
      private final ImmutableList.Builder matchers = ImmutableList.builder();

      private Builder() {
      }

      public static Builder properties() {
         return new Builder();
      }

      public Builder hasProperty(final Property property, final String value) {
         this.matchers.add(new PropertyMatcher(property.getName(), new ExactMatcher(value)));
         return this;
      }

      public Builder hasProperty(final Property property, final int value) {
         return this.hasProperty(property, Integer.toString(value));
      }

      public Builder hasProperty(final Property property, final boolean value) {
         return this.hasProperty(property, Boolean.toString(value));
      }

      public Builder hasProperty(final Property property, final Comparable value) {
         return this.hasProperty(property, ((StringRepresentable)value).getSerializedName());
      }

      public Optional build() {
         return Optional.of(new StatePropertiesPredicate(this.matchers.build()));
      }
   }
}
