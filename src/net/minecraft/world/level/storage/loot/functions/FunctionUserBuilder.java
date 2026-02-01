package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.function.Function;

public interface FunctionUserBuilder {
   FunctionUserBuilder apply(LootItemFunction.Builder builder);

   default FunctionUserBuilder apply(final Iterable collection, final Function functionProvider) {
      T result = (T)this.unwrap();

      for(Object value : collection) {
         result = (T)result.apply((LootItemFunction.Builder)functionProvider.apply(value));
      }

      return result;
   }

   default FunctionUserBuilder apply(final Object[] collection, final Function functionProvider) {
      return this.apply((Iterable)Arrays.asList(collection), functionProvider);
   }

   FunctionUserBuilder unwrap();
}
