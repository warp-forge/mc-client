package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature {
   public ReplaceBlockFeature(final Codec codec) {
      super(codec);
   }

   public boolean place(final FeaturePlaceContext context) {
      WorldGenLevel level = context.level();
      BlockPos origin = context.origin();
      ReplaceBlockConfiguration config = (ReplaceBlockConfiguration)context.config();

      for(OreConfiguration.TargetBlockState targetState : config.targetStates) {
         if (targetState.target.test(level.getBlockState(origin), context.random())) {
            level.setBlock(origin, targetState.state, 2);
            break;
         }
      }

      return true;
   }
}
