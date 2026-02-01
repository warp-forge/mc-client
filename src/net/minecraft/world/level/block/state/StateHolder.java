package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public abstract class StateHolder {
   public static final String NAME_TAG = "Name";
   public static final String PROPERTIES_TAG = "Properties";
   private static final Function PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function() {
      public String apply(final Map.@Nullable Entry entry) {
         if (entry == null) {
            return "<NULL>";
         } else {
            Property<?> property = (Property)entry.getKey();
            String var10000 = property.getName();
            return var10000 + "=" + this.getName(property, (Comparable)entry.getValue());
         }
      }

      private String getName(final Property property, final Comparable value) {
         return property.getName(value);
      }
   };
   protected final Object owner;
   private final Reference2ObjectArrayMap values;
   private Map neighbours;
   protected final MapCodec propertiesCodec;

   protected StateHolder(final Object owner, final Reference2ObjectArrayMap values, final MapCodec propertiesCodec) {
      this.owner = owner;
      this.values = values;
      this.propertiesCodec = propertiesCodec;
   }

   public Object cycle(final Property property) {
      return this.setValue(property, (Comparable)findNextInCollection(property.getPossibleValues(), this.getValue(property)));
   }

   protected static Object findNextInCollection(final List values, final Object current) {
      int nextIndex = values.indexOf(current) + 1;
      return nextIndex == values.size() ? values.getFirst() : values.get(nextIndex);
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.owner);
      if (!this.getValues().isEmpty()) {
         builder.append('[');
         builder.append((String)this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
         builder.append(']');
      }

      return builder.toString();
   }

   public final boolean equals(final Object obj) {
      return super.equals(obj);
   }

   public int hashCode() {
      return super.hashCode();
   }

   public Collection getProperties() {
      return Collections.unmodifiableCollection(this.values.keySet());
   }

   public boolean hasProperty(final Property property) {
      return this.values.containsKey(property);
   }

   public Comparable getValue(final Property property) {
      Comparable<?> value = (Comparable)this.values.get(property);
      if (value == null) {
         String var10002 = String.valueOf(property);
         throw new IllegalArgumentException("Cannot get property " + var10002 + " as it does not exist in " + String.valueOf(this.owner));
      } else {
         return (Comparable)property.getValueClass().cast(value);
      }
   }

   public Optional getOptionalValue(final Property property) {
      return Optional.ofNullable(this.getNullableValue(property));
   }

   public Comparable getValueOrElse(final Property property, final Comparable defaultValue) {
      return (Comparable)Objects.requireNonNullElse(this.getNullableValue(property), defaultValue);
   }

   private @Nullable Comparable getNullableValue(final Property property) {
      Comparable<?> value = (Comparable)this.values.get(property);
      return value == null ? null : (Comparable)property.getValueClass().cast(value);
   }

   public Object setValue(final Property property, final Comparable value) {
      Comparable<?> oldValue = (Comparable)this.values.get(property);
      if (oldValue == null) {
         String var10002 = String.valueOf(property);
         throw new IllegalArgumentException("Cannot set property " + var10002 + " as it does not exist in " + String.valueOf(this.owner));
      } else {
         return this.setValueInternal(property, value, oldValue);
      }
   }

   public Object trySetValue(final Property property, final Comparable value) {
      Comparable<?> oldValue = (Comparable)this.values.get(property);
      return oldValue == null ? this : this.setValueInternal(property, value, oldValue);
   }

   private Object setValueInternal(final Property property, final Comparable value, final Comparable oldValue) {
      if (oldValue.equals(value)) {
         return this;
      } else {
         int internalIndex = property.getInternalIndex(value);
         if (internalIndex < 0) {
            String var10002 = String.valueOf(property);
            throw new IllegalArgumentException("Cannot set property " + var10002 + " to " + String.valueOf(value) + " on " + String.valueOf(this.owner) + ", it is not an allowed value");
         } else {
            return ((Object[])this.neighbours.get(property))[internalIndex];
         }
      }
   }

   public void populateNeighbours(final Map statesByValues) {
      if (this.neighbours != null) {
         throw new IllegalStateException();
      } else {
         Map<Property<?>, S[]> neighbours = new Reference2ObjectArrayMap(this.values.size());
         ObjectIterator var3 = this.values.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry<Property<?>, Comparable<?>> entry = (Map.Entry)var3.next();
            Property<?> property = (Property)entry.getKey();
            neighbours.put(property, property.getPossibleValues().stream().map((value) -> statesByValues.get(this.makeNeighbourValues(property, value))).toArray());
         }

         this.neighbours = neighbours;
      }
   }

   private Map makeNeighbourValues(final Property property, final Comparable value) {
      Map<Property<?>, Comparable<?>> neighbour = new Reference2ObjectArrayMap(this.values);
      neighbour.put(property, value);
      return neighbour;
   }

   public Map getValues() {
      return this.values;
   }

   protected static Codec codec(final Codec ownerCodec, final Function defaultState) {
      return ownerCodec.dispatch("Name", (s) -> s.owner, (o) -> {
         S defaultValue = (S)((StateHolder)defaultState.apply(o));
         return defaultValue.getValues().isEmpty() ? MapCodec.unit(defaultValue) : defaultValue.propertiesCodec.codec().lenientOptionalFieldOf("Properties").xmap((oo) -> (StateHolder)oo.orElse(defaultValue), Optional::of);
      });
   }
}
