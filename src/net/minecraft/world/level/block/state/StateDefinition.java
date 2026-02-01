package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public class StateDefinition {
   private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private final Object owner;
   private final ImmutableSortedMap propertiesByName;
   private final ImmutableList states;

   protected StateDefinition(final Function defaultState, final Object owner, final Factory factory, final Map properties) {
      this.owner = owner;
      this.propertiesByName = ImmutableSortedMap.copyOf(properties);
      Supplier<S> defaultSupplier = () -> (StateHolder)defaultState.apply(owner);
      MapCodec<S> codec = MapCodec.of(Encoder.empty(), Decoder.unit(defaultSupplier));

      Map.Entry<String, Property<?>> entry;
      for(UnmodifiableIterator var7 = this.propertiesByName.entrySet().iterator(); var7.hasNext(); codec = appendPropertyCodec(codec, defaultSupplier, (String)entry.getKey(), (Property)entry.getValue())) {
         entry = (Map.Entry)var7.next();
      }

      Map<Map<Property<?>, Comparable<?>>, S> statesByValues = Maps.newLinkedHashMap();
      List<S> states = Lists.newArrayList();
      Stream<List<Pair<Property<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());

      Property<?> property;
      for(UnmodifiableIterator var11 = this.propertiesByName.values().iterator(); var11.hasNext(); stream = stream.flatMap((list) -> property.getPossibleValues().stream().map((value) -> {
            List<Pair<Property<?>, Comparable<?>>> newList = Lists.newArrayList(list);
            newList.add(Pair.of(property, value));
            return newList;
         }))) {
         property = (Property)var11.next();
      }

      stream.forEach((list) -> {
         Reference2ObjectArrayMap<Property<?>, Comparable<?>> map = new Reference2ObjectArrayMap(list.size());

         for(Pair pair : list) {
            map.put((Property)pair.getFirst(), (Comparable)pair.getSecond());
         }

         S blockState = (S)((StateHolder)factory.create(owner, map, codec));
         statesByValues.put(map, blockState);
         states.add(blockState);
      });

      for(StateHolder blockState : states) {
         blockState.populateNeighbours(statesByValues);
      }

      this.states = ImmutableList.copyOf(states);
   }

   private static MapCodec appendPropertyCodec(final MapCodec codec, final Supplier defaultSupplier, final String name, final Property property) {
      return Codec.mapPair(codec, property.valueCodec().fieldOf(name).orElseGet((e) -> {
      }, () -> property.value((StateHolder)defaultSupplier.get()))).xmap((pair) -> (StateHolder)((StateHolder)pair.getFirst()).setValue(property, ((Property.Value)pair.getSecond()).value()), (state) -> Pair.of(state, property.value(state)));
   }

   public ImmutableList getPossibleStates() {
      return this.states;
   }

   public StateHolder any() {
      return (StateHolder)this.states.get(0);
   }

   public Object getOwner() {
      return this.owner;
   }

   public Collection getProperties() {
      return this.propertiesByName.values();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
   }

   public @Nullable Property getProperty(final String name) {
      return (Property)this.propertiesByName.get(name);
   }

   public static class Builder {
      private final Object owner;
      private final Map properties = Maps.newHashMap();

      public Builder(final Object owner) {
         this.owner = owner;
      }

      public Builder add(final Property... properties) {
         for(Property property : properties) {
            this.validateProperty(property);
            this.properties.put(property.getName(), property);
         }

         return this;
      }

      private void validateProperty(final Property property) {
         String name = property.getName();
         if (!StateDefinition.NAME_PATTERN.matcher(name).matches()) {
            String var8 = String.valueOf(this.owner);
            throw new IllegalArgumentException(var8 + " has invalidly named property: " + name);
         } else {
            Collection<T> values = property.getPossibleValues();
            if (values.size() <= 1) {
               String var7 = String.valueOf(this.owner);
               throw new IllegalArgumentException(var7 + " attempted use property " + name + " with <= 1 possible values");
            } else {
               for(Comparable comparable : values) {
                  String valueName = property.getName(comparable);
                  if (!StateDefinition.NAME_PATTERN.matcher(valueName).matches()) {
                     throw new IllegalArgumentException(String.valueOf(this.owner) + " has property: " + name + " with invalidly named value: " + valueName);
                  }
               }

               if (this.properties.containsKey(name)) {
                  String var10002 = String.valueOf(this.owner);
                  throw new IllegalArgumentException(var10002 + " has duplicate property: " + name);
               }
            }
         }
      }

      public StateDefinition create(final Function defaultState, final Factory factory) {
         return new StateDefinition(defaultState, this.owner, factory, this.properties);
      }
   }

   public interface Factory {
      Object create(Object type, Reference2ObjectArrayMap values, final MapCodec propertiesCodec);
   }
}
