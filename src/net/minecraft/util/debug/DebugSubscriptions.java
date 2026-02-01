package net.minecraft.util.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.redstone.Orientation;

public class DebugSubscriptions {
   public static final DebugSubscription DEDICATED_SERVER_TICK_TIME = registerSimple("dedicated_server_tick_time");
   public static final DebugSubscription BEES;
   public static final DebugSubscription BRAINS;
   public static final DebugSubscription BREEZES;
   public static final DebugSubscription GOAL_SELECTORS;
   public static final DebugSubscription ENTITY_PATHS;
   public static final DebugSubscription ENTITY_BLOCK_INTERSECTIONS;
   public static final DebugSubscription BEE_HIVES;
   public static final DebugSubscription POIS;
   public static final DebugSubscription REDSTONE_WIRE_ORIENTATIONS;
   public static final DebugSubscription VILLAGE_SECTIONS;
   public static final DebugSubscription RAIDS;
   public static final DebugSubscription STRUCTURES;
   public static final DebugSubscription GAME_EVENT_LISTENERS;
   public static final DebugSubscription NEIGHBOR_UPDATES;
   public static final DebugSubscription GAME_EVENTS;

   public static DebugSubscription bootstrap(final Registry registry) {
      return DEDICATED_SERVER_TICK_TIME;
   }

   private static DebugSubscription registerSimple(final String id) {
      return (DebugSubscription)Registry.register(BuiltInRegistries.DEBUG_SUBSCRIPTION, (Identifier)Identifier.withDefaultNamespace(id), new DebugSubscription((StreamCodec)null));
   }

   private static DebugSubscription registerWithValue(final String id, final StreamCodec valueStreamCodec) {
      return (DebugSubscription)Registry.register(BuiltInRegistries.DEBUG_SUBSCRIPTION, (Identifier)Identifier.withDefaultNamespace(id), new DebugSubscription(valueStreamCodec));
   }

   private static DebugSubscription registerTemporaryValue(final String id, final StreamCodec valueStreamCodec, final int expireAfterTicks) {
      return (DebugSubscription)Registry.register(BuiltInRegistries.DEBUG_SUBSCRIPTION, (Identifier)Identifier.withDefaultNamespace(id), new DebugSubscription(valueStreamCodec, expireAfterTicks));
   }

   static {
      BEES = registerWithValue("bees", DebugBeeInfo.STREAM_CODEC);
      BRAINS = registerWithValue("brains", DebugBrainDump.STREAM_CODEC);
      BREEZES = registerWithValue("breezes", DebugBreezeInfo.STREAM_CODEC);
      GOAL_SELECTORS = registerWithValue("goal_selectors", DebugGoalInfo.STREAM_CODEC);
      ENTITY_PATHS = registerWithValue("entity_paths", DebugPathInfo.STREAM_CODEC);
      ENTITY_BLOCK_INTERSECTIONS = registerTemporaryValue("entity_block_intersections", DebugEntityBlockIntersection.STREAM_CODEC, 100);
      BEE_HIVES = registerWithValue("bee_hives", DebugHiveInfo.STREAM_CODEC);
      POIS = registerWithValue("pois", DebugPoiInfo.STREAM_CODEC);
      REDSTONE_WIRE_ORIENTATIONS = registerTemporaryValue("redstone_wire_orientations", Orientation.STREAM_CODEC, 200);
      VILLAGE_SECTIONS = registerWithValue("village_sections", Unit.STREAM_CODEC);
      RAIDS = registerWithValue("raids", BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()));
      STRUCTURES = registerWithValue("structures", DebugStructureInfo.STREAM_CODEC.apply(ByteBufCodecs.list()));
      GAME_EVENT_LISTENERS = registerWithValue("game_event_listeners", DebugGameEventListenerInfo.STREAM_CODEC);
      NEIGHBOR_UPDATES = registerTemporaryValue("neighbor_updates", BlockPos.STREAM_CODEC, 200);
      GAME_EVENTS = registerTemporaryValue("game_events", DebugGameEventInfo.STREAM_CODEC, 60);
   }
}
