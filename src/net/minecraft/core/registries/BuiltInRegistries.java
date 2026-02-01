package net.minecraft.core.registries;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dialog.DialogTypes;
import net.minecraft.server.dialog.action.ActionTypes;
import net.minecraft.server.dialog.body.DialogBodyTypes;
import net.minecraft.server.dialog.input.InputControlTypes;
import net.minecraft.server.jsonrpc.IncomingRpcMethods;
import net.minecraft.server.jsonrpc.OutgoingRpcMethods;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.permissions.PermissionCheckTypes;
import net.minecraft.server.permissions.PermissionTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Util;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.variant.SpawnConditions;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeSerializers;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplays;
import net.minecraft.world.item.crafting.display.SlotDisplays;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProviderTypes;
import net.minecraft.world.item.slot.SlotSources;
import net.minecraft.world.level.biome.BiomeSources;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.chunk.ChunkGenerators;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBindings;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import org.slf4j.Logger;

public class BuiltInRegistries {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map LOADERS = Maps.newLinkedHashMap();
   private static final WritableRegistry WRITABLE_REGISTRY;
   public static final DataComponentInitializers DATA_COMPONENT_INITIALIZERS;
   public static final DefaultedRegistry GAME_EVENT;
   public static final Registry SOUND_EVENT;
   public static final DefaultedRegistry FLUID;
   public static final Registry MOB_EFFECT;
   public static final DefaultedRegistry BLOCK;
   public static final Registry DEBUG_SUBSCRIPTION;
   public static final DefaultedRegistry ENTITY_TYPE;
   public static final DefaultedRegistry ITEM;
   public static final Registry POTION;
   public static final Registry PARTICLE_TYPE;
   public static final Registry BLOCK_ENTITY_TYPE;
   public static final Registry CUSTOM_STAT;
   public static final DefaultedRegistry CHUNK_STATUS;
   public static final Registry RULE_TEST;
   public static final Registry RULE_BLOCK_ENTITY_MODIFIER;
   public static final Registry POS_RULE_TEST;
   public static final Registry MENU;
   public static final Registry RECIPE_TYPE;
   public static final Registry RECIPE_SERIALIZER;
   public static final Registry ATTRIBUTE;
   public static final Registry POSITION_SOURCE_TYPE;
   public static final Registry COMMAND_ARGUMENT_TYPE;
   public static final Registry STAT_TYPE;
   public static final DefaultedRegistry VILLAGER_TYPE;
   public static final DefaultedRegistry VILLAGER_PROFESSION;
   public static final Registry POINT_OF_INTEREST_TYPE;
   public static final DefaultedRegistry MEMORY_MODULE_TYPE;
   public static final DefaultedRegistry SENSOR_TYPE;
   public static final Registry ACTIVITY;
   public static final Registry LOOT_POOL_ENTRY_TYPE;
   public static final Registry LOOT_FUNCTION_TYPE;
   public static final Registry LOOT_CONDITION_TYPE;
   public static final Registry LOOT_NUMBER_PROVIDER_TYPE;
   public static final Registry LOOT_NBT_PROVIDER_TYPE;
   public static final Registry LOOT_SCORE_PROVIDER_TYPE;
   public static final Registry FLOAT_PROVIDER_TYPE;
   public static final Registry INT_PROVIDER_TYPE;
   public static final Registry HEIGHT_PROVIDER_TYPE;
   public static final Registry BLOCK_PREDICATE_TYPE;
   public static final Registry CARVER;
   public static final Registry FEATURE;
   public static final Registry STRUCTURE_PLACEMENT;
   public static final Registry STRUCTURE_PIECE;
   public static final Registry STRUCTURE_TYPE;
   public static final Registry PLACEMENT_MODIFIER_TYPE;
   public static final Registry BLOCKSTATE_PROVIDER_TYPE;
   public static final Registry FOLIAGE_PLACER_TYPE;
   public static final Registry TRUNK_PLACER_TYPE;
   public static final Registry ROOT_PLACER_TYPE;
   public static final Registry TREE_DECORATOR_TYPE;
   public static final Registry FEATURE_SIZE_TYPE;
   public static final Registry BIOME_SOURCE;
   public static final Registry CHUNK_GENERATOR;
   public static final Registry MATERIAL_CONDITION;
   public static final Registry MATERIAL_RULE;
   public static final Registry DENSITY_FUNCTION_TYPE;
   public static final Registry BLOCK_TYPE;
   public static final Registry STRUCTURE_PROCESSOR;
   public static final Registry STRUCTURE_POOL_ELEMENT;
   public static final Registry POOL_ALIAS_BINDING_TYPE;
   public static final Registry DECORATED_POT_PATTERN;
   public static final Registry CREATIVE_MODE_TAB;
   public static final Registry TRIGGER_TYPES;
   public static final Registry NUMBER_FORMAT_TYPE;
   public static final Registry DATA_COMPONENT_TYPE;
   public static final Registry GAME_RULE;
   public static final Registry ENTITY_SUB_PREDICATE_TYPE;
   public static final Registry DATA_COMPONENT_PREDICATE_TYPE;
   public static final Registry MAP_DECORATION_TYPE;
   public static final Registry ENCHANTMENT_EFFECT_COMPONENT_TYPE;
   public static final Registry ENCHANTMENT_LEVEL_BASED_VALUE_TYPE;
   public static final Registry ENCHANTMENT_ENTITY_EFFECT_TYPE;
   public static final Registry ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE;
   public static final Registry ENCHANTMENT_VALUE_EFFECT_TYPE;
   public static final Registry ENCHANTMENT_PROVIDER_TYPE;
   public static final Registry CONSUME_EFFECT_TYPE;
   public static final Registry RECIPE_DISPLAY;
   public static final Registry SLOT_DISPLAY;
   public static final Registry RECIPE_BOOK_CATEGORY;
   public static final Registry TICKET_TYPE;
   public static final Registry INCOMING_RPC_METHOD;
   public static final Registry OUTGOING_RPC_METHOD;
   public static final Registry TEST_ENVIRONMENT_DEFINITION_TYPE;
   public static final Registry TEST_INSTANCE_TYPE;
   public static final Registry SPAWN_CONDITION_TYPE;
   public static final Registry DIALOG_TYPE;
   public static final Registry DIALOG_ACTION_TYPE;
   public static final Registry INPUT_CONTROL_TYPE;
   public static final Registry DIALOG_BODY_TYPE;
   public static final Registry PERMISSION_TYPE;
   public static final Registry PERMISSION_CHECK_TYPE;
   public static final Registry ENVIRONMENT_ATTRIBUTE;
   public static final Registry ATTRIBUTE_TYPE;
   public static final Registry SLOT_SOURCE_TYPE;
   public static final Registry TEST_FUNCTION;
   public static final Registry REGISTRY;

