package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.util.Util;

public class AllOfCondition extends CompositeLootItemCondition {
   public static final MapCodec MAP_CODEC = createCodec(AllOfCondition::new);
   public static final Codec INLINE_CODEC = createInlineCodec(AllOfCondition::new);

   private AllOfCondition(final List terms) {
      super(terms, Util.allOf(terms));
   }

   public static AllOfCondition allOf(final List terms) {
      return new AllOfCondition(List.copyOf(terms));
   }

   public MapCodec codec() {
      return MAP_CODEC;
   }

   public static Builder allOf(final LootItemCondition.Builder... terms) {
      return new Builder(terms);
   }

   public static class Builder extends CompositeLootItemCondition.Builder {
      public Builder(final LootItemCondition.Builder... terms) {
         super(terms);
      }

      public Builder and(final LootItemCondition.Builder term) {
         this.addTerm(term);
         return this;
      }

      protected LootItemCondition create(final List terms) {
         return new AllOfCondition(terms);
      }
   }
}
