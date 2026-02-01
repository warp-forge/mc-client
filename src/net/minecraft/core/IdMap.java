package net.minecraft.core;

import org.jspecify.annotations.Nullable;

public interface IdMap extends Iterable {
   int DEFAULT = -1;

   int getId(Object thing);

   @Nullable Object byId(int id);

   default Object byIdOrThrow(final int id) {
      T result = (T)this.byId(id);
      if (result == null) {
         throw new IllegalArgumentException("No value with id " + id);
      } else {
         return result;
      }
   }

   default int getIdOrThrow(final Object value) {
      int id = this.getId(value);
      if (id == -1) {
         String var10002 = String.valueOf(value);
         throw new IllegalArgumentException("Can't find id for '" + var10002 + "' in map " + String.valueOf(this));
      } else {
         return id;
      }
   }

   int size();
}
