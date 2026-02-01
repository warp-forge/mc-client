package net.minecraft.world.level.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class DoubleBlockCombiner {
   public static NeighborCombineResult combineWithNeigbour(final BlockEntityType entityType, final Function typeResolver, final Function connectionResolver, final Property facingProperty, final BlockState state, final LevelAccessor level, final BlockPos pos, final BiPredicate blockedChecker) {
      S blockEntity = (S)entityType.getBlockEntity(level, pos);
      if (blockEntity == null) {
         return Combiner::acceptNone;
      } else if (blockedChecker.test(level, pos)) {
         return Combiner::acceptNone;
      } else {
         BlockType type = (BlockType)typeResolver.apply(state);
         boolean single = type == DoubleBlockCombiner.BlockType.SINGLE;
         boolean isFirst = type == DoubleBlockCombiner.BlockType.FIRST;
         if (single) {
            return new NeighborCombineResult.Single(blockEntity);
         } else {
            BlockPos neighborPos = pos.relative((Direction)connectionResolver.apply(state));
            BlockState neighbourState = level.getBlockState(neighborPos);
            if (neighbourState.is(state.getBlock())) {
               BlockType neighbourType = (BlockType)typeResolver.apply(neighbourState);
               if (neighbourType != DoubleBlockCombiner.BlockType.SINGLE && type != neighbourType && neighbourState.getValue(facingProperty) == state.getValue(facingProperty)) {
                  if (blockedChecker.test(level, neighborPos)) {
                     return Combiner::acceptNone;
                  }

                  S neighbour = (S)entityType.getBlockEntity(level, neighborPos);
                  if (neighbour != null) {
                     S first = (S)(isFirst ? blockEntity : neighbour);
                     S second = (S)(isFirst ? neighbour : blockEntity);
                     return new NeighborCombineResult.Double(first, second);
                  }
               }
            }

            return new NeighborCombineResult.Single(blockEntity);
         }
      }
   }

   public static enum BlockType {
      SINGLE,
      FIRST,
      SECOND;

      // $FF: synthetic method
      private static BlockType[] $values() {
         return new BlockType[]{SINGLE, FIRST, SECOND};
      }
   }

   public interface Combiner {
      Object acceptDouble(Object first, Object second);

      Object acceptSingle(Object single);

      Object acceptNone();
   }

   public interface NeighborCombineResult {
      Object apply(Combiner callback);

      public static final class Double implements NeighborCombineResult {
         private final Object first;
         private final Object second;

         public Double(final Object first, final Object second) {
            this.first = first;
            this.second = second;
         }

         public Object apply(final Combiner callback) {
            return callback.acceptDouble(this.first, this.second);
         }
      }

      public static final class Single implements NeighborCombineResult {
         private final Object single;

         public Single(final Object single) {
            this.single = single;
         }

         public Object apply(final Combiner callback) {
            return callback.acceptSingle(this.single);
         }
      }
   }
}
