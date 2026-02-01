package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

@FunctionalInterface
public interface PieceGeneratorSupplier {
   Optional createGenerator(Context context);

   static PieceGeneratorSupplier simple(final Predicate check, final PieceGenerator generator) {
      Optional<PieceGenerator<C>> result = Optional.of(generator);
      return (context) -> check.test(context) ? result : Optional.empty();
   }

   static Predicate checkForBiomeOnTop(final Heightmap.Types type) {
      return (context) -> context.validBiomeOnTop(type);
   }

   public static record Context(ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, long seed, ChunkPos chunkPos, FeatureConfiguration config, LevelHeightAccessor heightAccessor, Predicate validBiome, StructureTemplateManager structureTemplateManager, RegistryAccess registryAccess) {
      public boolean validBiomeOnTop(final Heightmap.Types type) {
         int blockX = this.chunkPos.getMiddleBlockX();
         int blockZ = this.chunkPos.getMiddleBlockZ();
         int blockY = this.chunkGenerator.getFirstOccupiedHeight(blockX, blockZ, type, this.heightAccessor, this.randomState);
         Holder<Biome> biome = this.chunkGenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(blockY), QuartPos.fromBlock(blockZ), this.randomState.sampler());
         return this.validBiome.test(biome);
      }
   }
}
