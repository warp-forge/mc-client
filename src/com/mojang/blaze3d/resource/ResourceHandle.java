package com.mojang.blaze3d.resource;

public interface ResourceHandle {
   ResourceHandle INVALID_HANDLE = () -> {
      throw new IllegalStateException("Cannot dereference handle with no underlying resource");
   };

   static ResourceHandle invalid() {
      return INVALID_HANDLE;
   }

   Object get();
}
