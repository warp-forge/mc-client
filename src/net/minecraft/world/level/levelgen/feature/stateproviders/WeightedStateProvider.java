package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
   public static final MapCodec CODEC;
   private final WeightedList weightedList;

   private static DataResult create(final WeightedList weightedList) {
      return weightedList.isEmpty() ? DataResult.error(() -> "WeightedStateProvider with no states") : DataResult.success(new WeightedStateProvider(weightedList));
   }

   public WeightedStateProvider(final WeightedList weightedList) {
      this.weightedList = weightedList;
   }

   public WeightedStateProvider(final WeightedList.Builder weightedList) {
      this(weightedList.build());
   }

   protected BlockStateProviderType type() {
      return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
   }

   public BlockState getState(final RandomSource random, final BlockPos pos) {
      return (BlockState)this.weightedList.getRandomOrThrow(random);
   }

   static {
      CODEC = WeightedList.nonEmptyCodec(BlockState.CODEC).comapFlatMap(WeightedStateProvider::create, (p) -> p.weightedList).fieldOf("entries");
   }
}
