package net.minecraft.core.component;

import org.jspecify.annotations.Nullable;

public interface DataComponentGetter {
   @Nullable Object get(DataComponentType type);

   default Object getOrDefault(final DataComponentType type, final Object defaultValue) {
      T value = (T)this.get(type);
      return value != null ? value : defaultValue;
   }

   default @Nullable TypedDataComponent getTyped(final DataComponentType type) {
      T value = (T)this.get(type);
      return value != null ? new TypedDataComponent(type, value) : null;
   }
}
