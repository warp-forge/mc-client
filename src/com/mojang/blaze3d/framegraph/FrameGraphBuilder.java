package com.mojang.blaze3d.framegraph;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

public class FrameGraphBuilder {
   private final List internalResources = new ArrayList();
   private final List externalResources = new ArrayList();
   private final List passes = new ArrayList();

   public FramePass addPass(final String name) {
      Pass pass = new Pass(this.passes.size(), name);
      this.passes.add(pass);
      return pass;
   }

   public ResourceHandle importExternal(final String name, final Object resource) {
      ExternalResource<T> holder = new ExternalResource(name, (Pass)null, resource);
      this.externalResources.add(holder);
      return holder.handle;
   }

   public ResourceHandle createInternal(final String name, final ResourceDescriptor descriptor) {
      return this.createInternalResource(name, descriptor, (Pass)null).handle;
   }

   private InternalVirtualResource createInternalResource(final String name, final ResourceDescriptor descriptor, final Pass createdBy) {
      int id = this.internalResources.size();
      InternalVirtualResource<T> resource = new InternalVirtualResource(id, name, createdBy, descriptor);
      this.internalResources.add(resource);
      return resource;
   }

   public void execute(final GraphicsResourceAllocator resourceAllocator) {
      this.execute(resourceAllocator, FrameGraphBuilder.Inspector.NONE);
   }

   public void execute(final GraphicsResourceAllocator resourceAllocator, final Inspector inspector) {
      BitSet passesToKeep = this.identifyPassesToKeep();
      List<Pass> passesInOrder = new ArrayList(passesToKeep.cardinality());
      BitSet visiting = new BitSet(this.passes.size());

      for(Pass pass : this.passes) {
         this.resolvePassOrder(pass, passesToKeep, visiting, passesInOrder);
      }

      this.assignResourceLifetimes(passesInOrder);

      for(Pass pass : passesInOrder) {
         for(InternalVirtualResource resource : pass.resourcesToAcquire) {
            inspector.acquireResource(resource.name);
            resource.acquire(resourceAllocator);
         }

         inspector.beforeExecutePass(pass.name);
         pass.task.run();
         inspector.afterExecutePass(pass.name);

         for(int id = pass.resourcesToRelease.nextSetBit(0); id >= 0; id = pass.resourcesToRelease.nextSetBit(id + 1)) {
            InternalVirtualResource<?> resource = (InternalVirtualResource)this.internalResources.get(id);
            inspector.releaseResource(resource.name);
            resource.release(resourceAllocator);
         }
      }

   }

   private BitSet identifyPassesToKeep() {
      Deque<Pass> scratchQueue = new ArrayDeque(this.passes.size());
      BitSet passesToKeep = new BitSet(this.passes.size());

      for(VirtualResource resource : this.externalResources) {
         Pass pass = resource.handle.createdBy;
         if (pass != null) {
            this.discoverAllRequiredPasses(pass, passesToKeep, scratchQueue);
         }
      }

      for(Pass pass : this.passes) {
         if (pass.disableCulling) {
            this.discoverAllRequiredPasses(pass, passesToKeep, scratchQueue);
         }
      }

      return passesToKeep;
   }

   private void discoverAllRequiredPasses(final Pass sourcePass, final BitSet visited, final Deque passesToTrace) {
      passesToTrace.add(sourcePass);

      while(!passesToTrace.isEmpty()) {
         Pass pass = (Pass)passesToTrace.poll();
         if (!visited.get(pass.id)) {
            visited.set(pass.id);

            for(int id = pass.requiredPassIds.nextSetBit(0); id >= 0; id = pass.requiredPassIds.nextSetBit(id + 1)) {
               passesToTrace.add((Pass)this.passes.get(id));
            }
         }
      }

   }

   private void resolvePassOrder(final Pass pass, final BitSet passesToFind, final BitSet visiting, final List output) {
      if (visiting.get(pass.id)) {
         String involvedPasses = (String)visiting.stream().mapToObj((idx) -> ((Pass)this.passes.get(idx)).name).collect(Collectors.joining(", "));
         throw new IllegalStateException("Frame graph cycle detected between " + involvedPasses);
      } else if (passesToFind.get(pass.id)) {
         visiting.set(pass.id);
         passesToFind.clear(pass.id);

         for(int id = pass.requiredPassIds.nextSetBit(0); id >= 0; id = pass.requiredPassIds.nextSetBit(id + 1)) {
            this.resolvePassOrder((Pass)this.passes.get(id), passesToFind, visiting, output);
         }

         for(Handle handle : pass.writesFrom) {
            for(int id = handle.readBy.nextSetBit(0); id >= 0; id = handle.readBy.nextSetBit(id + 1)) {
               if (id != pass.id) {
                  this.resolvePassOrder((Pass)this.passes.get(id), passesToFind, visiting, output);
               }
            }
         }

         output.add(pass);
         visiting.clear(pass.id);
      }
   }

   private void assignResourceLifetimes(final Collection passesInOrder) {
      Pass[] lastPassByResource = new Pass[this.internalResources.size()];

      for(Pass pass : passesInOrder) {
         for(int id = pass.requiredResourceIds.nextSetBit(0); id >= 0; id = pass.requiredResourceIds.nextSetBit(id + 1)) {
            InternalVirtualResource<?> resource = (InternalVirtualResource)this.internalResources.get(id);
            Pass lastPass = lastPassByResource[id];
            lastPassByResource[id] = pass;
            if (lastPass == null) {
               pass.resourcesToAcquire.add(resource);
            } else {
               lastPass.resourcesToRelease.clear(id);
            }

            pass.resourcesToRelease.set(id);
         }
      }

   }

