package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps extends DelegatingOps {
   private final RegistryInfoLookup lookupProvider;

   public static RegistryOps create(final DynamicOps parent, final HolderLookup.Provider lookupProvider) {
      return create(parent, (RegistryInfoLookup)(new HolderLookupAdapter(lookupProvider)));
   }

   public static RegistryOps create(final DynamicOps parent, final RegistryInfoLookup lookupProvider) {
      return new RegistryOps(parent, lookupProvider);
   }

   public static Dynamic injectRegistryContext(final Dynamic dynamic, final HolderLookup.Provider lookupProvider) {
      return new Dynamic(lookupProvider.createSerializationContext(dynamic.getOps()), dynamic.getValue());
   }

   private RegistryOps(final DynamicOps parent, final RegistryInfoLookup lookupProvider) {
      super(parent);
      this.lookupProvider = lookupProvider;
   }

   public RegistryOps withParent(final DynamicOps parent) {
      return parent == this.delegate ? this : new RegistryOps(parent, this.lookupProvider);
   }

   public Optional owner(final ResourceKey registryKey) {
      return this.lookupProvider.lookup(registryKey).map(RegistryInfo::owner);
   }

   public Optional getter(final ResourceKey registryKey) {
      return this.lookupProvider.lookup(registryKey).map(RegistryInfo::getter);
   }

   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         RegistryOps<?> ops = (RegistryOps)obj;
         return this.delegate.equals(ops.delegate) && this.lookupProvider.equals(ops.lookupProvider);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.delegate.hashCode() * 31 + this.lookupProvider.hashCode();
   }

   public static RecordCodecBuilder retrieveGetter(final ResourceKey registryKey) {
      return ExtraCodecs.retrieveContext((ops) -> {
         if (ops instanceof RegistryOps registryOps) {
            return (DataResult)registryOps.lookupProvider.lookup(registryKey).map((r) -> DataResult.success(r.getter(), r.elementsLifecycle())).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + String.valueOf(registryKey)));
         } else {
            return DataResult.error(() -> "Not a registry ops");
         }
      }).forGetter((e) -> null);
   }

   public static RecordCodecBuilder retrieveElement(final ResourceKey key) {
      ResourceKey<? extends Registry<E>> registryKey = ResourceKey.createRegistryKey(key.registry());
      return ExtraCodecs.retrieveContext((ops) -> {
         if (ops instanceof RegistryOps registryOps) {
            return (DataResult)registryOps.lookupProvider.lookup(registryKey).flatMap((r) -> r.getter().get(key)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Can't find value: " + String.valueOf(key)));
         } else {
            return DataResult.error(() -> "Not a registry ops");
         }
      }).forGetter((e) -> null);
   }

   public static record RegistryInfo(HolderOwner owner, HolderGetter getter, Lifecycle elementsLifecycle) {
      public static RegistryInfo fromRegistryLookup(final HolderLookup.RegistryLookup registry) {
         return new RegistryInfo(registry, registry, registry.registryLifecycle());
      }
   }

   private static final class HolderLookupAdapter implements RegistryInfoLookup {
      private final HolderLookup.Provider lookupProvider;
      private final Map lookups = new ConcurrentHashMap();

      public HolderLookupAdapter(final HolderLookup.Provider lookupProvider) {
         this.lookupProvider = lookupProvider;
      }

      public Optional lookup(final ResourceKey registryKey) {
         return (Optional)this.lookups.computeIfAbsent(registryKey, this::createLookup);
      }

      private Optional createLookup(final ResourceKey key) {
         return this.lookupProvider.lookup(key).map(RegistryInfo::fromRegistryLookup);
      }

      public boolean equals(final Object obj) {
         if (this == obj) {
            return true;
         } else {
            boolean var10000;
            if (obj instanceof HolderLookupAdapter) {
               HolderLookupAdapter adapter = (HolderLookupAdapter)obj;
               if (this.lookupProvider.equals(adapter.lookupProvider)) {
                  var10000 = true;
                  return var10000;
               }
            }

            var10000 = false;
            return var10000;
         }
      }

      public int hashCode() {
         return this.lookupProvider.hashCode();
      }
   }

   public interface RegistryInfoLookup {
      Optional lookup(ResourceKey registryKey);
   }
}
