package net.minecraft.data.worldgen;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;

public interface BootstrapContext {
   Holder.Reference register(ResourceKey key, Object value, Lifecycle lifecycle);

   default Holder.Reference register(final ResourceKey key, final Object value) {
      return this.register(key, value, Lifecycle.stable());
   }

   HolderGetter lookup(ResourceKey key);
}
