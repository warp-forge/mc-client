package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootItemConditionalFunction implements LootItemFunction {
   protected final List predicates;
   private final Predicate compositePredicates;

   protected LootItemConditionalFunction(final List predicates) {
      this.predicates = predicates;
      this.compositePredicates = Util.allOf(predicates);
   }

   public abstract MapCodec codec();

   protected static Products.P1 commonFields(final RecordCodecBuilder.Instance i) {
      return i.group(LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter((f) -> f.predicates));
   }

   public final ItemStack apply(final ItemStack itemStack, final LootContext context) {
      return this.compositePredicates.test(context) ? this.run(itemStack, context) : itemStack;
   }

   protected abstract ItemStack run(final ItemStack itemStack, final LootContext context);

   public void validate(final ValidationContext context) {
      LootItemFunction.super.validate(context);
      Validatable.validate(context, "conditions", this.predicates);
   }

   protected static Builder simpleBuilder(final Function constructor) {
      return new DummyBuilder(constructor);
   }

   public abstract static class Builder implements LootItemFunction.Builder, ConditionUserBuilder {
      private final ImmutableList.Builder conditions = ImmutableList.builder();

      public Builder when(final LootItemCondition.Builder condition) {
         this.conditions.add(condition.build());
         return this.getThis();
      }

      public final Builder unwrap() {
         return this.getThis();
      }

      protected abstract Builder getThis();

      protected List getConditions() {
         return this.conditions.build();
      }
   }

   private static final class DummyBuilder extends Builder {
      private final Function constructor;

      public DummyBuilder(final Function constructor) {
         this.constructor = constructor;
      }

      protected DummyBuilder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return (LootItemFunction)this.constructor.apply(this.getConditions());
      }
   }
}
