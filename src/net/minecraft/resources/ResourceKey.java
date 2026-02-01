package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;

public class ResourceKey {
   private static final ConcurrentMap VALUES = (new MapMaker()).weakValues().makeMap();
   private final Identifier registryName;
   private final Identifier identifier;

   public static Codec codec(final ResourceKey registryName) {
      return Identifier.CODEC.xmap((name) -> create(registryName, name), ResourceKey::identifier);
   }

   public static StreamCodec streamCodec(final ResourceKey registryName) {
      return Identifier.STREAM_CODEC.map((name) -> create(registryName, name), ResourceKey::identifier);
   }

   public static ResourceKey create(final ResourceKey registryName, final Identifier location) {
      return create(registryName.identifier, location);
   }

   public static ResourceKey createRegistryKey(final Identifier identifier) {
      return create(Registries.ROOT_REGISTRY_NAME, identifier);
   }

   private static ResourceKey create(final Identifier registryName, final Identifier identifier) {
      return (ResourceKey)VALUES.computeIfAbsent(new InternKey(registryName, identifier), (k) -> new ResourceKey(k.registry, k.identifier));
   }

   private ResourceKey(final Identifier registryName, final Identifier identifier) {
      this.registryName = registryName;
      this.identifier = identifier;
   }

   public String toString() {
      String var10000 = String.valueOf(this.registryName);
      return "ResourceKey[" + var10000 + " / " + String.valueOf(this.identifier) + "]";
   }

   public boolean isFor(final ResourceKey registry) {
      return this.registryName.equals(registry.identifier());
   }

   public Optional cast(final ResourceKey registry) {
      return this.isFor(registry) ? Optional.of(this) : Optional.empty();
   }

   public Identifier identifier() {
      return this.identifier;
   }

   public Identifier registry() {
      return this.registryName;
   }

   public ResourceKey registryKey() {
      return createRegistryKey(this.registryName);
   }

   private static record InternKey(Identifier registry, Identifier identifier) {
   }
}