   private class Pass implements FramePass {
      private final int id;
      private final String name;
      private final List writesFrom;
      private final BitSet requiredResourceIds;
      private final BitSet requiredPassIds;
      private Runnable task;
      private final List resourcesToAcquire;
      private final BitSet resourcesToRelease;
      private boolean disableCulling;

      public Pass(final int id, final String name) {
         Objects.requireNonNull(FrameGraphBuilder.this);
         super();
         this.writesFrom = new ArrayList();
         this.requiredResourceIds = new BitSet();
         this.requiredPassIds = new BitSet();
         this.task = () -> {
         };
         this.resourcesToAcquire = new ArrayList();
         this.resourcesToRelease = new BitSet();
         this.id = id;
         this.name = name;
      }

      private void markResourceRequired(final Handle handle) {
         VirtualResource var3 = handle.holder;
         if (var3 instanceof InternalVirtualResource resource) {
            this.requiredResourceIds.set(resource.id);
         }

      }

      private void markPassRequired(final Pass pass) {
         this.requiredPassIds.set(pass.id);
      }

      public ResourceHandle createsInternal(final String name, final ResourceDescriptor descriptor) {
         InternalVirtualResource<T> resource = FrameGraphBuilder.this.createInternalResource(name, descriptor, this);
         this.requiredResourceIds.set(resource.id);
         return resource.handle;
      }

      public void reads(final ResourceHandle handle) {
         this._reads((Handle)handle);
      }

      private void _reads(final Handle handle) {
         this.markResourceRequired(handle);
         if (handle.createdBy != null) {
            this.markPassRequired(handle.createdBy);
         }

         handle.readBy.set(this.id);
      }

      public ResourceHandle readsAndWrites(final ResourceHandle handle) {
         return this._readsAndWrites((Handle)handle);
      }

      public void requires(final FramePass pass) {
         this.requiredPassIds.set(((Pass)pass).id);
      }

      public void disableCulling() {
         this.disableCulling = true;
      }

      private Handle _readsAndWrites(final Handle handle) {
         this.writesFrom.add(handle);
         this._reads(handle);
         return handle.writeAndAlias(this);
      }

      public void executes(final Runnable task) {
         this.task = task;
      }

      public String toString() {
         return this.name;
      }
   }

   private static class Handle implements ResourceHandle {
      private final VirtualResource holder;
      private final int version;
      private final Pass createdBy;
      private final BitSet readBy = new BitSet();
      private @Nullable Handle aliasedBy;

      private Handle(final VirtualResource holder, final int version, final Pass createdBy) {
         this.holder = holder;
         this.version = version;
         this.createdBy = createdBy;
      }

      public Object get() {
         return this.holder.get();
      }

      private Handle writeAndAlias(final Pass pass) {
         if (this.holder.handle != this) {
            String var10002 = String.valueOf(this);
            throw new IllegalStateException("Handle " + var10002 + " is no longer valid, as its contents were moved into " + String.valueOf(this.aliasedBy));
         } else {
            Handle<T> newHandle = new Handle(this.holder, this.version + 1, pass);
            this.holder.handle = newHandle;
            this.aliasedBy = newHandle;
            return newHandle;
         }
      }

      public String toString() {
         if (this.createdBy != null) {
            String var1 = String.valueOf(this.holder);
            return var1 + "#" + this.version + " (from " + String.valueOf(this.createdBy) + ")";
         } else {
            String var10000 = String.valueOf(this.holder);
            return var10000 + "#" + this.version;
         }
      }
   }

   private abstract static class VirtualResource {
      public final String name;
      public Handle handle;

      public VirtualResource(final String name, final Pass createdBy) {
         this.name = name;
         this.handle = new Handle(this, 0, createdBy);
      }

      public abstract Object get();

      public String toString() {
         return this.name;
      }
   }

   private static class InternalVirtualResource extends VirtualResource {
      private final int id;
      private final ResourceDescriptor descriptor;
      private @Nullable Object physicalResource;

      public InternalVirtualResource(final int id, final String name, final Pass createdBy, final ResourceDescriptor descriptor) {
         super(name, createdBy);
         this.id = id;
         this.descriptor = descriptor;
      }

      public Object get() {
         return Objects.requireNonNull(this.physicalResource, "Resource is not currently available");
      }

      public void acquire(final GraphicsResourceAllocator allocator) {
         if (this.physicalResource != null) {
            throw new IllegalStateException("Tried to acquire physical resource, but it was already assigned");
         } else {
            this.physicalResource = allocator.acquire(this.descriptor);
         }
      }

      public void release(final GraphicsResourceAllocator allocator) {
         if (this.physicalResource == null) {
            throw new IllegalStateException("Tried to release physical resource that was not allocated");
         } else {
            allocator.release(this.descriptor, this.physicalResource);
            this.physicalResource = null;
         }
      }
   }

   private static class ExternalResource extends VirtualResource {
      private final Object resource;

      public ExternalResource(final String name, final Pass createdBy, final Object resource) {
         super(name, createdBy);
         this.resource = resource;
      }

      public Object get() {
         return this.resource;
      }
   }

   public interface Inspector {
      Inspector NONE = new Inspector() {
      };

      default void acquireResource(final String name) {
      }

      default void releaseResource(final String name) {
      }

      default void beforeExecutePass(final String name) {
      }

      default void afterExecutePass(final String name) {
      }
   }
}
