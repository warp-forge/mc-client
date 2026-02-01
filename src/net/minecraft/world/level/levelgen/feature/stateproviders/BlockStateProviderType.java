package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class BlockStateProviderType {
   public static final BlockStateProviderType SIMPLE_STATE_PROVIDER;
   public static final BlockStateProviderType WEIGHTED_STATE_PROVIDER;
   public static final BlockStateProviderType NOISE_THRESHOLD_PROVIDER;
   public static final BlockStateProviderType NOISE_PROVIDER;
   public static final BlockStateProviderType DUAL_NOISE_PROVIDER;
   public static final BlockStateProviderType ROTATED_BLOCK_PROVIDER;
   public static final BlockStateProviderType RANDOMIZED_INT_STATE_PROVIDER;
   private final MapCodec codec;

   private static BlockStateProviderType register(final String name, final MapCodec codec) {
      return (BlockStateProviderType)Registry.register(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, (String)name, new BlockStateProviderType(codec));
   }

   private BlockStateProviderType(final MapCodec codec) {
      this.codec = codec;
   }

   public MapCodec codec() {
      return this.codec;
   }

   static {
      SIMPLE_STATE_PROVIDER = register("simple_state_provider", SimpleStateProvider.CODEC);
      WEIGHTED_STATE_PROVIDER = register("weighted_state_provider", WeightedStateProvider.CODEC);
      NOISE_THRESHOLD_PROVIDER = register("noise_threshold_provider", NoiseThresholdProvider.CODEC);
      NOISE_PROVIDER = register("noise_provider", NoiseProvider.CODEC);
      DUAL_NOISE_PROVIDER = register("dual_noise_provider", DualNoiseProvider.CODEC);
      ROTATED_BLOCK_PROVIDER = register("rotated_block_provider", RotatedBlockProvider.CODEC);
      RANDOMIZED_INT_STATE_PROVIDER = register("randomized_int_state_provider", RandomizedIntStateProvider.CODEC);
   }
}
