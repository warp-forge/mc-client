package net.minecraft.world.item.crafting.display;

import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.Level;

public class SlotDisplayContext {
   public static final ContextKey FUEL_VALUES = ContextKey.vanilla("fuel_values");
   public static final ContextKey REGISTRIES = ContextKey.vanilla("registries");
   public static final ContextKeySet CONTEXT;

   public static ContextMap fromLevel(final Level level) {
      return (new ContextMap.Builder()).withParameter(FUEL_VALUES, level.fuelValues()).withParameter(REGISTRIES, level.registryAccess()).create(CONTEXT);
   }

   static {
      CONTEXT = (new ContextKeySet.Builder()).optional(FUEL_VALUES).optional(REGISTRIES).build();
   }
}
