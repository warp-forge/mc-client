package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public interface EnchantmentProviderTypes {
   static MapCodec bootstrap(final Registry registry) {
      Registry.register(registry, (String)"by_cost", EnchantmentsByCost.CODEC);
      Registry.register(registry, (String)"by_cost_with_difficulty", EnchantmentsByCostWithDifficulty.CODEC);
      return (MapCodec)Registry.register(registry, (String)"single", SingleEnchantment.CODEC);
   }
}
