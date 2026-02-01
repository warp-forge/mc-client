package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record NoiseGeneratorSettings(NoiseSettings noiseSettings, BlockState defaultBlock, BlockState defaultFluid, NoiseRouter noiseRouter, SurfaceRules.RuleSource surfaceRule, List spawnTarget, int seaLevel, boolean disableMobGeneration, boolean aquifersEnabled, boolean oreVeinsEnabled, boolean useLegacyRandomSource) {
   public static final Codec DIRECT_CODEC = RecordCodecBuilder.create((i) -> i.group(NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings), BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::defaultBlock), BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::defaultFluid), NoiseRouter.CODEC.fieldOf("noise_router").forGetter(NoiseGeneratorSettings::noiseRouter), SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(NoiseGeneratorSettings::surfaceRule), Climate.ParameterPoint.CODEC.listOf().fieldOf("spawn_target").forGetter(NoiseGeneratorSettings::spawnTarget), Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel), Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration), Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled), Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::oreVeinsEnabled), Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource)).apply(i, NoiseGeneratorSettings::new));
   public static final Codec CODEC;
   public static final ResourceKey OVERWORLD;
   public static final ResourceKey LARGE_BIOMES;
   public static final ResourceKey AMPLIFIED;
   public static final ResourceKey NETHER;
   public static final ResourceKey END;
   public static final ResourceKey CAVES;
   public static final ResourceKey FLOATING_ISLANDS;

   /** @deprecated */
   @Deprecated
   public boolean disableMobGeneration() {
      return this.disableMobGeneration;
   }

   public boolean isAquifersEnabled() {
      return this.aquifersEnabled && !SharedConstants.DEBUG_DISABLE_AQUIFERS;
   }

   public boolean oreVeinsEnabled() {
      return this.oreVeinsEnabled && !SharedConstants.DEBUG_DISABLE_ORE_VEINS;
   }

   public WorldgenRandom.Algorithm getRandomSource() {
      return this.useLegacyRandomSource ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
   }

   public static void bootstrap(final BootstrapContext context) {
      context.register(OVERWORLD, overworld(context, false, false));
      context.register(LARGE_BIOMES, overworld(context, false, true));
      context.register(AMPLIFIED, overworld(context, true, false));
      context.register(NETHER, nether(context));
      context.register(END, end(context));
      context.register(CAVES, caves(context));
      context.register(FLOATING_ISLANDS, floatingIslands(context));
   }

   private static NoiseGeneratorSettings end(final BootstrapContext context) {
      return new NoiseGeneratorSettings(NoiseSettings.END_NOISE_SETTINGS, Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), NoiseRouterData.end(context.lookup(Registries.DENSITY_FUNCTION)), SurfaceRuleData.end(), List.of(), 0, true, false, false, true);
   }

   private static NoiseGeneratorSettings nether(final BootstrapContext context) {
      return new NoiseGeneratorSettings(NoiseSettings.NETHER_NOISE_SETTINGS, Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), NoiseRouterData.nether(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE)), SurfaceRuleData.nether(), List.of(), 32, false, false, false, true);
   }

   private static NoiseGeneratorSettings overworld(final BootstrapContext context, final boolean isAmplified, final boolean largeBiomes) {
      return new NoiseGeneratorSettings(NoiseSettings.OVERWORLD_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworld(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE), largeBiomes, isAmplified), SurfaceRuleData.overworld(), (new OverworldBiomeBuilder()).spawnTarget(), 63, false, true, true, false);
   }

   private static NoiseGeneratorSettings caves(final BootstrapContext context) {
      return new NoiseGeneratorSettings(NoiseSettings.CAVES_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.caves(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE)), SurfaceRuleData.overworldLike(false, true, true), List.of(), 32, false, false, false, true);
   }

   private static NoiseGeneratorSettings floatingIslands(final BootstrapContext context) {
      return new NoiseGeneratorSettings(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.floatingIslands(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE)), SurfaceRuleData.overworldLike(false, false, false), List.of(), -64, false, false, false, true);
   }

   public static NoiseGeneratorSettings dummy() {
      return new NoiseGeneratorSettings(NoiseSettings.OVERWORLD_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), NoiseRouterData.none(), SurfaceRuleData.air(), List.of(), 63, true, false, false, false);
   }

   static {
      CODEC = RegistryFileCodec.create(Registries.NOISE_SETTINGS, DIRECT_CODEC);
      OVERWORLD = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("overworld"));
      LARGE_BIOMES = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("large_biomes"));
      AMPLIFIED = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("amplified"));
      NETHER = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("nether"));
      END = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("end"));
      CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("caves"));
      FLOATING_ISLANDS = ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.withDefaultNamespace("floating_islands"));
   }
}
