package net.minecraft.data.loot;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface LootTableSubProvider {
   void generate(BiConsumer output);
}