   private static Registry registerSimple(final ResourceKey name, final RegistryBootstrap loader) {
      return internalRegister(name, new MappedRegistry(name, Lifecycle.stable(), false), loader);
   }

   private static Registry registerSimpleWithIntrusiveHolders(final ResourceKey name, final RegistryBootstrap loader) {
      return internalRegister(name, new MappedRegistry(name, Lifecycle.stable(), true), loader);
   }

   private static DefaultedRegistry registerDefaulted(final ResourceKey name, final String defaultKey, final RegistryBootstrap loader) {
      return (DefaultedRegistry)internalRegister(name, new DefaultedMappedRegistry(defaultKey, name, Lifecycle.stable(), false), loader);
   }

   private static DefaultedRegistry registerDefaultedWithIntrusiveHolders(final ResourceKey name, final String defaultKey, final RegistryBootstrap loader) {
      return (DefaultedRegistry)internalRegister(name, new DefaultedMappedRegistry(defaultKey, name, Lifecycle.stable(), true), loader);
   }

   private static WritableRegistry internalRegister(final ResourceKey name, final WritableRegistry registry, final RegistryBootstrap loader) {
      Bootstrap.checkBootstrapCalled(() -> "registry " + String.valueOf(name.identifier()));
      Identifier key = name.identifier();
      LOADERS.put(key, (Supplier)() -> loader.run(registry));
      WRITABLE_REGISTRY.register(name, registry, RegistrationInfo.BUILT_IN);
      return registry;
   }

