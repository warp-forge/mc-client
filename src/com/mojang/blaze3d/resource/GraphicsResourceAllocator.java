package com.mojang.blaze3d.resource;

public interface GraphicsResourceAllocator {
   GraphicsResourceAllocator UNPOOLED = new GraphicsResourceAllocator() {
      public Object acquire(final ResourceDescriptor descriptor) {
         T resource = (T)descriptor.allocate();
         descriptor.prepare(resource);
         return resource;
      }

      public void release(final ResourceDescriptor descriptor, final Object resource) {
         descriptor.free(resource);
      }
   };

   Object acquire(ResourceDescriptor descriptor);

   void release(ResourceDescriptor descriptor, Object resource);
}
