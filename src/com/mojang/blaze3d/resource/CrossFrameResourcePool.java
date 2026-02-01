package com.mojang.blaze3d.resource;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

public class CrossFrameResourcePool implements GraphicsResourceAllocator, AutoCloseable {
   private final int framesToKeepResource;
   private final Deque pool = new ArrayDeque();

   public CrossFrameResourcePool(final int framesToKeepResource) {
      this.framesToKeepResource = framesToKeepResource;
   }

   public void endFrame() {
      Iterator<? extends ResourceEntry<?>> iterator = this.pool.iterator();

      while(iterator.hasNext()) {
         ResourceEntry<?> entry = (ResourceEntry)iterator.next();
         if (entry.framesToLive-- == 0) {
            entry.close();
            iterator.remove();
         }
      }

   }

   public Object acquire(final ResourceDescriptor descriptor) {
      T resource = (T)this.acquireWithoutPreparing(descriptor);
      descriptor.prepare(resource);
      return resource;
   }

   private Object acquireWithoutPreparing(final ResourceDescriptor descriptor) {
      Iterator<? extends ResourceEntry<?>> iterator = this.pool.iterator();

      while(iterator.hasNext()) {
         ResourceEntry<?> entry = (ResourceEntry)iterator.next();
         if (descriptor.canUsePhysicalResource(entry.descriptor)) {
            iterator.remove();
            return entry.value;
         }
      }

      return descriptor.allocate();
   }

   public void release(final ResourceDescriptor descriptor, final Object resource) {
      this.pool.addFirst(new ResourceEntry(descriptor, resource, this.framesToKeepResource));
   }

   public void clear() {
      this.pool.forEach(ResourceEntry::close);
      this.pool.clear();
   }

   public void close() {
      this.clear();
   }

   @VisibleForTesting
   protected Collection entries() {
      return this.pool;
   }

   @VisibleForTesting
   protected static final class ResourceEntry implements AutoCloseable {
      private final ResourceDescriptor descriptor;
      private final Object value;
      private int framesToLive;

      private ResourceEntry(final ResourceDescriptor descriptor, final Object value, final int framesToLive) {
         this.descriptor = descriptor;
         this.value = value;
         this.framesToLive = framesToLive;
      }

      public void close() {
         this.descriptor.free(this.value);
      }
   }
}