   public static void bootStrap() {
      createContents();
      freeze();
      validate(REGISTRY);
   }

   private static void createContents() {
      LOADERS.forEach((key, value) -> {
         if (value.get() == null) {
            LOGGER.error("Unable to bootstrap registry '{}'", key);
         }

      });
   }

   private static void freeze() {
      REGISTRY.freeze();

      for(Registry registry : REGISTRY) {
         bindBootstrappedTagsToEmpty(registry);
         registry.freeze();
      }

   }

   private static void validate(final Registry registry) {
      registry.forEach((r) -> {
         if (r.keySet().isEmpty()) {
            Identifier var10000 = registry.getKey(r);
            Util.logAndPauseIfInIde("Registry '" + String.valueOf(var10000) + "' was empty after loading");
         }

         if (r instanceof DefaultedRegistry) {
            Identifier key = ((DefaultedRegistry)r).getDefaultKey();
            Objects.requireNonNull(r.getValue(key), "Missing default of DefaultedMappedRegistry: " + String.valueOf(key));
         }

      });
   }

   public static HolderGetter acquireBootstrapRegistrationLookup(final Registry registry) {
      return ((WritableRegistry)registry).createRegistrationLookup();
   }

   private static void bindBootstrappedTagsToEmpty(final Registry registry) {
      ((MappedRegistry)registry).bindAllTagsToEmpty();
   }

