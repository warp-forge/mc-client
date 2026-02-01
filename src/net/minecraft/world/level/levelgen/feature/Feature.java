package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FallenTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public abstract class Feature {
   public static final Feature NO_OP;
   public static final Feature TREE;
   public static final Feature FALLEN_TREE;
   public static final Feature FLOWER;
   public static final Feature NO_BONEMEAL_FLOWER;
   public static final Feature RANDOM_PATCH;
   public static final Feature BLOCK_PILE;
   public static final Feature SPRING;
   public static final Feature CHORUS_PLANT;
   public static final Feature REPLACE_SINGLE_BLOCK;
   public static final Feature VOID_START_PLATFORM;
   public static final Feature DESERT_WELL;
   public static final Feature FOSSIL;
   public static final Feature HUGE_RED_MUSHROOM;
   public static final Feature HUGE_BROWN_MUSHROOM;
   public static final Feature ICE_SPIKE;
   public static final Feature GLOWSTONE_BLOB;
   public static final Feature FREEZE_TOP_LAYER;
   public static final Feature VINES;
   public static final Feature BLOCK_COLUMN;
   public static final Feature VEGETATION_PATCH;
   public static final Feature WATERLOGGED_VEGETATION_PATCH;
   public static final Feature ROOT_SYSTEM;
   public static final Feature MULTIFACE_GROWTH;
   public static final Feature UNDERWATER_MAGMA;
   public static final Feature MONSTER_ROOM;
   public static final Feature BLUE_ICE;
   public static final Feature ICEBERG;
   public static final Feature FOREST_ROCK;
   public static final Feature DISK;
   public static final Feature LAKE;
   public static final Feature ORE;
   public static final Feature END_PLATFORM;
   public static final Feature END_SPIKE;
   public static final Feature END_ISLAND;
   public static final Feature END_GATEWAY;
   public static final SeagrassFeature SEAGRASS;
   public static final Feature KELP;
   public static final Feature CORAL_TREE;
   public static final Feature CORAL_MUSHROOM;
   public static final Feature CORAL_CLAW;
   public static final Feature SEA_PICKLE;
   public static final Feature SIMPLE_BLOCK;
   public static final Feature BAMBOO;
   public static final Feature HUGE_FUNGUS;
   public static final Feature NETHER_FOREST_VEGETATION;
   public static final Feature WEEPING_VINES;
   public static final Feature TWISTING_VINES;
   public static final Feature BASALT_COLUMNS;
   public static final Feature DELTA_FEATURE;
   public static final Feature REPLACE_BLOBS;
   public static final Feature FILL_LAYER;
   public static final BonusChestFeature BONUS_CHEST;
   public static final Feature BASALT_PILLAR;
   public static final Feature SCATTERED_ORE;
   public static final Feature RANDOM_SELECTOR;
   public static final Feature SIMPLE_RANDOM_SELECTOR;
   public static final Feature RANDOM_BOOLEAN_SELECTOR;
   public static final Feature GEODE;
   public static final Feature DRIPSTONE_CLUSTER;
   public static final Feature LARGE_DRIPSTONE;
   public static final Feature POINTED_DRIPSTONE;
   public static final Feature SCULK_PATCH;
   private final MapCodec configuredCodec;

   private static Feature register(final String name, final Feature feature) {
      return (Feature)Registry.register(BuiltInRegistries.FEATURE, (String)name, feature);
   }

   public Feature(final Codec codec) {
      this.configuredCodec = codec.fieldOf("config").xmap((c) -> new ConfiguredFeature(this, c), ConfiguredFeature::config);
   }

   public MapCodec configuredCodec() {
      return this.configuredCodec;
   }

   protected void setBlock(final LevelWriter level, final BlockPos pos, final BlockState blockState) {
      level.setBlock(pos, blockState, 3);
   }

   public static Predicate isReplaceable(final TagKey cannotReplaceTag) {
      return (s) -> !s.is(cannotReplaceTag);
   }

   protected void safeSetBlock(final WorldGenLevel level, final BlockPos pos, final BlockState state, final Predicate canReplace) {
      if (canReplace.test(level.getBlockState(pos))) {
         level.setBlock(pos, state, 2);
      }

   }

   public abstract boolean place(final FeaturePlaceContext context);

   public boolean place(final FeatureConfiguration config, final WorldGenLevel level, final ChunkGenerator chunkGenerator, final RandomSource random, final BlockPos origin) {
      return level.ensureCanWrite(origin) ? this.place(new FeaturePlaceContext(Optional.empty(), level, chunkGenerator, random, origin, config)) : false;
   }

   protected static boolean isStone(final BlockState state) {
      return state.is(BlockTags.BASE_STONE_OVERWORLD);
   }

   public static boolean isDirt(final BlockState state) {
      return state.is(BlockTags.DIRT);
   }

   public static boolean isGrassOrDirt(final LevelSimulatedReader level, final BlockPos pos) {
      return level.isStateAtPosition(pos, Feature::isDirt);
   }

   public static boolean checkNeighbors(final Function blockGetter, final BlockPos pos, final Predicate predicate) {
      BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.values()) {
         neighborPos.setWithOffset(pos, (Direction)direction);
         if (predicate.test((BlockState)blockGetter.apply(neighborPos))) {
            return true;
         }
      }

      return false;
   }

   public static boolean isAdjacentToAir(final Function blockGetter, final BlockPos pos) {
      return checkNeighbors(blockGetter, pos, BlockBehaviour.BlockStateBase::isAir);
   }

   protected void markAboveForPostProcessing(final WorldGenLevel level, final BlockPos placePos) {
      BlockPos.MutableBlockPos pos = placePos.mutable();

      for(int i = 0; i < 2; ++i) {
         pos.move(Direction.UP);
         if (level.getBlockState(pos).isAir()) {
            return;
         }

         level.getChunk(pos).markPosForPostprocessing(pos);
      }

   }

   static {
      NO_OP = register("no_op", new NoOpFeature(NoneFeatureConfiguration.CODEC));
      TREE = register("tree", new TreeFeature(TreeConfiguration.CODEC));
      FALLEN_TREE = register("fallen_tree", new FallenTreeFeature(FallenTreeConfiguration.CODEC));
      FLOWER = register("flower", new RandomPatchFeature(RandomPatchConfiguration.CODEC));
      NO_BONEMEAL_FLOWER = register("no_bonemeal_flower", new RandomPatchFeature(RandomPatchConfiguration.CODEC));
      RANDOM_PATCH = register("random_patch", new RandomPatchFeature(RandomPatchConfiguration.CODEC));
      BLOCK_PILE = register("block_pile", new BlockPileFeature(BlockPileConfiguration.CODEC));
      SPRING = register("spring_feature", new SpringFeature(SpringConfiguration.CODEC));
      CHORUS_PLANT = register("chorus_plant", new ChorusPlantFeature(NoneFeatureConfiguration.CODEC));
      REPLACE_SINGLE_BLOCK = register("replace_single_block", new ReplaceBlockFeature(ReplaceBlockConfiguration.CODEC));
      VOID_START_PLATFORM = register("void_start_platform", new VoidStartPlatformFeature(NoneFeatureConfiguration.CODEC));
      DESERT_WELL = register("desert_well", new DesertWellFeature(NoneFeatureConfiguration.CODEC));
      FOSSIL = register("fossil", new FossilFeature(FossilFeatureConfiguration.CODEC));
      HUGE_RED_MUSHROOM = register("huge_red_mushroom", new HugeRedMushroomFeature(HugeMushroomFeatureConfiguration.CODEC));
      HUGE_BROWN_MUSHROOM = register("huge_brown_mushroom", new HugeBrownMushroomFeature(HugeMushroomFeatureConfiguration.CODEC));
      ICE_SPIKE = register("ice_spike", new IceSpikeFeature(NoneFeatureConfiguration.CODEC));
      GLOWSTONE_BLOB = register("glowstone_blob", new GlowstoneFeature(NoneFeatureConfiguration.CODEC));
      FREEZE_TOP_LAYER = register("freeze_top_layer", new SnowAndFreezeFeature(NoneFeatureConfiguration.CODEC));
      VINES = register("vines", new VinesFeature(NoneFeatureConfiguration.CODEC));
      BLOCK_COLUMN = register("block_column", new BlockColumnFeature(BlockColumnConfiguration.CODEC));
      VEGETATION_PATCH = register("vegetation_patch", new VegetationPatchFeature(VegetationPatchConfiguration.CODEC));
      WATERLOGGED_VEGETATION_PATCH = register("waterlogged_vegetation_patch", new WaterloggedVegetationPatchFeature(VegetationPatchConfiguration.CODEC));
      ROOT_SYSTEM = register("root_system", new RootSystemFeature(RootSystemConfiguration.CODEC));
      MULTIFACE_GROWTH = register("multiface_growth", new MultifaceGrowthFeature(MultifaceGrowthConfiguration.CODEC));
      UNDERWATER_MAGMA = register("underwater_magma", new UnderwaterMagmaFeature(UnderwaterMagmaConfiguration.CODEC));
      MONSTER_ROOM = register("monster_room", new MonsterRoomFeature(NoneFeatureConfiguration.CODEC));
      BLUE_ICE = register("blue_ice", new BlueIceFeature(NoneFeatureConfiguration.CODEC));
      ICEBERG = register("iceberg", new IcebergFeature(BlockStateConfiguration.CODEC));
      FOREST_ROCK = register("forest_rock", new BlockBlobFeature(BlockStateConfiguration.CODEC));
      DISK = register("disk", new DiskFeature(DiskConfiguration.CODEC));
      LAKE = register("lake", new LakeFeature(LakeFeature.Configuration.CODEC));
      ORE = register("ore", new OreFeature(OreConfiguration.CODEC));
      END_PLATFORM = register("end_platform", new EndPlatformFeature(NoneFeatureConfiguration.CODEC));
      END_SPIKE = register("end_spike", new SpikeFeature(SpikeConfiguration.CODEC));
      END_ISLAND = register("end_island", new EndIslandFeature(NoneFeatureConfiguration.CODEC));
      END_GATEWAY = register("end_gateway", new EndGatewayFeature(EndGatewayConfiguration.CODEC));
      SEAGRASS = (SeagrassFeature)register("seagrass", new SeagrassFeature(ProbabilityFeatureConfiguration.CODEC));
      KELP = register("kelp", new KelpFeature(NoneFeatureConfiguration.CODEC));
      CORAL_TREE = register("coral_tree", new CoralTreeFeature(NoneFeatureConfiguration.CODEC));
      CORAL_MUSHROOM = register("coral_mushroom", new CoralMushroomFeature(NoneFeatureConfiguration.CODEC));
      CORAL_CLAW = register("coral_claw", new CoralClawFeature(NoneFeatureConfiguration.CODEC));
      SEA_PICKLE = register("sea_pickle", new SeaPickleFeature(CountConfiguration.CODEC));
      SIMPLE_BLOCK = register("simple_block", new SimpleBlockFeature(SimpleBlockConfiguration.CODEC));
      BAMBOO = register("bamboo", new BambooFeature(ProbabilityFeatureConfiguration.CODEC));
      HUGE_FUNGUS = register("huge_fungus", new HugeFungusFeature(HugeFungusConfiguration.CODEC));
      NETHER_FOREST_VEGETATION = register("nether_forest_vegetation", new NetherForestVegetationFeature(NetherForestVegetationConfig.CODEC));
      WEEPING_VINES = register("weeping_vines", new WeepingVinesFeature(NoneFeatureConfiguration.CODEC));
      TWISTING_VINES = register("twisting_vines", new TwistingVinesFeature(TwistingVinesConfig.CODEC));
      BASALT_COLUMNS = register("basalt_columns", new BasaltColumnsFeature(ColumnFeatureConfiguration.CODEC));
      DELTA_FEATURE = register("delta_feature", new DeltaFeature(DeltaFeatureConfiguration.CODEC));
      REPLACE_BLOBS = register("netherrack_replace_blobs", new ReplaceBlobsFeature(ReplaceSphereConfiguration.CODEC));
      FILL_LAYER = register("fill_layer", new FillLayerFeature(LayerConfiguration.CODEC));
      BONUS_CHEST = (BonusChestFeature)register("bonus_chest", new BonusChestFeature(NoneFeatureConfiguration.CODEC));
      BASALT_PILLAR = register("basalt_pillar", new BasaltPillarFeature(NoneFeatureConfiguration.CODEC));
      SCATTERED_ORE = register("scattered_ore", new ScatteredOreFeature(OreConfiguration.CODEC));
      RANDOM_SELECTOR = register("random_selector", new RandomSelectorFeature(RandomFeatureConfiguration.CODEC));
      SIMPLE_RANDOM_SELECTOR = register("simple_random_selector", new SimpleRandomSelectorFeature(SimpleRandomFeatureConfiguration.CODEC));
      RANDOM_BOOLEAN_SELECTOR = register("random_boolean_selector", new RandomBooleanSelectorFeature(RandomBooleanFeatureConfiguration.CODEC));
      GEODE = register("geode", new GeodeFeature(GeodeConfiguration.CODEC));
      DRIPSTONE_CLUSTER = register("dripstone_cluster", new DripstoneClusterFeature(DripstoneClusterConfiguration.CODEC));
      LARGE_DRIPSTONE = register("large_dripstone", new LargeDripstoneFeature(LargeDripstoneConfiguration.CODEC));
      POINTED_DRIPSTONE = register("pointed_dripstone", new PointedDripstoneFeature(PointedDripstoneConfiguration.CODEC));
      SCULK_PATCH = register("sculk_patch", new SculkPatchFeature(SculkPatchConfiguration.CODEC));
   }
}
