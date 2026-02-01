package net.minecraft.gametest.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public abstract class TestFunctionLoader {
   private static final List loaders = new ArrayList();

   public static void registerLoader(final TestFunctionLoader loader) {
      loaders.add(loader);
   }

   public static void runLoaders(final Registry registry) {
      for(TestFunctionLoader loader : loaders) {
         loader.load((key, function) -> Registry.register(registry, (ResourceKey)key, function));
      }

   }

   public abstract void load(BiConsumer register);
}
