package net.minecraft.world.attribute;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.timeline.Timeline;
import org.jspecify.annotations.Nullable;

public class EnvironmentAttributeSystem implements EnvironmentAttributeReader {
   private final Map attributeSamplers = new Reference2ObjectOpenHashMap();

   private EnvironmentAttributeSystem(final Map layersByAttribute) {
      layersByAttribute.forEach((attribute, layers) -> this.attributeSamplers.put(attribute, this.bakeLayerSampler(attribute, layers)));
   }

   private ValueSampler bakeLayerSampler(final EnvironmentAttribute attribute, final List untypedLayers) {
      List<EnvironmentAttributeLayer<Value>> layers = new ArrayList(untypedLayers);
      Value constantBaseValue = (Value)attribute.defaultValue();

      while(!layers.isEmpty()) {
         Object var6 = layers.getFirst();
         if (!(var6 instanceof EnvironmentAttributeLayer.Constant)) {
            break;
         }

         EnvironmentAttributeLayer.Constant<Value> constantLayer = (EnvironmentAttributeLayer.Constant)var6;
         constantBaseValue = (Value)constantLayer.applyConstant(constantBaseValue);
         layers.removeFirst();
      }

      boolean isAffectedByPosition = layers.stream().anyMatch((layer) -> layer instanceof EnvironmentAttributeLayer.Positional);
      return new ValueSampler(attribute, constantBaseValue, List.copyOf(layers), isAffectedByPosition);
   }

   public static Builder builder() {
      return new Builder();
   }

   private static void addDefaultLayers(final Builder builder, final Level level) {
      RegistryAccess registries = level.registryAccess();
      BiomeManager biomeManager = level.getBiomeManager();
      ClockManager clockManager = level.clockManager();
      addDimensionLayer(builder, level.dimensionType());
      addBiomeLayer(builder, registries.lookupOrThrow(Registries.BIOME), biomeManager);
      level.dimensionType().timelines().forEach((timeline) -> builder.addTimelineLayer(timeline, clockManager));
      if (level.canHaveWeather()) {
         WeatherAttributes.addBuiltinLayers(builder, WeatherAttributes.WeatherAccess.from(level));
      }

   }

   private static void addDimensionLayer(final Builder builder, final DimensionType dimensionType) {
      builder.addConstantLayer(dimensionType.attributes());
   }

   private static void addBiomeLayer(final Builder builder, final HolderLookup biomes, final BiomeManager biomeManager) {
      Stream<EnvironmentAttribute<?>> attributesProvidedByBiomes = biomes.listElements().flatMap((biome) -> ((Biome)biome.value()).getAttributes().keySet().stream()).distinct();
      attributesProvidedByBiomes.forEach((attribute) -> addBiomeLayerForAttribute(builder, attribute, biomeManager));
   }

   private static void addBiomeLayerForAttribute(final Builder builder, final EnvironmentAttribute attribute, final BiomeManager biomeManager) {
      builder.addPositionalLayer(attribute, (baseValue, pos, biomeWeights) -> {
         if (biomeWeights != null && attribute.isSpatiallyInterpolated()) {
            return biomeWeights.applyAttributeLayer(attribute, baseValue);
         } else {
            Holder<Biome> biome = biomeManager.getNoiseBiomeAtPosition(pos.x, pos.y, pos.z);
            return ((Biome)biome.value()).getAttributes().applyModifier(attribute, baseValue);
         }
      });
   }

   public void invalidateTickCache() {
      this.attributeSamplers.values().forEach(ValueSampler::invalidateTickCache);
   }

   private @Nullable ValueSampler getValueSampler(final EnvironmentAttribute attribute) {
      return (ValueSampler)this.attributeSamplers.get(attribute);
   }

