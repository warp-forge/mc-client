package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.block.entity.FuelValues;

public interface SlotDisplay {
   Codec CODEC = BuiltInRegistries.SLOT_DISPLAY.byNameCodec().dispatch(SlotDisplay::type, Type::codec);
   StreamCodec STREAM_CODEC = ByteBufCodecs.registry(Registries.SLOT_DISPLAY).dispatch(SlotDisplay::type, Type::streamCodec);

   Stream resolve(ContextMap context, DisplayContentsFactory builder);

   Type type();

   default boolean isEnabled(final FeatureFlagSet enabledFeatures) {
      return true;
   }

   default List resolveForStacks(final ContextMap context) {
      return this.resolve(context, SlotDisplay.ItemStackContentsFactory.INSTANCE).toList();
   }

   default ItemStack resolveForFirstStack(final ContextMap context) {
      return (ItemStack)this.resolve(context, SlotDisplay.ItemStackContentsFactory.INSTANCE).findFirst().orElse(ItemStack.EMPTY);
   }

   private static Stream applyDemoTransformation(final ContextMap context, final DisplayContentsFactory factory, final SlotDisplay firstDisplay, final SlotDisplay secondDisplay, final RandomSource randomSource, final BinaryOperator operation) {
      if (factory instanceof DisplayContentsFactory.ForStacks stacks) {
         List<ItemStack> firstItems = firstDisplay.resolveForStacks(context);
         if (firstItems.isEmpty()) {
            return Stream.empty();
         } else {
            List<ItemStack> secondItems = secondDisplay.resolveForStacks(context);
            if (secondItems.isEmpty()) {
               return Stream.empty();
            } else {
               Stream var10000 = Stream.generate(() -> {
                  ItemStack first = (ItemStack)Util.getRandom(firstItems, randomSource);
                  ItemStack second = (ItemStack)Util.getRandom(secondItems, randomSource);
                  return (ItemStack)operation.apply(first, second);
               }).limit(256L).filter((s) -> !s.isEmpty()).limit(16L);
               Objects.requireNonNull(stacks);
               return var10000.map(stacks::forStack);
            }
         }
      } else {
         return Stream.empty();
      }
   }

   private static Stream applyDemoTransformation(final ContextMap context, final DisplayContentsFactory factory, final SlotDisplay firstDisplay, final SlotDisplay secondDisplay, final BinaryOperator operation) {
      if (factory instanceof DisplayContentsFactory.ForStacks stacks) {
         List<ItemStack> firstItems = firstDisplay.resolveForStacks(context);
         if (firstItems.isEmpty()) {
            return Stream.empty();
         } else {
            List<ItemStack> secondItems = secondDisplay.resolveForStacks(context);
            if (secondItems.isEmpty()) {
               return Stream.empty();
            } else {
               int cycle = firstItems.size() * secondItems.size();
               Stream var10000 = IntStream.range(0, cycle).mapToObj((index) -> {
                  int firstItemCount = firstItems.size();
                  int firstItemIndex = index % firstItemCount;
                  int secondItemIndex = index / firstItemCount;
                  ItemStack first = (ItemStack)firstItems.get(firstItemIndex);
                  ItemStack second = (ItemStack)secondItems.get(secondItemIndex);
                  return (ItemStack)operation.apply(first, second);
               }).filter((s) -> !s.isEmpty()).limit(16L);
               Objects.requireNonNull(stacks);
               return var10000.map(stacks::forStack);
            }
         }
      } else {
         return Stream.empty();
      }
   }

   public static record Type(MapCodec codec, StreamCodec streamCodec) {
   }

   public static class ItemStackContentsFactory implements DisplayContentsFactory.ForStacks {
      public static final ItemStackContentsFactory INSTANCE = new ItemStackContentsFactory();

      public ItemStack forStack(final ItemStack stack) {
         return stack;
      }
   }

   public static class Empty implements SlotDisplay {
      public static final Empty INSTANCE = new Empty();
      public static final MapCodec MAP_CODEC;
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      private Empty() {
      }

      public Type type() {
         return TYPE;
      }

      public String toString() {
         return "<empty>";
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         return Stream.empty();
      }

