package net.minecraft.references;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class Blocks {
   public static final ResourceKey PUMPKIN = createKey("pumpkin");
   public static final ResourceKey PUMPKIN_STEM = createKey("pumpkin_stem");
   public static final ResourceKey ATTACHED_PUMPKIN_STEM = createKey("attached_pumpkin_stem");
   public static final ResourceKey MELON = createKey("melon");
   public static final ResourceKey MELON_STEM = createKey("melon_stem");
   public static final ResourceKey ATTACHED_MELON_STEM = createKey("attached_melon_stem");

   private static ResourceKey createKey(final String name) {
      return ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(name));
   }
}