   public Object getDimensionValue(final EnvironmentAttribute attribute) {
      if (SharedConstants.IS_RUNNING_IN_IDE && attribute.isPositional()) {
         throw new IllegalStateException("Position must always be provided for positional attribute " + String.valueOf(attribute));
      } else {
         ValueSampler<Value> sampler = this.getValueSampler(attribute);
         return sampler == null ? attribute.defaultValue() : sampler.getDimensionValue();
      }
   }

   public Object getValue(final EnvironmentAttribute attribute, final Vec3 pos, final @Nullable SpatialAttributeInterpolator biomeInterpolator) {
      ValueSampler<Value> sampler = this.getValueSampler(attribute);
      return sampler == null ? attribute.defaultValue() : sampler.getValue(pos, biomeInterpolator);
   }

   @VisibleForTesting
   Object getConstantBaseValue(final EnvironmentAttribute attribute) {
      ValueSampler<Value> sampler = this.getValueSampler(attribute);
      return sampler != null ? sampler.baseValue : attribute.defaultValue();
   }

   @VisibleForTesting
   boolean isAffectedByPosition(final EnvironmentAttribute attribute) {
      ValueSampler<?> sampler = this.getValueSampler(attribute);
      return sampler != null && sampler.isAffectedByPosition;
   }

   public static class Builder {
      private final Map layersByAttribute = new HashMap();

      private Builder() {
      }

      public Builder addDefaultLayers(final Level level) {
         EnvironmentAttributeSystem.addDefaultLayers(this, level);
         return this;
      }

      public Builder addConstantLayer(final EnvironmentAttributeMap attributeMap) {
         for(EnvironmentAttribute attribute : attributeMap.keySet()) {
            this.addConstantEntry(attribute, attributeMap);
         }

         return this;
      }

      private Builder addConstantEntry(final EnvironmentAttribute attribute, final EnvironmentAttributeMap attributeMap) {
         EnvironmentAttributeMap.Entry<Value, ?> entry = attributeMap.get(attribute);
         if (entry == null) {
            throw new IllegalArgumentException("Missing attribute " + String.valueOf(attribute));
         } else {
            Objects.requireNonNull(entry);
            return this.addConstantLayer(attribute, entry::applyModifier);
         }
      }

      public Builder addConstantLayer(final EnvironmentAttribute attribute, final EnvironmentAttributeLayer.Constant layer) {
         return this.addLayer(attribute, layer);
      }

      public Builder addTimeBasedLayer(final EnvironmentAttribute attribute, final EnvironmentAttributeLayer.TimeBased layer) {
         return this.addLayer(attribute, layer);
      }

      public Builder addPositionalLayer(final EnvironmentAttribute attribute, final EnvironmentAttributeLayer.Positional layer) {
         return this.addLayer(attribute, layer);
      }

      private Builder addLayer(final EnvironmentAttribute attribute, final EnvironmentAttributeLayer layer) {
         ((List)this.layersByAttribute.computeIfAbsent(attribute, (t) -> new ArrayList())).add(layer);
         return this;
      }

      public Builder addTimelineLayer(final Holder timeline, final ClockManager clockManager) {
         for(EnvironmentAttribute attribute : ((Timeline)timeline.value()).attributes()) {
            this.addTimelineLayerForAttribute(timeline, attribute, clockManager);
         }

         return this;
      }

      private void addTimelineLayerForAttribute(final Holder timeline, final EnvironmentAttribute attribute, final ClockManager clockManager) {
         this.addTimeBasedLayer(attribute, ((Timeline)timeline.value()).createTrackSampler(attribute, clockManager));
      }

      public EnvironmentAttributeSystem build() {
         return new EnvironmentAttributeSystem(this.layersByAttribute);
      }
   }

   private static class ValueSampler {
      private final EnvironmentAttribute attribute;
      private final Object baseValue;
      private final List layers;
      private final boolean isAffectedByPosition;
      private @Nullable Object cachedTickValue;
      private int cacheTickId;

