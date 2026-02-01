package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess extends HolderLookup.Provider {
   Logger LOGGER = LogUtils.getLogger();
   Frozen EMPTY = (new ImmutableRegistryAccess(Map.of())).freeze();

   Optional lookup(final ResourceKey registryKey);

   default Registry lookupOrThrow(final ResourceKey name) {
      return (Registry)this.lookup(name).orElseThrow(() -> new IllegalStateException("Missing registry: " + String.valueOf(name)));
   }

   Stream registries();

   default Stream listRegistryKeys() {
      return this.registries().map((e) -> e.key);
   }

   static Frozen fromRegistryOfRegistries(final Registry registries) {
      return new Frozen() {
         public Optional lookup(final ResourceKey registryKey) {
            Registry<Registry<T>> registry = registries;
            return registry.getOptional(registryKey);
         }

         public Stream registries() {
            return registries.entrySet().stream().map(RegistryEntry::fromMapEntry);
         }

         public Frozen freeze() {
            return this;
         }
      };
   }

   default Frozen freeze() {
      class FrozenAccess extends ImmutableRegistryAccess implements Frozen {
         protected FrozenAccess(final Stream entries) {
            Objects.requireNonNull(RegistryAccess.this);
            super(entries);
         }
      }

      return new FrozenAccess(this.registries().map(RegistryEntry::freeze));
   }

   public static record RegistryEntry(ResourceKey key, Registry value) {
      private static RegistryEntry fromMapEntry(final Map.Entry e) {
         return fromUntyped((ResourceKey)e.getKey(), (Registry)e.getValue());
      }

      private static RegistryEntry fromUntyped(final ResourceKey key, final Registry value) {
         return new RegistryEntry(key, value);
      }

      private RegistryEntry freeze() {
         return new RegistryEntry(this.key, this.value.freeze());
      }
   }

   public static class ImmutableRegistryAccess implements RegistryAccess {
      private final Map registries;

      public ImmutableRegistryAccess(final List registries) {
         this.registries = (Map)registries.stream().collect(Collectors.toUnmodifiableMap(Registry::key, (v) -> v));
      }

      public ImmutableRegistryAccess(final Map registries) {
         this.registries = Map.copyOf(registries);
      }

      public ImmutableRegistryAccess(final Stream entries) {
         this.registries = (Map)entries.collect(ImmutableMap.toImmutableMap(RegistryEntry::key, RegistryEntry::value));
      }

      public Optional lookup(final ResourceKey registryKey) {
         return Optional.ofNullable((Registry)this.registries.get(registryKey)).map((r) -> r);
      }

      public Stream registries() {
         return this.registries.entrySet().stream().map(RegistryEntry::fromMapEntry);
      }
   }

   public interface Frozen extends RegistryAccess {
   }
}
