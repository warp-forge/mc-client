package net.minecraft.world.level.biome;

import net.minecraft.core.Holder;

public interface BiomeResolver {
   Holder getNoiseBiome(final int quartX, final int quartY, final int quartZ, final Climate.Sampler sampler);
}
