package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ParticleResources implements PreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
   private final Map spriteSets = Maps.newHashMap();
   private final Int2ObjectMap providers = new Int2ObjectOpenHashMap();
   private @Nullable Runnable onReload;

   public ParticleResources() {
      this.registerProviders();
   }

   public void onReload(final Runnable onReload) {
      this.onReload = onReload;
   }

   private void registerProviders() {
      this.register(ParticleTypes.ANGRY_VILLAGER, (SpriteParticleRegistration)(HeartParticle.AngryVillagerProvider::new));
      this.register(ParticleTypes.BLOCK_MARKER, (ParticleProvider)(new BlockMarker.Provider()));
      this.register(ParticleTypes.BLOCK, (ParticleProvider)(new TerrainParticle.Provider()));
      this.register(ParticleTypes.BUBBLE, (SpriteParticleRegistration)(BubbleParticle.Provider::new));
      this.register(ParticleTypes.BUBBLE_COLUMN_UP, (SpriteParticleRegistration)(BubbleColumnUpParticle.Provider::new));
      this.register(ParticleTypes.BUBBLE_POP, (SpriteParticleRegistration)(BubblePopParticle.Provider::new));
      this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, (SpriteParticleRegistration)(CampfireSmokeParticle.CosyProvider::new));
      this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, (SpriteParticleRegistration)(CampfireSmokeParticle.SignalProvider::new));
      this.register(ParticleTypes.CLOUD, (SpriteParticleRegistration)(PlayerCloudParticle.Provider::new));
      this.register(ParticleTypes.COMPOSTER, (SpriteParticleRegistration)(SuspendedTownParticle.ComposterFillProvider::new));
      this.register(ParticleTypes.COPPER_FIRE_FLAME, (SpriteParticleRegistration)(FlameParticle.Provider::new));
      this.register(ParticleTypes.CRIT, (SpriteParticleRegistration)(CritParticle.Provider::new));
      this.register(ParticleTypes.CURRENT_DOWN, (SpriteParticleRegistration)(WaterCurrentDownParticle.Provider::new));
      this.register(ParticleTypes.DAMAGE_INDICATOR, (SpriteParticleRegistration)(CritParticle.DamageIndicatorProvider::new));
      this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
      this.register(ParticleTypes.DOLPHIN, (SpriteParticleRegistration)(SuspendedTownParticle.DolphinSpeedProvider::new));
      this.register(ParticleTypes.DRIPPING_LAVA, (SpriteParticleRegistration)(DripParticle.LavaHangProvider::new));
      this.register(ParticleTypes.FALLING_LAVA, (SpriteParticleRegistration)(DripParticle.LavaFallProvider::new));
      this.register(ParticleTypes.LANDING_LAVA, (SpriteParticleRegistration)(DripParticle.LavaLandProvider::new));
      this.register(ParticleTypes.DRIPPING_WATER, (SpriteParticleRegistration)(DripParticle.WaterHangProvider::new));
      this.register(ParticleTypes.FALLING_WATER, (SpriteParticleRegistration)(DripParticle.WaterFallProvider::new));
      this.register(ParticleTypes.DUST, DustParticle.Provider::new);
      this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
      this.register(ParticleTypes.EFFECT, SpellParticle.InstantProvider::new);
      this.register(ParticleTypes.ELDER_GUARDIAN, (ParticleProvider)(new ElderGuardianParticle.Provider()));
      this.register(ParticleTypes.ENCHANTED_HIT, (SpriteParticleRegistration)(CritParticle.MagicProvider::new));
      this.register(ParticleTypes.ENCHANT, (SpriteParticleRegistration)(FlyTowardsPositionParticle.EnchantProvider::new));
      this.register(ParticleTypes.END_ROD, (SpriteParticleRegistration)(EndRodParticle.Provider::new));
      this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobEffectProvider::new);
      this.register(ParticleTypes.EXPLOSION_EMITTER, (ParticleProvider)(new HugeExplosionSeedParticle.Provider()));
      this.register(ParticleTypes.EXPLOSION, (SpriteParticleRegistration)(HugeExplosionParticle.Provider::new));
      this.register(ParticleTypes.SONIC_BOOM, (SpriteParticleRegistration)(SonicBoomParticle.Provider::new));
      this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
      this.register(ParticleTypes.GUST, (SpriteParticleRegistration)(GustParticle.Provider::new));
      this.register(ParticleTypes.SMALL_GUST, (SpriteParticleRegistration)(GustParticle.SmallProvider::new));
      this.register(ParticleTypes.GUST_EMITTER_LARGE, (ParticleProvider)(new GustSeedParticle.Provider((double)3.0F, 7, 0)));
      this.register(ParticleTypes.GUST_EMITTER_SMALL, (ParticleProvider)(new GustSeedParticle.Provider((double)1.0F, 3, 2)));
      this.register(ParticleTypes.FIREWORK, (SpriteParticleRegistration)(FireworkParticles.SparkProvider::new));
      this.register(ParticleTypes.FISHING, (SpriteParticleRegistration)(WakeParticle.Provider::new));
      this.register(ParticleTypes.FLAME, (SpriteParticleRegistration)(FlameParticle.Provider::new));
      this.register(ParticleTypes.INFESTED, (SpriteParticleRegistration)(SpellParticle.Provider::new));
      this.register(ParticleTypes.SCULK_SOUL, (SpriteParticleRegistration)(SoulParticle.EmissiveProvider::new));
      this.register(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Provider::new);
      this.register(ParticleTypes.SCULK_CHARGE_POP, (SpriteParticleRegistration)(SculkChargePopParticle.Provider::new));
      this.register(ParticleTypes.SOUL, (SpriteParticleRegistration)(SoulParticle.Provider::new));
      this.register(ParticleTypes.SOUL_FIRE_FLAME, (SpriteParticleRegistration)(FlameParticle.Provider::new));
      this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
      this.register(ParticleTypes.HAPPY_VILLAGER, (SpriteParticleRegistration)(SuspendedTownParticle.HappyVillagerProvider::new));
      this.register(ParticleTypes.HEART, (SpriteParticleRegistration)(HeartParticle.Provider::new));
      this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
      this.register(ParticleTypes.ITEM, (ParticleProvider)(new BreakingItemParticle.Provider()));
      this.register(ParticleTypes.ITEM_SLIME, (ParticleProvider)(new BreakingItemParticle.SlimeProvider()));
      this.register(ParticleTypes.ITEM_COBWEB, (ParticleProvider)(new BreakingItemParticle.CobwebProvider()));
      this.register(ParticleTypes.ITEM_SNOWBALL, (ParticleProvider)(new BreakingItemParticle.SnowballProvider()));
      this.register(ParticleTypes.LARGE_SMOKE, (SpriteParticleRegistration)(LargeSmokeParticle.Provider::new));
      this.register(ParticleTypes.LAVA, (SpriteParticleRegistration)(LavaParticle.Provider::new));
      this.register(ParticleTypes.MYCELIUM, (SpriteParticleRegistration)(SuspendedTownParticle.Provider::new));
      this.register(ParticleTypes.NAUTILUS, (SpriteParticleRegistration)(FlyTowardsPositionParticle.NautilusProvider::new));
      this.register(ParticleTypes.NOTE, (SpriteParticleRegistration)(NoteParticle.Provider::new));
      this.register(ParticleTypes.POOF, (SpriteParticleRegistration)(ExplodeParticle.Provider::new));
      this.register(ParticleTypes.PORTAL, (SpriteParticleRegistration)(PortalParticle.Provider::new));
      this.register(ParticleTypes.RAIN, (SpriteParticleRegistration)(WaterDropParticle.Provider::new));
      this.register(ParticleTypes.SMOKE, (SpriteParticleRegistration)(SmokeParticle.Provider::new));
      this.register(ParticleTypes.WHITE_SMOKE, (SpriteParticleRegistration)(WhiteSmokeParticle.Provider::new));
      this.register(ParticleTypes.SNEEZE, (SpriteParticleRegistration)(PlayerCloudParticle.SneezeProvider::new));
      this.register(ParticleTypes.SNOWFLAKE, (SpriteParticleRegistration)(SnowflakeParticle.Provider::new));
      this.register(ParticleTypes.SPIT, (SpriteParticleRegistration)(SpitParticle.Provider::new));
      this.register(ParticleTypes.SWEEP_ATTACK, (SpriteParticleRegistration)(AttackSweepParticle.Provider::new));
      this.register(ParticleTypes.TOTEM_OF_UNDYING, (SpriteParticleRegistration)(TotemParticle.Provider::new));
      this.register(ParticleTypes.SQUID_INK, (SpriteParticleRegistration)(SquidInkParticle.Provider::new));
      this.register(ParticleTypes.UNDERWATER, (SpriteParticleRegistration)(SuspendedParticle.UnderwaterProvider::new));
      this.register(ParticleTypes.SPLASH, (SpriteParticleRegistration)(SplashParticle.Provider::new));
      this.register(ParticleTypes.WITCH, (SpriteParticleRegistration)(SpellParticle.WitchProvider::new));
      this.register(ParticleTypes.DRIPPING_HONEY, (SpriteParticleRegistration)(DripParticle.HoneyHangProvider::new));
      this.register(ParticleTypes.FALLING_HONEY, (SpriteParticleRegistration)(DripParticle.HoneyFallProvider::new));
      this.register(ParticleTypes.LANDING_HONEY, (SpriteParticleRegistration)(DripParticle.HoneyLandProvider::new));
      this.register(ParticleTypes.FALLING_NECTAR, (SpriteParticleRegistration)(DripParticle.NectarFallProvider::new));
      this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, (SpriteParticleRegistration)(DripParticle.SporeBlossomFallProvider::new));
      this.register(ParticleTypes.SPORE_BLOSSOM_AIR, (SpriteParticleRegistration)(SuspendedParticle.SporeBlossomAirProvider::new));
      this.register(ParticleTypes.ASH, (SpriteParticleRegistration)(AshParticle.Provider::new));
      this.register(ParticleTypes.CRIMSON_SPORE, (SpriteParticleRegistration)(SuspendedParticle.CrimsonSporeProvider::new));
      this.register(ParticleTypes.WARPED_SPORE, (SpriteParticleRegistration)(SuspendedParticle.WarpedSporeProvider::new));
      this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (SpriteParticleRegistration)(DripParticle.ObsidianTearHangProvider::new));
      this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, (SpriteParticleRegistration)(DripParticle.ObsidianTearFallProvider::new));
      this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, (SpriteParticleRegistration)(DripParticle.ObsidianTearLandProvider::new));
      this.register(ParticleTypes.REVERSE_PORTAL, (SpriteParticleRegistration)(ReversePortalParticle.ReversePortalProvider::new));
      this.register(ParticleTypes.WHITE_ASH, (SpriteParticleRegistration)(WhiteAshParticle.Provider::new));
      this.register(ParticleTypes.SMALL_FLAME, (SpriteParticleRegistration)(FlameParticle.SmallFlameProvider::new));
      this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, (SpriteParticleRegistration)(DripParticle.DripstoneWaterHangProvider::new));
      this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, (SpriteParticleRegistration)(DripParticle.DripstoneWaterFallProvider::new));
      this.register(ParticleTypes.CHERRY_LEAVES, (SpriteParticleRegistration)(FallingLeavesParticle.CherryProvider::new));
      this.register(ParticleTypes.PALE_OAK_LEAVES, (SpriteParticleRegistration)(FallingLeavesParticle.PaleOakProvider::new));
      this.register(ParticleTypes.TINTED_LEAVES, FallingLeavesParticle.TintedLeavesProvider::new);
      this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, (SpriteParticleRegistration)(DripParticle.DripstoneLavaHangProvider::new));
      this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, (SpriteParticleRegistration)(DripParticle.DripstoneLavaFallProvider::new));
      this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
      this.register(ParticleTypes.TRAIL, TrailParticle.Provider::new);
      this.register(ParticleTypes.GLOW_SQUID_INK, (SpriteParticleRegistration)(SquidInkParticle.GlowInkProvider::new));
      this.register(ParticleTypes.GLOW, (SpriteParticleRegistration)(GlowParticle.GlowSquidProvider::new));
      this.register(ParticleTypes.WAX_ON, (SpriteParticleRegistration)(GlowParticle.WaxOnProvider::new));
      this.register(ParticleTypes.WAX_OFF, (SpriteParticleRegistration)(GlowParticle.WaxOffProvider::new));
      this.register(ParticleTypes.ELECTRIC_SPARK, (SpriteParticleRegistration)(GlowParticle.ElectricSparkProvider::new));
      this.register(ParticleTypes.SCRAPE, (SpriteParticleRegistration)(GlowParticle.ScrapeProvider::new));
      this.register(ParticleTypes.SHRIEK, ShriekParticle.Provider::new);
      this.register(ParticleTypes.EGG_CRACK, (SpriteParticleRegistration)(SuspendedTownParticle.EggCrackProvider::new));
      this.register(ParticleTypes.DUST_PLUME, (SpriteParticleRegistration)(DustPlumeParticle.Provider::new));
      this.register(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER, (SpriteParticleRegistration)(TrialSpawnerDetectionParticle.Provider::new));
      this.register(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, (SpriteParticleRegistration)(TrialSpawnerDetectionParticle.Provider::new));
      this.register(ParticleTypes.VAULT_CONNECTION, (SpriteParticleRegistration)(FlyTowardsPositionParticle.VaultConnectionProvider::new));
      this.register(ParticleTypes.DUST_PILLAR, (ParticleProvider)(new TerrainParticle.DustPillarProvider()));
      this.register(ParticleTypes.RAID_OMEN, (SpriteParticleRegistration)(SpellParticle.Provider::new));
      this.register(ParticleTypes.TRIAL_OMEN, (SpriteParticleRegistration)(SpellParticle.Provider::new));
      this.register(ParticleTypes.OMINOUS_SPAWNING, (SpriteParticleRegistration)(FlyStraightTowardsParticle.OminousSpawnProvider::new));
      this.register(ParticleTypes.BLOCK_CRUMBLE, (ParticleProvider)(new TerrainParticle.CrumblingProvider()));
      this.register(ParticleTypes.FIREFLY, (SpriteParticleRegistration)(FireflyParticle.FireflyProvider::new));
   }

   private void register(final ParticleType type, final ParticleProvider provider) {
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(type), provider);
   }

   private void register(final ParticleType type, final SpriteParticleRegistration provider) {
      MutableSpriteSet spriteSet = new MutableSpriteSet();
      this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(type), spriteSet);
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(type), provider.create(spriteSet));
   }

   public CompletableFuture reload(final PreparableReloadListener.SharedState currentReload, final Executor taskExecutor, final PreparableReloadListener.PreparationBarrier preparationBarrier, final Executor reloadExecutor) {
      ResourceManager manager = currentReload.resourceManager();
      CompletableFuture<List<ParticleDefinition>> spriteSetsToLoad = CompletableFuture.supplyAsync(() -> PARTICLE_LISTER.listMatchingResources(manager), taskExecutor).thenCompose((definitionsToScan) -> {
         List<CompletableFuture<ParticleDefinition>> loadTasks = new ArrayList(definitionsToScan.size());
         definitionsToScan.forEach((resourceId, resource) -> {
            Identifier particleId = PARTICLE_LISTER.fileToId(resourceId);
            loadTasks.add(CompletableFuture.supplyAsync(() -> {
               record ParticleDefinition(Identifier id, Optional sprites) {
               }

               return new ParticleDefinition(particleId, this.loadParticleDescription(particleId, resource));
            }, taskExecutor));
         });
         return Util.sequence(loadTasks);
      });
      CompletableFuture<SpriteLoader.Preparations> pendingSprites = ((AtlasManager.PendingStitchResults)currentReload.get(AtlasManager.PENDING_STITCH)).get(AtlasIds.PARTICLES);
      CompletableFuture var10000 = CompletableFuture.allOf(spriteSetsToLoad, pendingSprites);
      Objects.requireNonNull(preparationBarrier);
      return var10000.thenCompose(preparationBarrier::wait).thenAcceptAsync((unused) -> {
         if (this.onReload != null) {
            this.onReload.run();
         }

         ProfilerFiller reloadProfiler = Profiler.get();
         reloadProfiler.push("upload");
         SpriteLoader.Preparations sprites = (SpriteLoader.Preparations)pendingSprites.join();
         reloadProfiler.popPush("bindSpriteSets");
         Set<Identifier> missingSprites = new HashSet();
         TextureAtlasSprite missingSprite = sprites.missing();
         ((List)spriteSetsToLoad.join()).forEach((p) -> {
            Optional<List<Identifier>> spriteIds = p.sprites();
            if (!spriteIds.isEmpty()) {
               List<TextureAtlasSprite> contents = new ArrayList();

               for(Identifier spriteId : (List)spriteIds.get()) {
                  TextureAtlasSprite sprite = sprites.getSprite(spriteId);
                  if (sprite == null) {
                     missingSprites.add(spriteId);
                     contents.add(missingSprite);
                  } else {
                     contents.add(sprite);
                  }
               }

               if (contents.isEmpty()) {
                  contents.add(missingSprite);
               }

               ((MutableSpriteSet)this.spriteSets.get(p.id())).rebind(contents);
            }
         });
         if (!missingSprites.isEmpty()) {
            LOGGER.warn("Missing particle sprites: {}", missingSprites.stream().sorted().map(Identifier::toString).collect(Collectors.joining(",")));
         }

         reloadProfiler.pop();
      }, reloadExecutor);
   }

   private Optional loadParticleDescription(final Identifier id, final Resource resource) {
      if (!this.spriteSets.containsKey(id)) {
         LOGGER.debug("Redundant texture list for particle: {}", id);
         return Optional.empty();
      } else {
         try {
            Reader reader = resource.openAsReader();

            Optional var5;
            try {
               ParticleDescription description = ParticleDescription.fromJson(GsonHelper.parse(reader));
               var5 = Optional.of(description.getTextures());
            } catch (Throwable var7) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (reader != null) {
               reader.close();
            }

            return var5;
         } catch (IOException e) {
            throw new IllegalStateException("Failed to load description for particle " + String.valueOf(id), e);
         }
      }
   }

   public Int2ObjectMap getProviders() {
      return this.providers;
   }

   private static class MutableSpriteSet implements SpriteSet {
      private List sprites;

      public TextureAtlasSprite get(final int index, final int max) {
         return (TextureAtlasSprite)this.sprites.get(index * (this.sprites.size() - 1) / max);
      }

      public TextureAtlasSprite get(final RandomSource random) {
         return (TextureAtlasSprite)this.sprites.get(random.nextInt(this.sprites.size()));
      }

      public TextureAtlasSprite first() {
         return (TextureAtlasSprite)this.sprites.getFirst();
      }

      public void rebind(final List ids) {
         this.sprites = ImmutableList.copyOf(ids);
      }
   }

   @FunctionalInterface
   private interface SpriteParticleRegistration {
      ParticleProvider create(SpriteSet spriteSet);
   }
}
