package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record LootDataType(ResourceKey registryKey, Codec codec, ContextGetter contextGetter) {
   public static final LootDataType PREDICATE;
   public static final LootDataType MODIFIER;
   public static final LootDataType TABLE;

   public void runValidation(final ValidationContextSource contextSource, final ResourceKey key, final Validatable value) {
      ContextKeySet contextKeys = this.contextGetter.context(value);
      ValidationContext rootContext = contextSource.context(contextKeys).enterElement(new ProblemReporter.RootElementPathElement(key), key);
      value.validate(rootContext);
   }

   public void runValidation(final ValidationContextSource contextSource, final HolderLookup lookup) {
      lookup.listElements().forEach((holder) -> this.runValidation(contextSource, holder.key(), (Validatable)holder.value()));
   }

   public static Stream values() {
      return Stream.of(PREDICATE, MODIFIER, TABLE);
   }

   static {
      PREDICATE = new LootDataType(Registries.PREDICATE, LootItemCondition.DIRECT_CODEC, LootDataType.ContextGetter.constant(LootContextParamSets.ALL_PARAMS));
      MODIFIER = new LootDataType(Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC, LootDataType.ContextGetter.constant(LootContextParamSets.ALL_PARAMS));
      TABLE = new LootDataType(Registries.LOOT_TABLE, LootTable.DIRECT_CODEC, LootTable::getParamSet);
   }

   @FunctionalInterface
   public interface ContextGetter {
      ContextKeySet context(Object value);

      static ContextGetter constant(final ContextKeySet v) {
         return (value) -> v;
      }
   }
}
