package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;

public interface ConditionUserBuilder {
   ConditionUserBuilder when(final LootItemCondition.Builder builder);

   default ConditionUserBuilder when(final Iterable collection, final Function conditionProvider) {
      T result = (T)this.unwrap();

      for(Object value : collection) {
         result = (T)result.when((LootItemCondition.Builder)conditionProvider.apply(value));
      }

      return result;
   }

   ConditionUserBuilder unwrap();
}
