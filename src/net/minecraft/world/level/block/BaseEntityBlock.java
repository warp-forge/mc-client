package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class BaseEntityBlock extends Block implements EntityBlock {
   protected BaseEntityBlock(final BlockBehaviour.Properties properties) {
      super(properties);
   }

   protected abstract MapCodec codec();

   protected boolean triggerEvent(final BlockState state, final Level level, final BlockPos pos, final int b0, final int b1) {
      super.triggerEvent(state, level, pos, b0, b1);
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return blockEntity == null ? false : blockEntity.triggerEvent(b0, b1);
   }

   protected @Nullable MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return blockEntity instanceof MenuProvider ? (MenuProvider)blockEntity : null;
   }

   protected static @Nullable BlockEntityTicker createTickerHelper(final BlockEntityType actual, final BlockEntityType expected, final BlockEntityTicker ticker) {
      return expected == actual ? ticker : null;
   }
}
