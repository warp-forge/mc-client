package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public record TelemetryProperty(String id, String exportKey, Codec codec, Exporter exporter) {
   private static final DateTimeFormatter TIMESTAMP_FORMATTER;
   public static final TelemetryProperty USER_ID;
   public static final TelemetryProperty CLIENT_ID;
   public static final TelemetryProperty MINECRAFT_SESSION_ID;
   public static final TelemetryProperty GAME_VERSION;
   public static final TelemetryProperty OPERATING_SYSTEM;
   public static final TelemetryProperty PLATFORM;
   public static final TelemetryProperty CLIENT_MODDED;
   public static final TelemetryProperty LAUNCHER_NAME;
   public static final TelemetryProperty WORLD_SESSION_ID;
   public static final TelemetryProperty SERVER_MODDED;
   public static final TelemetryProperty SERVER_TYPE;
   public static final TelemetryProperty OPT_IN;
   public static final TelemetryProperty EVENT_TIMESTAMP_UTC;
   public static final TelemetryProperty GAME_MODE;
   public static final TelemetryProperty REALMS_MAP_CONTENT;
   public static final TelemetryProperty SECONDS_SINCE_LOAD;
   public static final TelemetryProperty TICKS_SINCE_LOAD;
   public static final TelemetryProperty FRAME_RATE_SAMPLES;
   public static final TelemetryProperty RENDER_TIME_SAMPLES;
   public static final TelemetryProperty USED_MEMORY_SAMPLES;
   public static final TelemetryProperty NUMBER_OF_SAMPLES;
   public static final TelemetryProperty RENDER_DISTANCE;
   public static final TelemetryProperty DEDICATED_MEMORY_KB;
   public static final TelemetryProperty WORLD_LOAD_TIME_MS;
   public static final TelemetryProperty NEW_WORLD;
   public static final TelemetryProperty LOAD_TIME_TOTAL_TIME_MS;
   public static final TelemetryProperty LOAD_TIME_PRE_WINDOW_MS;
   public static final TelemetryProperty LOAD_TIME_BOOTSTRAP_MS;
   public static final TelemetryProperty LOAD_TIME_LOADING_OVERLAY_MS;
   public static final TelemetryProperty ADVANCEMENT_ID;
   public static final TelemetryProperty ADVANCEMENT_GAME_TIME;

   public static TelemetryProperty create(final String id, final String exportKey, final Codec codec, final Exporter exporter) {
      return new TelemetryProperty(id, exportKey, codec, exporter);
   }

   public static TelemetryProperty bool(final String id, final String exportKey) {
      return create(id, exportKey, Codec.BOOL, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty string(final String id, final String exportKey) {
      return create(id, exportKey, Codec.STRING, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty integer(final String id, final String exportKey) {
      return create(id, exportKey, Codec.INT, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty makeLong(final String id, final String exportKey) {
      return create(id, exportKey, Codec.LONG, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty uuid(final String id, final String exportKey) {
      return create(id, exportKey, UUIDUtil.STRING_CODEC, (output, key, value) -> output.addProperty(key, value.toString()));
   }

   public static TelemetryProperty gameLoadMeasurement(final String id, final String exportKey) {
      return create(id, exportKey, GameLoadTimesEvent.Measurement.CODEC, (output, key, value) -> output.addProperty(key, value.millis()));
   }

   public static TelemetryProperty longSamples(final String id, final String exportKey) {
      return create(id, exportKey, Codec.LONG.listOf().xmap(LongArrayList::new, Function.identity()), (output, key, value) -> output.addProperty(key, (String)value.longStream().mapToObj(String::valueOf).collect(Collectors.joining(";"))));
   }

   public void export(final TelemetryPropertyMap input, final TelemetryPropertyContainer output) {
      T value = (T)input.get(this);
      if (value != null) {
         this.exporter.apply(output, this.exportKey, value);
      } else {
         output.addNullProperty(this.exportKey);
      }

   }

   public MutableComponent title() {
      return Component.translatable("telemetry.property." + this.id + ".title");
   }

   public String toString() {
      return "TelemetryProperty[" + this.id + "]";
   }

   static {
      TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
      USER_ID = string("user_id", "userId");
      CLIENT_ID = string("client_id", "clientId");
      MINECRAFT_SESSION_ID = uuid("minecraft_session_id", "deviceSessionId");
      GAME_VERSION = string("game_version", "buildDisplayName");
      OPERATING_SYSTEM = string("operating_system", "buildPlatform");
      PLATFORM = string("platform", "platform");
      CLIENT_MODDED = bool("client_modded", "clientModded");
      LAUNCHER_NAME = string("launcher_name", "launcherName");
      WORLD_SESSION_ID = uuid("world_session_id", "worldSessionId");
      SERVER_MODDED = bool("server_modded", "serverModded");
      SERVER_TYPE = create("server_type", "serverType", TelemetryProperty.ServerType.CODEC, (output, key, value) -> output.addProperty(key, value.getSerializedName()));
      OPT_IN = bool("opt_in", "isOptional");
      EVENT_TIMESTAMP_UTC = create("event_timestamp_utc", "eventTimestampUtc", ExtraCodecs.INSTANT_ISO8601, (output, key, value) -> output.addProperty(key, TIMESTAMP_FORMATTER.format(value)));
      GAME_MODE = create("game_mode", "playerGameMode", TelemetryProperty.GameMode.CODEC, (output, key, value) -> output.addProperty(key, value.id()));
      REALMS_MAP_CONTENT = string("realms_map_content", "realmsMapContent");
      SECONDS_SINCE_LOAD = integer("seconds_since_load", "secondsSinceLoad");
      TICKS_SINCE_LOAD = integer("ticks_since_load", "ticksSinceLoad");
      FRAME_RATE_SAMPLES = longSamples("frame_rate_samples", "serializedFpsSamples");
      RENDER_TIME_SAMPLES = longSamples("render_time_samples", "serializedRenderTimeSamples");
      USED_MEMORY_SAMPLES = longSamples("used_memory_samples", "serializedUsedMemoryKbSamples");
      NUMBER_OF_SAMPLES = integer("number_of_samples", "numSamples");
      RENDER_DISTANCE = integer("render_distance", "renderDistance");
      DEDICATED_MEMORY_KB = integer("dedicated_memory_kb", "dedicatedMemoryKb");
      WORLD_LOAD_TIME_MS = integer("world_load_time_ms", "worldLoadTimeMs");
      NEW_WORLD = bool("new_world", "newWorld");
      LOAD_TIME_TOTAL_TIME_MS = gameLoadMeasurement("load_time_total_time_ms", "loadTimeTotalTimeMs");
      LOAD_TIME_PRE_WINDOW_MS = gameLoadMeasurement("load_time_pre_window_ms", "loadTimePreWindowMs");
      LOAD_TIME_BOOTSTRAP_MS = gameLoadMeasurement("load_time_bootstrap_ms", "loadTimeBootstrapMs");
      LOAD_TIME_LOADING_OVERLAY_MS = gameLoadMeasurement("load_time_loading_overlay_ms", "loadTimeLoadingOverlayMs");
      ADVANCEMENT_ID = string("advancement_id", "advancementId");
      ADVANCEMENT_GAME_TIME = makeLong("advancement_game_time", "advancementGameTime");
   }

   public static enum ServerType implements StringRepresentable {
      REALM("realm"),
      LOCAL("local"),
      OTHER("server");

      public static final Codec CODEC = StringRepresentable.fromEnum(ServerType::values);
      private final String key;

      private ServerType(final String key) {
         this.key = key;
      }

      public String getSerializedName() {
         return this.key;
      }

      // $FF: synthetic method
      private static ServerType[] $values() {
         return new ServerType[]{REALM, LOCAL, OTHER};
      }
   }

   public static enum GameMode implements StringRepresentable {
      SURVIVAL("survival", 0),
      CREATIVE("creative", 1),
      ADVENTURE("adventure", 2),
      SPECTATOR("spectator", 6),
      HARDCORE("hardcore", 99);

      public static final Codec CODEC = StringRepresentable.fromEnum(GameMode::values);
      private final String key;
      private final int id;

      private GameMode(final String key, final int id) {
         this.key = key;
         this.id = id;
      }

      public int id() {
         return this.id;
      }

      public String getSerializedName() {
         return this.key;
      }

      // $FF: synthetic method
      private static GameMode[] $values() {
         return new GameMode[]{SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR, HARDCORE};
      }
   }

   public interface Exporter {
      void apply(TelemetryPropertyContainer output, String key, Object value);
   }
}