   static {
      WRITABLE_REGISTRY = new MappedRegistry(ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME), Lifecycle.stable());
      DATA_COMPONENT_INITIALIZERS = new DataComponentInitializers();
      GAME_EVENT = registerDefaulted(Registries.GAME_EVENT, "step", GameEvent::bootstrap);
      SOUND_EVENT = registerSimple(Registries.SOUND_EVENT, (registry) -> SoundEvents.ITEM_PICKUP);
      FLUID = registerDefaultedWithIntrusiveHolders(Registries.FLUID, "empty", (registry) -> Fluids.EMPTY);
      MOB_EFFECT = registerSimple(Registries.MOB_EFFECT, MobEffects::bootstrap);
      BLOCK = registerDefaultedWithIntrusiveHolders(Registries.BLOCK, "air", (registry) -> Blocks.AIR);
      DEBUG_SUBSCRIPTION = registerSimple(Registries.DEBUG_SUBSCRIPTION, DebugSubscriptions::bootstrap);
      ENTITY_TYPE = registerDefaultedWithIntrusiveHolders(Registries.ENTITY_TYPE, "pig", (registry) -> EntityType.PIG);
      ITEM = registerDefaultedWithIntrusiveHolders(Registries.ITEM, "air", (registry) -> Items.AIR);
      POTION = registerSimple(Registries.POTION, Potions::bootstrap);
      PARTICLE_TYPE = registerSimple(Registries.PARTICLE_TYPE, (registry) -> ParticleTypes.BLOCK);
      BLOCK_ENTITY_TYPE = registerSimpleWithIntrusiveHolders(Registries.BLOCK_ENTITY_TYPE, (registry) -> BlockEntityType.FURNACE);
      CUSTOM_STAT = registerSimple(Registries.CUSTOM_STAT, (registry) -> Stats.JUMP);
      CHUNK_STATUS = registerDefaulted(Registries.CHUNK_STATUS, "empty", (registry) -> ChunkStatus.EMPTY);
      RULE_TEST = registerSimple(Registries.RULE_TEST, (registry) -> RuleTestType.ALWAYS_TRUE_TEST);
      RULE_BLOCK_ENTITY_MODIFIER = registerSimple(Registries.RULE_BLOCK_ENTITY_MODIFIER, (registry) -> RuleBlockEntityModifierType.PASSTHROUGH);
      POS_RULE_TEST = registerSimple(Registries.POS_RULE_TEST, (registry) -> PosRuleTestType.ALWAYS_TRUE_TEST);
      MENU = registerSimple(Registries.MENU, (registry) -> MenuType.ANVIL);
      RECIPE_TYPE = registerSimple(Registries.RECIPE_TYPE, (registry) -> RecipeType.CRAFTING);
      RECIPE_SERIALIZER = registerSimple(Registries.RECIPE_SERIALIZER, RecipeSerializers::bootstrap);
      ATTRIBUTE = registerSimple(Registries.ATTRIBUTE, Attributes::bootstrap);
      POSITION_SOURCE_TYPE = registerSimple(Registries.POSITION_SOURCE_TYPE, (registry) -> PositionSourceType.BLOCK);
      COMMAND_ARGUMENT_TYPE = registerSimple(Registries.COMMAND_ARGUMENT_TYPE, ArgumentTypeInfos::bootstrap);
      STAT_TYPE = registerSimple(Registries.STAT_TYPE, (registry) -> Stats.ITEM_USED);
      VILLAGER_TYPE = registerDefaulted(Registries.VILLAGER_TYPE, "plains", VillagerType::bootstrap);
      VILLAGER_PROFESSION = registerDefaulted(Registries.VILLAGER_PROFESSION, "none", VillagerProfession::bootstrap);
      POINT_OF_INTEREST_TYPE = registerSimple(Registries.POINT_OF_INTEREST_TYPE, PoiTypes::bootstrap);
      MEMORY_MODULE_TYPE = registerDefaulted(Registries.MEMORY_MODULE_TYPE, "dummy", (registry) -> MemoryModuleType.DUMMY);
      SENSOR_TYPE = registerDefaulted(Registries.SENSOR_TYPE, "dummy", (registry) -> SensorType.DUMMY);
      ACTIVITY = registerSimple(Registries.ACTIVITY, (registry) -> Activity.IDLE);
      LOOT_POOL_ENTRY_TYPE = registerSimple(Registries.LOOT_POOL_ENTRY_TYPE, LootPoolEntries::bootstrap);
      LOOT_FUNCTION_TYPE = registerSimple(Registries.LOOT_FUNCTION_TYPE, LootItemFunctions::bootstrap);
      LOOT_CONDITION_TYPE = registerSimple(Registries.LOOT_CONDITION_TYPE, LootItemConditions::bootstrap);
      LOOT_NUMBER_PROVIDER_TYPE = registerSimple(Registries.LOOT_NUMBER_PROVIDER_TYPE, NumberProviders::bootstrap);
      LOOT_NBT_PROVIDER_TYPE = registerSimple(Registries.LOOT_NBT_PROVIDER_TYPE, NbtProviders::bootstrap);
      LOOT_SCORE_PROVIDER_TYPE = registerSimple(Registries.LOOT_SCORE_PROVIDER_TYPE, ScoreboardNameProviders::bootstrap);
      FLOAT_PROVIDER_TYPE = registerSimple(Registries.FLOAT_PROVIDER_TYPE, (registry) -> FloatProviderType.CONSTANT);
      INT_PROVIDER_TYPE = registerSimple(Registries.INT_PROVIDER_TYPE, (registry) -> IntProviderType.CONSTANT);
      HEIGHT_PROVIDER_TYPE = registerSimple(Registries.HEIGHT_PROVIDER_TYPE, (registry) -> HeightProviderType.CONSTANT);
      BLOCK_PREDICATE_TYPE = registerSimple(Registries.BLOCK_PREDICATE_TYPE, (registry) -> BlockPredicateType.NOT);
      CARVER = registerSimple(Registries.CARVER, (registry) -> WorldCarver.CAVE);
      FEATURE = registerSimple(Registries.FEATURE, (registry) -> Feature.ORE);
      STRUCTURE_PLACEMENT = registerSimple(Registries.STRUCTURE_PLACEMENT, (registry) -> StructurePlacementType.RANDOM_SPREAD);
      STRUCTURE_PIECE = registerSimple(Registries.STRUCTURE_PIECE, (registry) -> StructurePieceType.MINE_SHAFT_ROOM);
      STRUCTURE_TYPE = registerSimple(Registries.STRUCTURE_TYPE, (registry) -> StructureType.JIGSAW);
      PLACEMENT_MODIFIER_TYPE = registerSimple(Registries.PLACEMENT_MODIFIER_TYPE, (registry) -> PlacementModifierType.COUNT);
      BLOCKSTATE_PROVIDER_TYPE = registerSimple(Registries.BLOCK_STATE_PROVIDER_TYPE, (registry) -> BlockStateProviderType.SIMPLE_STATE_PROVIDER);
      FOLIAGE_PLACER_TYPE = registerSimple(Registries.FOLIAGE_PLACER_TYPE, (registry) -> FoliagePlacerType.BLOB_FOLIAGE_PLACER);
      TRUNK_PLACER_TYPE = registerSimple(Registries.TRUNK_PLACER_TYPE, (registry) -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
      ROOT_PLACER_TYPE = registerSimple(Registries.ROOT_PLACER_TYPE, (registry) -> RootPlacerType.MANGROVE_ROOT_PLACER);
      TREE_DECORATOR_TYPE = registerSimple(Registries.TREE_DECORATOR_TYPE, (registry) -> TreeDecoratorType.LEAVE_VINE);
      FEATURE_SIZE_TYPE = registerSimple(Registries.FEATURE_SIZE_TYPE, (registry) -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE);
      BIOME_SOURCE = registerSimple(Registries.BIOME_SOURCE, BiomeSources::bootstrap);
      CHUNK_GENERATOR = registerSimple(Registries.CHUNK_GENERATOR, ChunkGenerators::bootstrap);
      MATERIAL_CONDITION = registerSimple(Registries.MATERIAL_CONDITION, SurfaceRules.ConditionSource::bootstrap);
      MATERIAL_RULE = registerSimple(Registries.MATERIAL_RULE, SurfaceRules.RuleSource::bootstrap);
      DENSITY_FUNCTION_TYPE = registerSimple(Registries.DENSITY_FUNCTION_TYPE, DensityFunctions::bootstrap);
      BLOCK_TYPE = registerSimple(Registries.BLOCK_TYPE, BlockTypes::bootstrap);
      STRUCTURE_PROCESSOR = registerSimple(Registries.STRUCTURE_PROCESSOR, (registry) -> StructureProcessorType.BLOCK_IGNORE);
      STRUCTURE_POOL_ELEMENT = registerSimple(Registries.STRUCTURE_POOL_ELEMENT, (registry) -> StructurePoolElementType.EMPTY);
      POOL_ALIAS_BINDING_TYPE = registerSimple(Registries.POOL_ALIAS_BINDING, PoolAliasBindings::bootstrap);
      DECORATED_POT_PATTERN = registerSimple(Registries.DECORATED_POT_PATTERN, DecoratedPotPatterns::bootstrap);
      CREATIVE_MODE_TAB = registerSimple(Registries.CREATIVE_MODE_TAB, CreativeModeTabs::bootstrap);
      TRIGGER_TYPES = registerSimple(Registries.TRIGGER_TYPE, CriteriaTriggers::bootstrap);
      NUMBER_FORMAT_TYPE = registerSimple(Registries.NUMBER_FORMAT_TYPE, NumberFormatTypes::bootstrap);
      DATA_COMPONENT_TYPE = registerSimple(Registries.DATA_COMPONENT_TYPE, DataComponents::bootstrap);
      GAME_RULE = registerSimple(Registries.GAME_RULE, GameRules::bootstrap);
      ENTITY_SUB_PREDICATE_TYPE = registerSimple(Registries.ENTITY_SUB_PREDICATE_TYPE, EntitySubPredicates::bootstrap);
      DATA_COMPONENT_PREDICATE_TYPE = registerSimple(Registries.DATA_COMPONENT_PREDICATE_TYPE, DataComponentPredicates::bootstrap);
      MAP_DECORATION_TYPE = registerSimple(Registries.MAP_DECORATION_TYPE, MapDecorationTypes::bootstrap);
      ENCHANTMENT_EFFECT_COMPONENT_TYPE = registerSimple(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, EnchantmentEffectComponents::bootstrap);
      ENCHANTMENT_LEVEL_BASED_VALUE_TYPE = registerSimple(Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, LevelBasedValue::bootstrap);
      ENCHANTMENT_ENTITY_EFFECT_TYPE = registerSimple(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, EnchantmentEntityEffect::bootstrap);
      ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE = registerSimple(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, EnchantmentLocationBasedEffect::bootstrap);
      ENCHANTMENT_VALUE_EFFECT_TYPE = registerSimple(Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, EnchantmentValueEffect::bootstrap);
      ENCHANTMENT_PROVIDER_TYPE = registerSimple(Registries.ENCHANTMENT_PROVIDER_TYPE, EnchantmentProviderTypes::bootstrap);
      CONSUME_EFFECT_TYPE = registerSimple(Registries.CONSUME_EFFECT_TYPE, (registry) -> ConsumeEffect.Type.APPLY_EFFECTS);
      RECIPE_DISPLAY = registerSimple(Registries.RECIPE_DISPLAY, RecipeDisplays::bootstrap);
      SLOT_DISPLAY = registerSimple(Registries.SLOT_DISPLAY, SlotDisplays::bootstrap);
      RECIPE_BOOK_CATEGORY = registerSimple(Registries.RECIPE_BOOK_CATEGORY, RecipeBookCategories::bootstrap);
      TICKET_TYPE = registerSimple(Registries.TICKET_TYPE, (registry) -> TicketType.UNKNOWN);
      INCOMING_RPC_METHOD = registerSimple(Registries.INCOMING_RPC_METHOD, IncomingRpcMethods::bootstrap);
      OUTGOING_RPC_METHOD = registerSimple(Registries.OUTGOING_RPC_METHOD, (registry) -> OutgoingRpcMethods.SERVER_STARTED);
      TEST_ENVIRONMENT_DEFINITION_TYPE = registerSimple(Registries.TEST_ENVIRONMENT_DEFINITION_TYPE, TestEnvironmentDefinition::bootstrap);
      TEST_INSTANCE_TYPE = registerSimple(Registries.TEST_INSTANCE_TYPE, GameTestInstance::bootstrap);
      SPAWN_CONDITION_TYPE = registerSimple(Registries.SPAWN_CONDITION_TYPE, SpawnConditions::bootstrap);
      DIALOG_TYPE = registerSimple(Registries.DIALOG_TYPE, DialogTypes::bootstrap);
      DIALOG_ACTION_TYPE = registerSimple(Registries.DIALOG_ACTION_TYPE, ActionTypes::bootstrap);
      INPUT_CONTROL_TYPE = registerSimple(Registries.INPUT_CONTROL_TYPE, InputControlTypes::bootstrap);
      DIALOG_BODY_TYPE = registerSimple(Registries.DIALOG_BODY_TYPE, DialogBodyTypes::bootstrap);
      PERMISSION_TYPE = registerSimple(Registries.PERMISSION_TYPE, PermissionTypes::bootstrap);
      PERMISSION_CHECK_TYPE = registerSimple(Registries.PERMISSION_CHECK_TYPE, PermissionCheckTypes::bootstrap);
      ENVIRONMENT_ATTRIBUTE = registerSimple(Registries.ENVIRONMENT_ATTRIBUTE, EnvironmentAttributes::bootstrap);
      ATTRIBUTE_TYPE = registerSimple(Registries.ATTRIBUTE_TYPE, AttributeTypes::bootstrap);
      SLOT_SOURCE_TYPE = registerSimple(Registries.SLOT_SOURCE_TYPE, SlotSources::bootstrap);
      TEST_FUNCTION = registerSimple(Registries.TEST_FUNCTION, BuiltinTestFunctions::bootstrap);
      REGISTRY = WRITABLE_REGISTRY;
   }

   @FunctionalInterface
   private interface RegistryBootstrap {
      Object run(Registry registry);
   }
}
