package net.minecraft.world.level.storage.loot;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotCollection;

public interface ContainerComponentManipulator {
   DataComponentType type();

   Object empty();

   Object setContents(Object component, Stream newContents);

   Stream getContents(Object component);

   default void setContents(final ItemStack itemStack, final Object defaultValue, final Stream newContents) {
      T currentValue = (T)itemStack.getOrDefault(this.type(), defaultValue);
      T newValue = (T)this.setContents(currentValue, newContents);
      itemStack.set(this.type(), newValue);
   }

   default void setContents(final ItemStack itemStack, final Stream newContents) {
      this.setContents(itemStack, this.empty(), newContents);
   }

   default void modifyItems(final ItemStack itemStack, final UnaryOperator modifier) {
      T contents = (T)itemStack.get(this.type());
      if (contents != null) {
         UnaryOperator<ItemStack> nonEmptyModifier = (currentItemStack) -> {
            if (currentItemStack.isEmpty()) {
               return currentItemStack;
            } else {
               ItemStack newItemStack = (ItemStack)modifier.apply(currentItemStack);
               newItemStack.limitSize(newItemStack.getMaxStackSize());
               return newItemStack;
            }
         };
         this.setContents(itemStack, this.getContents(contents).map(nonEmptyModifier));
      }

   }

   default SlotCollection getSlots(final ItemStack itemStack) {
      return () -> {
         T contents = (T)itemStack.get(this.type());
         return contents != null ? this.getContents(contents).filter((stack) -> !stack.isEmpty()) : Stream.empty();
      };
   }
}
