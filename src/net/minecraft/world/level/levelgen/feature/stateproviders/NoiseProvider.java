package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseProvider extends NoiseBasedStateProvider {
   public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((i) -> noiseProviderCodec(i).apply(i, NoiseProvider::new));
   protected final List states;

   protected static Products.P4 noiseProviderCodec(final RecordCodecBuilder.Instance instance) {
      return noiseCodec(instance).and(ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("states").forGetter((p) -> p.states));
   }

   public NoiseProvider(final long seed, final NormalNoise.NoiseParameters parameters, final float scale, final List states) {
      super(seed, parameters, scale);
      this.states = states;
   }

   protected BlockStateProviderType type() {
      return BlockStateProviderType.NOISE_PROVIDER;
   }

   public BlockState getState(final RandomSource random, final BlockPos pos) {
      return this.getRandomState(this.states, pos, (double)this.scale);
   }

   protected BlockState getRandomState(final List states, final BlockPos pos, final double scale) {
      double noiseValue = this.getNoiseValue(pos, scale);
      return this.getRandomState(states, noiseValue);
   }

   protected BlockState getRandomState(final List states, final double noiseValue) {
      double placementValue = Mth.clamp(((double)1.0F + noiseValue) / (double)2.0F, (double)0.0F, 0.9999);
      return (BlockState)states.get((int)(placementValue * (double)states.size()));
   }
}
