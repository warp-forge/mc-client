package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class RecipeMap {
   public static final RecipeMap EMPTY = new RecipeMap(ImmutableMultimap.of(), Map.of());
   private final Multimap byType;
   private final Map byKey;

   private RecipeMap(final Multimap byType, final Map byKey) {
      this.byType = byType;
      this.byKey = byKey;
   }

   public static RecipeMap create(final Iterable recipes) {
      ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
      ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = ImmutableMap.builder();

      for(RecipeHolder recipe : recipes) {
         byType.put(recipe.value().getType(), recipe);
         byKey.put(recipe.id(), recipe);
      }

      return new RecipeMap(byType.build(), byKey.build());
   }

   public Collection byType(final RecipeType type) {
      return this.byType.get(type);
   }

   public Collection values() {
      return this.byKey.values();
   }

   public @Nullable RecipeHolder byKey(final ResourceKey recipeId) {
      return (RecipeHolder)this.byKey.get(recipeId);
   }

   public Stream getRecipesFor(final RecipeType type, final RecipeInput container, final Level level) {
      return container.isEmpty() ? Stream.empty() : this.byType(type).stream().filter((r) -> r.value().matches(container, level));
   }
}