      private ValueSampler(final EnvironmentAttribute attribute, final Object baseValue, final List layers, final boolean isAffectedByPosition) {
         this.attribute = attribute;
         this.baseValue = baseValue;
         this.layers = layers;
         this.isAffectedByPosition = isAffectedByPosition;
      }

      public void invalidateTickCache() {
         this.cachedTickValue = null;
         ++this.cacheTickId;
      }

      public Object getDimensionValue() {
         if (this.cachedTickValue != null) {
            return this.cachedTickValue;
         } else {
            Value result = (Value)this.computeValueNotPositional();
            this.cachedTickValue = result;
            return result;
         }
      }

      public Object getValue(final Vec3 pos, final @Nullable SpatialAttributeInterpolator biomeInterpolator) {
         return !this.isAffectedByPosition ? this.getDimensionValue() : this.computeValuePositional(pos, biomeInterpolator);
      }

      private Object computeValuePositional(final Vec3 pos, final @Nullable SpatialAttributeInterpolator biomeInterpolator) {
         Value result = (Value)this.baseValue;

         for(EnvironmentAttributeLayer layer : this.layers) {
            Objects.requireNonNull(layer);
            byte var7 = 0;
            Object var10000;
            //$FF: var7->value
            //0->net/minecraft/world/attribute/EnvironmentAttributeLayer$Constant
            //1->net/minecraft/world/attribute/EnvironmentAttributeLayer$TimeBased
            //2->net/minecraft/world/attribute/EnvironmentAttributeLayer$Positional
            switch (layer.typeSwitch<invokedynamic>(layer, var7)) {
               case 0:
                  EnvironmentAttributeLayer.Constant<Value> constantLayer = (EnvironmentAttributeLayer.Constant)layer;
                  var10000 = constantLayer.applyConstant(result);
                  break;
               case 1:
                  EnvironmentAttributeLayer.TimeBased<Value> timeBasedLayer = (EnvironmentAttributeLayer.TimeBased)layer;
                  var10000 = timeBasedLayer.applyTimeBased(result, this.cacheTickId);
                  break;
               case 2:
                  EnvironmentAttributeLayer.Positional<Value> positionalLayer = (EnvironmentAttributeLayer.Positional)layer;
                  var10000 = positionalLayer.applyPositional(result, (Vec3)Objects.requireNonNull(pos), biomeInterpolator);
                  break;
               default:
                  throw new MatchException((String)null, (Throwable)null);
            }

            result = (Value)var10000;
         }

         return this.attribute.sanitizeValue(result);
      }

      private Object computeValueNotPositional() {
         Value result = (Value)this.baseValue;

         for(EnvironmentAttributeLayer layer : this.layers) {
            Objects.requireNonNull(layer);
            byte var5 = 0;
            Object var10000;
            //$FF: var5->value
            //0->net/minecraft/world/attribute/EnvironmentAttributeLayer$Constant
            //1->net/minecraft/world/attribute/EnvironmentAttributeLayer$TimeBased
            //2->net/minecraft/world/attribute/EnvironmentAttributeLayer$Positional
            switch (layer.typeSwitch<invokedynamic>(layer, var5)) {
               case 0:
                  EnvironmentAttributeLayer.Constant<Value> constantLayer = (EnvironmentAttributeLayer.Constant)layer;
                  var10000 = constantLayer.applyConstant(result);
                  break;
               case 1:
                  EnvironmentAttributeLayer.TimeBased<Value> timeBasedLayer = (EnvironmentAttributeLayer.TimeBased)layer;
                  var10000 = timeBasedLayer.applyTimeBased(result, this.cacheTickId);
                  break;
               case 2:
                  EnvironmentAttributeLayer.Positional<Value> ignored = (EnvironmentAttributeLayer.Positional)layer;
                  var10000 = result;
                  break;
               default:
                  throw new MatchException((String)null, (Throwable)null);
            }

            result = (Value)var10000;
         }

         return this.attribute.sanitizeValue(result);
      }
   }
}
