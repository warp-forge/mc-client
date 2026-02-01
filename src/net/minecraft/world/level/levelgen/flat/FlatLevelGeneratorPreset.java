package net.minecraft.world.level.levelgen.flat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.item.Item;

public record FlatLevelGeneratorPreset(Holder displayItem, FlatLevelGeneratorSettings settings) {
   public static final Codec DIRECT_CODEC = RecordCodecBuilder.create((i) -> i.group(Item.CODEC.fieldOf("display").forGetter((e) -> e.displayItem), FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter((e) -> e.settings)).apply(i, FlatLevelGeneratorPreset::new));
   public static final Codec CODEC;

   static {
      CODEC = RegistryFileCodec.create(Registries.FLAT_LEVEL_GENERATOR_PRESET, DIRECT_CODEC);
   }
}
