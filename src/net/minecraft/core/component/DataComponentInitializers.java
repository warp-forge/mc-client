package net.minecraft.core.component;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class DataComponentInitializers {
   private final List initializers = new ArrayList();

   public void add(final ResourceKey key, final Initializer initializer) {
      this.initializers.add(new InitializerEntry(key, initializer));
   }

   private Map runInitializers(final HolderLookup.Provider context) {
      Map<ResourceKey<?>, DataComponentMap.Builder> results = new HashMap();

      for(InitializerEntry initializer : this.initializers) {
         DataComponentMap.Builder builder = (DataComponentMap.Builder)results.computeIfAbsent(initializer.key, (k) -> DataComponentMap.builder());
         initializer.run(builder, context);
      }

      return results;
   }

   private static void registryEmpty(final Map buildersByRegistry, final ResourceKey registryKey) {
      buildersByRegistry.put(registryKey, new PendingComponentBuilders(registryKey, new HashMap()));
   }

   private static void addBuilder(final Map buildersByRegistry, final ResourceKey key, final DataComponentMap.Builder builder) {
      PendingComponentBuilders<T> buildersForRegistry = (PendingComponentBuilders)buildersByRegistry.get(key.registryKey());
      buildersForRegistry.builders.put(key, builder);
   }

   public List build(final HolderLookup.Provider context) {
      Map<ResourceKey<? extends Registry<?>>, PendingComponentBuilders<?>> buildersByRegistry = new HashMap();
      context.listRegistryKeys().forEach((registryKey) -> registryEmpty(buildersByRegistry, registryKey));
      this.runInitializers(context).forEach((key, builder) -> addBuilder(buildersByRegistry, key, builder));
      return (List)buildersByRegistry.values().stream().map((elementBuilders) -> createInitializerForRegistry(context, elementBuilders)).collect(Collectors.toUnmodifiableList());
   }

   private static PendingComponents createInitializerForRegistry(final HolderLookup.Provider context, final PendingComponentBuilders elementBuilders) {
      final List<BakedEntry<T>> entries = new ArrayList();
      final ResourceKey<? extends Registry<T>> registryKey = elementBuilders.registryKey;
      HolderLookup.RegistryLookup<T> registry = context.lookupOrThrow(registryKey);
      Set<Holder.Reference<T>> elementsWithComponents = Sets.newIdentityHashSet();
      elementBuilders.builders.forEach((elementKey, elementBuilder) -> {
         Holder.Reference<T> element = registry.getOrThrow(elementKey);
         DataComponentMap components = elementBuilder.build();
         entries.add(new BakedEntry(element, components));
         elementsWithComponents.add(element);
      });
      registry.listElements().filter((e) -> !elementsWithComponents.contains(e)).forEach((elementWithoutComponents) -> entries.add(new BakedEntry(elementWithoutComponents, DataComponentMap.EMPTY)));
      return new PendingComponents() {
         public ResourceKey key() {
            return registryKey;
         }

         public void forEach(final BiConsumer output) {
            entries.forEach((e) -> output.accept(e.element, e.components));
         }

         public void apply() {
            entries.forEach(BakedEntry::apply);
         }
      };
   }

   private static record InitializerEntry(ResourceKey key, Initializer initializer) {
      public void run(final DataComponentMap.Builder components, final HolderLookup.Provider context) {
         this.initializer.run(components, context, this.key);
      }
   }

   private static record BakedEntry(Holder.Reference element, DataComponentMap components) {
      public void apply() {
         this.element.bindComponents(this.components);
      }
   }

   @FunctionalInterface
   public interface Initializer {
      void run(DataComponentMap.Builder components, HolderLookup.Provider context, ResourceKey key);

      default Initializer andThen(final Initializer other) {
         return (components, context, key) -> {
            this.run(components, context, key);
            other.run(components, context, key);
         };
      }

      default Initializer add(final DataComponentType type, final Object value) {
         return this.andThen((components, context, key) -> components.set(type, value));
      }
   }

   @FunctionalInterface
   public interface SingleComponentInitializer {
      Object create(HolderLookup.Provider context);

      default Initializer asInitializer(final DataComponentType type) {
         return (components, context, key) -> components.set(type, this.create(context));
      }
   }

   private static record PendingComponentBuilders(ResourceKey registryKey, Map builders) {
   }

   public interface PendingComponents {
      ResourceKey key();

      void forEach(BiConsumer output);

      void apply();
   }
}
