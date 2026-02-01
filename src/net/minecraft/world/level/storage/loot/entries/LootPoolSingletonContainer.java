package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
   public static final int DEFAULT_WEIGHT = 1;
   public static final int DEFAULT_QUALITY = 0;
   protected final int weight;
   protected final int quality;
   protected final List functions;
   private final BiFunction compositeFunction;
   private final LootPoolEntry entry = new EntryBase() {
      {
         Objects.requireNonNull(LootPoolSingletonContainer.this);
      }

      public void createItemStack(final Consumer output, final LootContext context) {
         LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, output, context), context);
      }
   };

   protected LootPoolSingletonContainer(final int weight, final int quality, final List conditions, final List functions) {
      super(conditions);
      this.weight = weight;
      this.quality = quality;
      this.functions = functions;
      this.compositeFunction = LootItemFunctions.compose(functions);
   }

   public abstract MapCodec codec();

   protected static Products.P4 singletonFields(final RecordCodecBuilder.Instance i) {
      return i.group(Codec.INT.optionalFieldOf("weight", 1).forGetter((e) -> e.weight), Codec.INT.optionalFieldOf("quality", 0).forGetter((e) -> e.quality)).and(commonFields(i).t1()).and(LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter((e) -> e.functions));
   }

   public void validate(final ValidationContext context) {
      super.validate(context);
      Validatable.validate(context, "functions", this.functions);
   }

   protected abstract void createItemStack(Consumer output, LootContext context);

   public boolean expand(final LootContext context, final Consumer output) {
      if (this.canRun(context)) {
         output.accept(this.entry);
         return true;
      } else {
         return false;
      }
   }

   public static Builder simpleBuilder(final EntryConstructor constructor) {
      return new DummyBuilder(constructor);
   }

   protected abstract class EntryBase implements LootPoolEntry {
      protected EntryBase() {
         Objects.requireNonNull(LootPoolSingletonContainer.this);
         super();
      }

      public int getWeight(final float luck) {
         return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * luck), 0);
      }
   }

   public abstract static class Builder extends LootPoolEntryContainer.Builder implements FunctionUserBuilder {
      protected int weight = 1;
      protected int quality = 0;
      private final ImmutableList.Builder functions = ImmutableList.builder();

      public Builder apply(final LootItemFunction.Builder function) {
         this.functions.add(function.build());
         return (Builder)this.getThis();
      }

      protected List getFunctions() {
         return this.functions.build();
      }

      public Builder setWeight(final int weight) {
         this.weight = weight;
         return (Builder)this.getThis();
      }

      public Builder setQuality(final int quality) {
         this.quality = quality;
         return (Builder)this.getThis();
      }
   }

   private static class DummyBuilder extends Builder {
      private final EntryConstructor constructor;

      public DummyBuilder(final EntryConstructor constructor) {
         this.constructor = constructor;
      }

      protected DummyBuilder getThis() {
         return this;
      }

      public LootPoolEntryContainer build() {
         return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
      }
   }

   @FunctionalInterface
   protected interface EntryConstructor {
      LootPoolSingletonContainer build(int weight, int quality, List conditions, List functions);
   }
}
