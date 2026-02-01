package net.minecraft.core;

public interface HolderOwner {
   default boolean canSerializeIn(final HolderOwner context) {
      return context == this;
   }
}
