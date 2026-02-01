package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PlayerWallHeadBlock extends WallSkullBlock {
   public static final MapCodec CODEC = simpleCodec(PlayerWallHeadBlock::new);

   public MapCodec codec() {
      return CODEC;
   }

   protected PlayerWallHeadBlock(final BlockBehaviour.Properties properties) {
      super(SkullBlock.Types.PLAYER, properties);
   }
}
