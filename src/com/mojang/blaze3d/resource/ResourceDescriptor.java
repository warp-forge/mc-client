package com.mojang.blaze3d.resource;

public interface ResourceDescriptor {
   Object allocate();

   default void prepare(final Object resource) {
   }

   void free(Object resource);

   default boolean canUsePhysicalResource(final ResourceDescriptor other) {
      return this.equals(other);
   }
}
