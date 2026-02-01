package net.minecraft.world.item.crafting;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record SelectableRecipe(SlotDisplay optionDisplay, Optional recipe) {
   public static StreamCodec noRecipeCodec() {
      return StreamCodec.composite(SlotDisplay.STREAM_CODEC, SelectableRecipe::optionDisplay, (slotDisplay) -> new SelectableRecipe(slotDisplay, Optional.empty()));
   }

   public static record SingleInputEntry(Ingredient input, SelectableRecipe recipe) {
      public static StreamCodec noRecipeCodec() {
         return StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, SingleInputEntry::input, SelectableRecipe.noRecipeCodec(), SingleInputEntry::recipe, SingleInputEntry::new);
      }
   }

   public static record SingleInputSet(List entries) {
      public static SingleInputSet empty() {
         return new SingleInputSet(List.of());
      }

      public static StreamCodec noRecipeCodec() {
         return StreamCodec.composite(SelectableRecipe.SingleInputEntry.noRecipeCodec().apply(ByteBufCodecs.list()), SingleInputSet::entries, SingleInputSet::new);
      }

      public boolean acceptsInput(final ItemStack input) {
         return this.entries.stream().anyMatch((e) -> e.input.test(input));
      }

      public SingleInputSet selectByInput(final ItemStack input) {
         return new SingleInputSet(this.entries.stream().filter((e) -> e.input.test(input)).toList());
      }

      public boolean isEmpty() {
         return this.entries.isEmpty();
      }

      public int size() {
         return this.entries.size();
      }
   }
}
