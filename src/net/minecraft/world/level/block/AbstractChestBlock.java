package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractChestBlock extends BaseEntityBlock {
   protected final Supplier blockEntityType;

   protected AbstractChestBlock(final BlockBehaviour.Properties properties, final Supplier blockEntityType) {
      super(properties);
      this.blockEntityType = blockEntityType;
   }

   protected abstract MapCodec codec();

   public abstract DoubleBlockCombiner.NeighborCombineResult combine(BlockState state, Level level, BlockPos pos, boolean ignoreBeingBlocked);
}
