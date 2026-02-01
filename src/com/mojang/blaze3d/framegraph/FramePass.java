package com.mojang.blaze3d.framegraph;

import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;

public interface FramePass {
   ResourceHandle createsInternal(String name, ResourceDescriptor descriptor);

   void reads(ResourceHandle handle);

   ResourceHandle readsAndWrites(ResourceHandle handle);

   void requires(FramePass pass);

   void disableCulling();

   void executes(Runnable task);
}
