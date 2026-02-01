package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;

public record TrimMaterial(MaterialAssetGroup assets, Component description) {
   public static final Codec DIRECT_CODEC = RecordCodecBuilder.create((i) -> i.group(MaterialAssetGroup.MAP_CODEC.forGetter(TrimMaterial::assets), ComponentSerialization.CODEC.fieldOf("description").forGetter(TrimMaterial::description)).apply(i, TrimMaterial::new));
   public static final StreamCodec DIRECT_STREAM_CODEC;
   public static final Codec CODEC;
   public static final StreamCodec STREAM_CODEC;

   static {
      DIRECT_STREAM_CODEC = StreamCodec.composite(MaterialAssetGroup.STREAM_CODEC, TrimMaterial::assets, ComponentSerialization.STREAM_CODEC, TrimMaterial::description, TrimMaterial::new);
      CODEC = RegistryFileCodec.create(Registries.TRIM_MATERIAL, DIRECT_CODEC);
      STREAM_CODEC = ByteBufCodecs.holder(Registries.TRIM_MATERIAL, DIRECT_STREAM_CODEC);
   }
}
