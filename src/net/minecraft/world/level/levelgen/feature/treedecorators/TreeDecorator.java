package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class TreeDecorator {
   public static final Codec CODEC;

   protected abstract TreeDecoratorType type();

   public abstract void place(final Context context);

   static {
      CODEC = BuiltInRegistries.TREE_DECORATOR_TYPE.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);
   }

   public static final class Context {
      private final LevelSimulatedReader level;
      private final BiConsumer decorationSetter;
      private final RandomSource random;
      private final ObjectArrayList logs;
      private final ObjectArrayList leaves;
      private final ObjectArrayList roots;

      public Context(final LevelSimulatedReader level, final BiConsumer decorationSetter, final RandomSource random, final Set trunkSet, final Set foliageSet, final Set rootSet) {
         this.level = level;
         this.decorationSetter = decorationSetter;
         this.random = random;
         this.roots = new ObjectArrayList(rootSet);
         this.logs = new ObjectArrayList(trunkSet);
         this.leaves = new ObjectArrayList(foliageSet);
         this.logs.sort(Comparator.comparingInt(Vec3i::getY));
         this.leaves.sort(Comparator.comparingInt(Vec3i::getY));
         this.roots.sort(Comparator.comparingInt(Vec3i::getY));
      }

      public void placeVine(final BlockPos pos, final BooleanProperty direction) {
         this.setBlock(pos, (BlockState)Blocks.VINE.defaultBlockState().setValue(direction, true));
      }

      public void setBlock(final BlockPos pos, final BlockState state) {
         this.decorationSetter.accept(pos, state);
      }

      public boolean isAir(final BlockPos pos) {
         return this.level.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::isAir);
      }

      public boolean checkBlock(final BlockPos pos, final Predicate predicate) {
         return this.level.isStateAtPosition(pos, predicate);
      }

      public LevelSimulatedReader level() {
         return this.level;
      }

      public RandomSource random() {
         return this.random;
      }

      public ObjectArrayList logs() {
         return this.logs;
      }

      public ObjectArrayList leaves() {
         return this.leaves;
      }

      public ObjectArrayList roots() {
         return this.roots;
      }
   }
}