      static {
         MAP_CODEC = MapCodec.unit(INSTANCE);
         STREAM_CODEC = StreamCodec.unit(INSTANCE);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static class AnyFuel implements SlotDisplay {
      public static final AnyFuel INSTANCE = new AnyFuel();
      public static final MapCodec MAP_CODEC;
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      private AnyFuel() {
      }

      public Type type() {
         return TYPE;
      }

      public String toString() {
         return "<any fuel>";
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         if (factory instanceof DisplayContentsFactory.ForStacks stacks) {
            FuelValues fuelValues = (FuelValues)context.getOptional(SlotDisplayContext.FUEL_VALUES);
            if (fuelValues != null) {
               Stream var10000 = fuelValues.fuelItems().stream();
               Objects.requireNonNull(stacks);
               return var10000.map(stacks::forStack);
            }
         }

         return Stream.empty();
      }

      static {
         MAP_CODEC = MapCodec.unit(INSTANCE);
         STREAM_CODEC = StreamCodec.unit(INSTANCE);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record WithAnyPotion(SlotDisplay display) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(SlotDisplay.CODEC.fieldOf("contents").forGetter(WithAnyPotion::display)).apply(i, WithAnyPotion::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         if (factory instanceof DisplayContentsFactory.ForStacks stacks) {
            List<ItemStack> displayItems = this.display.resolveForStacks(context);
            Optional<? extends HolderLookup.RegistryLookup<Potion>> potions = Optional.ofNullable((HolderLookup.Provider)context.getOptional(SlotDisplayContext.REGISTRIES)).flatMap((r) -> r.lookup(Registries.POTION));
            return potions.stream().flatMap(HolderLookup::listElements).flatMap((potion) -> {
               PotionContents potionContents = new PotionContents(potion);
               return displayItems.stream().map((item) -> {
                  ItemStack itemCopy = item.copy();
                  itemCopy.set(DataComponents.POTION_CONTENTS, potionContents);
                  return stacks.forStack(itemCopy);
               });
            });
         } else {
            return Stream.empty();
         }
      }

      static {
         STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, WithAnyPotion::display, WithAnyPotion::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record OnlyWithComponent(SlotDisplay source, DataComponentType component) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(SlotDisplay.CODEC.fieldOf("contents").forGetter(OnlyWithComponent::source), DataComponentType.CODEC.fieldOf("component").forGetter(OnlyWithComponent::component)).apply(i, OnlyWithComponent::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Stream resolve(final ContextMap context, final DisplayContentsFactory builder) {
         if (builder instanceof DisplayContentsFactory.ForStacks stacks) {
            Stream var10000 = this.source.resolve(context, SlotDisplay.ItemStackContentsFactory.INSTANCE).filter((s) -> s.has(this.component));
            Objects.requireNonNull(stacks);
            return var10000.map(stacks::forStack);
         } else {
            return Stream.empty();
         }
      }

      public Type type() {
         return TYPE;
      }

      static {
         STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, OnlyWithComponent::source, DataComponentType.STREAM_CODEC, OnlyWithComponent::component, OnlyWithComponent::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record DyedSlotDemo(SlotDisplay dye, SlotDisplay target) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(SlotDisplay.CODEC.fieldOf("dye").forGetter(DyedSlotDemo::dye), SlotDisplay.CODEC.fieldOf("target").forGetter(DyedSlotDemo::target)).apply(i, DyedSlotDemo::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         BinaryOperator<ItemStack> tranformation = (target, dye) -> {
            DyeColor dyeValue = (DyeColor)dye.getOrDefault(DataComponents.DYE, DyeColor.WHITE);
            return DyedItemColor.applyDyes(target.copy(), List.of(dyeValue));
         };
         return SlotDisplay.applyDemoTransformation(context, factory, this.target, this.dye, tranformation);
      }

      static {
         STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, DyedSlotDemo::dye, SlotDisplay.STREAM_CODEC, DyedSlotDemo::target, DyedSlotDemo::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record SmithingTrimDemoSlotDisplay(SlotDisplay base, SlotDisplay material, Holder pattern) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(SlotDisplay.CODEC.fieldOf("base").forGetter(SmithingTrimDemoSlotDisplay::base), SlotDisplay.CODEC.fieldOf("material").forGetter(SmithingTrimDemoSlotDisplay::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(SmithingTrimDemoSlotDisplay::pattern)).apply(i, SmithingTrimDemoSlotDisplay::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         RandomSource randomSource = RandomSource.create((long)System.identityHashCode(this));
         BinaryOperator<ItemStack> tranformation = (base, material) -> SmithingTrimRecipe.applyTrim(base, material, this.pattern);
         return SlotDisplay.applyDemoTransformation(context, factory, this.base, this.material, randomSource, tranformation);
      }

      static {
         STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, SmithingTrimDemoSlotDisplay::base, SlotDisplay.STREAM_CODEC, SmithingTrimDemoSlotDisplay::material, TrimPattern.STREAM_CODEC, SmithingTrimDemoSlotDisplay::pattern, SmithingTrimDemoSlotDisplay::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record ItemSlotDisplay(Holder item) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(Item.CODEC.fieldOf("item").forGetter(ItemSlotDisplay::item)).apply(i, ItemSlotDisplay::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public ItemSlotDisplay(final Item item) {
         this((Holder)item.builtInRegistryHolder());
      }

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         if (factory instanceof DisplayContentsFactory.ForStacks stacks) {
            return Stream.of(stacks.forStack(this.item));
         } else {
            return Stream.empty();
         }
      }

      public boolean isEnabled(final FeatureFlagSet enabledFeatures) {
         return ((Item)this.item.value()).isEnabled(enabledFeatures);
      }

      static {
         STREAM_CODEC = StreamCodec.composite(Item.STREAM_CODEC, ItemSlotDisplay::item, ItemSlotDisplay::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record ItemStackSlotDisplay(ItemStackTemplate stack) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(ItemStackTemplate.CODEC.fieldOf("item").forGetter(ItemStackSlotDisplay::stack)).apply(i, ItemStackSlotDisplay::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         if (factory instanceof DisplayContentsFactory.ForStacks stacks) {
            return Stream.of(stacks.forStack(this.stack.create()));
         } else {
            return Stream.empty();
         }
      }

      public boolean isEnabled(final FeatureFlagSet enabledFeatures) {
         return ((Item)this.stack.item().value()).isEnabled(enabledFeatures);
      }

      static {
         STREAM_CODEC = StreamCodec.composite(ItemStackTemplate.STREAM_CODEC, ItemStackSlotDisplay::stack, ItemStackSlotDisplay::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record TagSlotDisplay(TagKey tag) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagSlotDisplay::tag)).apply(i, TagSlotDisplay::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         if (factory instanceof DisplayContentsFactory.ForStacks stacks) {
            HolderLookup.Provider registries = (HolderLookup.Provider)context.getOptional(SlotDisplayContext.REGISTRIES);
            if (registries != null) {
               return registries.lookupOrThrow(Registries.ITEM).get(this.tag).map((t) -> {
                  Stream var10000 = t.stream();
                  Objects.requireNonNull(stacks);
                  return var10000.map(stacks::forStack);
               }).stream().flatMap((s) -> s);
            }
         }

         return Stream.empty();
      }

      static {
         STREAM_CODEC = StreamCodec.composite(TagKey.streamCodec(Registries.ITEM), TagSlotDisplay::tag, TagSlotDisplay::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record Composite(List contents) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(SlotDisplay.CODEC.listOf().fieldOf("contents").forGetter(Composite::contents)).apply(i, Composite::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         return this.contents.stream().flatMap((d) -> d.resolve(context, factory));
      }

      public boolean isEnabled(final FeatureFlagSet enabledFeatures) {
         return this.contents.stream().allMatch((c) -> c.isEnabled(enabledFeatures));
      }

      static {
         STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), Composite::contents, Composite::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }

   public static record WithRemainder(SlotDisplay input, SlotDisplay remainder) implements SlotDisplay {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(SlotDisplay.CODEC.fieldOf("input").forGetter(WithRemainder::input), SlotDisplay.CODEC.fieldOf("remainder").forGetter(WithRemainder::remainder)).apply(i, WithRemainder::new));
      public static final StreamCodec STREAM_CODEC;
      public static final Type TYPE;

      public Type type() {
         return TYPE;
      }

      public Stream resolve(final ContextMap context, final DisplayContentsFactory factory) {
         if (factory instanceof DisplayContentsFactory.ForRemainders remainders) {
            List<T> resolvedRemainders = this.remainder.resolve(context, factory).toList();
            return this.input.resolve(context, factory).map((input) -> remainders.addRemainder(input, resolvedRemainders));
         } else {
            return this.input.resolve(context, factory);
         }
      }

      public boolean isEnabled(final FeatureFlagSet enabledFeatures) {
         return this.input.isEnabled(enabledFeatures) && this.remainder.isEnabled(enabledFeatures);
      }

      static {
         STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, WithRemainder::input, SlotDisplay.STREAM_CODEC, WithRemainder::remainder, WithRemainder::new);
         TYPE = new Type(MAP_CODEC, STREAM_CODEC);
      }
   }
}
