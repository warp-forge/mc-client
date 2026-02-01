package net.minecraft.references;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class Items {
   public static final ResourceKey PUMPKIN_SEEDS = createKey("pumpkin_seeds");
   public static final ResourceKey MELON_SEEDS = createKey("melon_seeds");

   private static ResourceKey createKey(final String name) {
      return ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace(name));
   }
}
