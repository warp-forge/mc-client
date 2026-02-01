package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class RegistrySetBuilder {
   private final List entries = new ArrayList();

   private static HolderGetter wrapContextLookup(final HolderLookup.RegistryLookup original) {
      return new EmptyTagLookup(original) {
         public Optional get(final ResourceKey id) {
            return original.get(id);
         }
      };
   }

   private static HolderLookup.RegistryLookup lookupFromMap(final ResourceKey key, final Lifecycle lifecycle, final HolderOwner owner, final Map entries) {
      return new EmptyTagRegistryLookup(owner) {
         public ResourceKey key() {
            return key;
         }

         public Lifecycle registryLifecycle() {
            return lifecycle;
         }

         public Optional get(final ResourceKey id) {
            return Optional.ofNullable((Holder.Reference)entries.get(id));
         }

         public Stream listElements() {
            return entries.values().stream();
         }
      };
   }

   public RegistrySetBuilder add(final ResourceKey key, final Lifecycle lifecycle, final RegistryBootstrap bootstrap) {
      this.entries.add(new RegistryStub(key, lifecycle, bootstrap));
      return this;
   }

   public RegistrySetBuilder add(final ResourceKey key, final RegistryBootstrap bootstrap) {
      return this.add(key, Lifecycle.stable(), bootstrap);
   }

   private BuildState createState(final RegistryAccess context) {
      BuildState state = RegistrySetBuilder.BuildState.create(context, this.entries.stream().map(RegistryStub::key));
      this.entries.forEach((e) -> e.apply(state));
      return state;
   }

   private static HolderLookup.Provider buildProviderWithContext(final UniversalOwner owner, final RegistryAccess context, final Stream newRegistries) {
      final Map<ResourceKey<? extends Registry<?>>, Entry<?>> lookups = new HashMap();
      context.registries().forEach((contextRegistry) -> lookups.put(contextRegistry.key(), Entry.createForContextRegistry(contextRegistry.value())));
      newRegistries.forEach((newRegistry) -> lookups.put(newRegistry.key(), Entry.createForNewRegistry(owner, newRegistry)));
      return new HolderLookup.Provider() {
         public Stream listRegistryKeys() {
            return lookups.keySet().stream();
         }

         private Optional getEntry(final ResourceKey key) {
            return Optional.ofNullable((Entry)lookups.get(key));
         }

         public Optional lookup(final ResourceKey key) {
            return this.getEntry(key).map(Entry::lookup);
         }

         public RegistryOps createSerializationContext(final DynamicOps parent) {
            return RegistryOps.create(parent, new RegistryOps.RegistryInfoLookup() {
               {
                  Objects.requireNonNull(<VAR_NAMELESS_ENCLOSURE>);
               }

               public Optional lookup(final ResourceKey registryKey) {
                  return getEntry(registryKey).map(Entry::opsInfo);
               }
            });
         }
      };

      record Entry(HolderLookup.RegistryLookup lookup, RegistryOps.RegistryInfo opsInfo) {
         public static Entry createForContextRegistry(final HolderLookup.RegistryLookup registryLookup) {
            return new Entry(new EmptyTagLookupWrapper(registryLookup, registryLookup), RegistryOps.RegistryInfo.fromRegistryLookup(registryLookup));
         }

         public static Entry createForNewRegistry(final UniversalOwner owner, final HolderLookup.RegistryLookup registryLookup) {
            return new Entry(new EmptyTagLookupWrapper(owner.cast(), registryLookup), new RegistryOps.RegistryInfo(owner.cast(), registryLookup, registryLookup.registryLifecycle()));
         }
      }

   }

   public HolderLookup.Provider build(final RegistryAccess context) {
      BuildState state = this.createState(context);
      Stream<HolderLookup.RegistryLookup<?>> newRegistries = this.entries.stream().map((stub) -> stub.collectRegisteredValues(state).buildAsLookup(state.owner));
      HolderLookup.Provider result = buildProviderWithContext(state.owner, context, newRegistries);
      state.reportNotCollectedHolders();
      state.reportUnclaimedRegisteredValues();
      state.throwOnError();
      return result;
   }

   private HolderLookup.Provider createLazyFullPatchedRegistries(final RegistryAccess context, final HolderLookup.Provider fallbackProvider, final Cloner.Factory clonerFactory, final Map newRegistries, final HolderLookup.Provider patchOnlyRegistries) {
      UniversalOwner fullPatchedOwner = new UniversalOwner();
      MutableObject<HolderLookup.Provider> resultReference = new MutableObject();
      List<HolderLookup.RegistryLookup<?>> lazyFullRegistries = (List)newRegistries.keySet().stream().map((registryKey) -> this.createLazyFullPatchedRegistries(fullPatchedOwner, clonerFactory, registryKey, patchOnlyRegistries, fallbackProvider, resultReference)).collect(Collectors.toUnmodifiableList());
      HolderLookup.Provider result = buildProviderWithContext(fullPatchedOwner, context, lazyFullRegistries.stream());
      resultReference.setValue(result);
      return result;
   }

   private HolderLookup.RegistryLookup createLazyFullPatchedRegistries(final HolderOwner owner, final Cloner.Factory clonerFactory, final ResourceKey registryKey, final HolderLookup.Provider patchProvider, final HolderLookup.Provider fallbackProvider, final MutableObject targetProvider) {
      Cloner<T> cloner = clonerFactory.cloner(registryKey);
      if (cloner == null) {
         throw new NullPointerException("No cloner for " + String.valueOf(registryKey.identifier()));
      } else {
         Map<ResourceKey<T>, Holder.Reference<T>> entries = new HashMap();
         HolderLookup.RegistryLookup<T> patchContents = patchProvider.lookupOrThrow(registryKey);
         patchContents.listElements().forEach((elementHolder) -> {
            ResourceKey<T> elementKey = elementHolder.key();
            LazyHolder<T> holder = new LazyHolder(owner, elementKey);
            holder.supplier = () -> cloner.clone(elementHolder.value(), patchProvider, (HolderLookup.Provider)targetProvider.get());
            entries.put(elementKey, holder);
         });
         HolderLookup.RegistryLookup<T> fallbackContents = fallbackProvider.lookupOrThrow(registryKey);
         fallbackContents.listElements().forEach((elementHolder) -> {
            ResourceKey<T> elementKey = elementHolder.key();
            entries.computeIfAbsent(elementKey, (key) -> {
               LazyHolder<T> holder = new LazyHolder(owner, elementKey);
               holder.supplier = () -> cloner.clone(elementHolder.value(), fallbackProvider, (HolderLookup.Provider)targetProvider.get());
               return holder;
            });
         });
         Lifecycle lifecycle = patchContents.registryLifecycle().add(fallbackContents.registryLifecycle());
         return lookupFromMap(registryKey, lifecycle, owner, entries);
      }
   }

   public PatchedRegistries buildPatch(final RegistryAccess context, final HolderLookup.Provider fallbackProvider, final Cloner.Factory clonerFactory) {
      BuildState state = this.createState(context);
      Map<ResourceKey<? extends Registry<?>>, RegistryContents<?>> newRegistries = new HashMap();
      this.entries.stream().map((stub) -> stub.collectRegisteredValues(state)).forEach((e) -> newRegistries.put(e.key, e));
      Set<ResourceKey<? extends Registry<?>>> contextRegistries = (Set)context.listRegistryKeys().collect(Collectors.toUnmodifiableSet());
      fallbackProvider.listRegistryKeys().filter((k) -> !contextRegistries.contains(k)).forEach((resourceKey) -> newRegistries.putIfAbsent(resourceKey, new RegistryContents(resourceKey, Lifecycle.stable(), Map.of())));
      Stream<HolderLookup.RegistryLookup<?>> dynamicRegistries = newRegistries.values().stream().map((registryContents) -> registryContents.buildAsLookup(state.owner));
      HolderLookup.Provider patchOnlyRegistries = buildProviderWithContext(state.owner, context, dynamicRegistries);
      state.reportUnclaimedRegisteredValues();
      state.throwOnError();
      HolderLookup.Provider fullPatchedRegistries = this.createLazyFullPatchedRegistries(context, fallbackProvider, clonerFactory, newRegistries, patchOnlyRegistries);
      return new PatchedRegistries(fullPatchedRegistries, patchOnlyRegistries);
   }

   private static class LazyHolder extends Holder.Reference {
      private @Nullable Supplier supplier;

      protected LazyHolder(final HolderOwner owner, final @Nullable ResourceKey key) {
         super(Holder.Reference.Type.STAND_ALONE, owner, key, (Object)null);
      }

      protected void bindValue(final Object value) {
         super.bindValue(value);
         this.supplier = null;
      }

      public Object value() {
         if (this.supplier != null) {
            this.bindValue(this.supplier.get());
         }

         return super.value();
      }
   }

   private abstract static class EmptyTagLookup implements HolderGetter {
      protected final HolderOwner owner;

      protected EmptyTagLookup(final HolderOwner owner) {
         this.owner = owner;
      }

      public Optional get(final TagKey id) {
         return Optional.of(HolderSet.emptyNamed(this.owner, id));
      }
   }

   private abstract static class EmptyTagRegistryLookup extends EmptyTagLookup implements HolderLookup.RegistryLookup {
      protected EmptyTagRegistryLookup(final HolderOwner owner) {
         super(owner);
      }

      public Stream listTags() {
         throw new UnsupportedOperationException("Tags are not available in datagen");
      }
   }

   private static class EmptyTagLookupWrapper extends EmptyTagRegistryLookup implements HolderLookup.RegistryLookup.Delegate {
      private final HolderLookup.RegistryLookup parent;

      private EmptyTagLookupWrapper(final HolderOwner owner, final HolderLookup.RegistryLookup parent) {
         super(owner);
         this.parent = parent;
      }

      public HolderLookup.RegistryLookup parent() {
         return this.parent;
      }
   }

   private static class UniversalOwner implements HolderOwner {
      public HolderOwner cast() {
         return this;
      }
   }

   private static class UniversalLookup extends EmptyTagLookup {
      private final Map holders = new HashMap();

      public UniversalLookup(final HolderOwner owner) {
         super(owner);
      }

      public Optional get(final ResourceKey id) {
         return Optional.of(this.getOrCreate(id));
      }

      private Holder.Reference getOrCreate(final ResourceKey id) {
         return (Holder.Reference)this.holders.computeIfAbsent(id, (k) -> Holder.Reference.createStandAlone(this.owner, k));
      }
   }

   private static record RegisteredValue(Object value, Lifecycle lifecycle) {
   }

   private static record BuildState(UniversalOwner owner, UniversalLookup lookup, Map registries, Map registeredValues, List errors) {
      public static BuildState create(final RegistryAccess context, final Stream newRegistries) {
         UniversalOwner owner = new UniversalOwner();
         List<RuntimeException> errors = new ArrayList();
         UniversalLookup lookup = new UniversalLookup(owner);
         ImmutableMap.Builder<Identifier, HolderGetter<?>> registries = ImmutableMap.builder();
         context.registries().forEach((contextRegistry) -> registries.put(contextRegistry.key().identifier(), RegistrySetBuilder.wrapContextLookup(contextRegistry.value())));
         newRegistries.forEach((newRegistry) -> registries.put(newRegistry.identifier(), lookup));
         return new BuildState(owner, lookup, registries.build(), new HashMap(), errors);
      }

      public BootstrapContext bootstrapContext() {
         return new BootstrapContext() {
            {
               Objects.requireNonNull(BuildState.this);
            }

            public Holder.Reference register(final ResourceKey key, final Object value, final Lifecycle lifecycle) {
               RegisteredValue<?> previousValue = (RegisteredValue)BuildState.this.registeredValues.put(key, new RegisteredValue(value, lifecycle));
               if (previousValue != null) {
                  List var10000 = BuildState.this.errors;
                  String var10003 = String.valueOf(key);
                  var10000.add(new IllegalStateException("Duplicate registration for " + var10003 + ", new=" + String.valueOf(value) + ", old=" + String.valueOf(previousValue.value)));
               }

               return BuildState.this.lookup.getOrCreate(key);
            }

            public HolderGetter lookup(final ResourceKey key) {
               return (HolderGetter)BuildState.this.registries.getOrDefault(key.identifier(), BuildState.this.lookup);
            }
         };
      }

      public void reportUnclaimedRegisteredValues() {
         this.registeredValues.forEach((key, registeredValue) -> {
            List var10000 = this.errors;
            String var10003 = String.valueOf(registeredValue.value);
            var10000.add(new IllegalStateException("Orpaned value " + var10003 + " for key " + String.valueOf(key)));
         });
      }

      public void reportNotCollectedHolders() {
         for(ResourceKey key : this.lookup.holders.keySet()) {
            this.errors.add(new IllegalStateException("Unreferenced key: " + String.valueOf(key)));
         }

      }

      public void throwOnError() {
         if (!this.errors.isEmpty()) {
            IllegalStateException result = new IllegalStateException("Errors during registry creation");

            for(RuntimeException error : this.errors) {
               result.addSuppressed(error);
            }

            throw result;
         }
      }
   }

   private static record ValueAndHolder(RegisteredValue value, Optional holder) {
   }

   private static record RegistryStub(ResourceKey key, Lifecycle lifecycle, RegistryBootstrap bootstrap) {
      private void apply(final BuildState state) {
         this.bootstrap.run(state.bootstrapContext());
      }

      public RegistryContents collectRegisteredValues(final BuildState state) {
         Map<ResourceKey<T>, ValueAndHolder<T>> result = new HashMap();
         Iterator<Map.Entry<ResourceKey<?>, RegisteredValue<?>>> iterator = state.registeredValues.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<ResourceKey<?>, RegisteredValue<?>> entry = (Map.Entry)iterator.next();
            ResourceKey<?> key = (ResourceKey)entry.getKey();
            if (key.isFor(this.key)) {
               RegisteredValue<T> value = (RegisteredValue)entry.getValue();
               Holder.Reference<T> holder = (Holder.Reference)state.lookup.holders.remove(key);
               result.put(key, new ValueAndHolder(value, Optional.ofNullable(holder)));
               iterator.remove();
            }
         }

         return new RegistryContents(this.key, this.lifecycle, result);
      }
   }

   private static record RegistryContents(ResourceKey key, Lifecycle lifecycle, Map values) {
      public HolderLookup.RegistryLookup buildAsLookup(final UniversalOwner owner) {
         Map<ResourceKey<T>, Holder.Reference<T>> entries = (Map)this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, (e) -> {
            ValueAndHolder<T> entry = (ValueAndHolder)e.getValue();
            Holder.Reference<T> holder = (Holder.Reference)entry.holder().orElseGet(() -> Holder.Reference.createStandAlone(owner.cast(), (ResourceKey)e.getKey()));
            holder.bindValue(entry.value().value());
            return holder;
         }));
         return RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, owner.cast(), entries);
      }
   }

   public static record PatchedRegistries(HolderLookup.Provider full, HolderLookup.Provider patches) {
   }

   @FunctionalInterface
   public interface RegistryBootstrap {
      void run(BootstrapContext registry);
   }
}
