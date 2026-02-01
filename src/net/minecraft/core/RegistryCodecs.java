package net.minecraft.core;

import com.mojang.serialization.Codec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
   public static Codec homogeneousList(final ResourceKey registryKey, final Codec elementCodec) {
      return homogeneousList(registryKey, elementCodec, false);
   }

   public static Codec homogeneousList(final ResourceKey registryKey, final Codec elementCodec, final boolean alwaysUseList) {
      return HolderSetCodec.create(registryKey, RegistryFileCodec.create(registryKey, elementCodec), alwaysUseList);
   }

   public static Codec homogeneousList(final ResourceKey registryKey) {
      return homogeneousList(registryKey, false);
   }

   public static Codec homogeneousList(final ResourceKey registryKey, final boolean alwaysUseList) {
      return HolderSetCodec.create(registryKey, RegistryFixedCodec.create(registryKey), alwaysUseList);
   }
}
