package net.minecraft.server.packs.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DynamicOps ops;
   private final Codec codec;
   private final FileToIdConverter lister;

   protected SimpleJsonResourceReloadListener(final HolderLookup.Provider registries, final Codec codec, final ResourceKey registryKey) {
      this((DynamicOps)registries.createSerializationContext(JsonOps.INSTANCE), codec, (FileToIdConverter)FileToIdConverter.registry(registryKey));
   }

   protected SimpleJsonResourceReloadListener(final Codec codec, final FileToIdConverter lister) {
      this((DynamicOps)JsonOps.INSTANCE, codec, (FileToIdConverter)lister);
   }

   private SimpleJsonResourceReloadListener(final DynamicOps ops, final Codec codec, final FileToIdConverter lister) {
      this.ops = ops;
      this.codec = codec;
      this.lister = lister;
   }

   protected Map prepare(final ResourceManager manager, final ProfilerFiller profiler) {
      Map<Identifier, T> result = new HashMap();
      scanDirectory(manager, this.lister, this.ops, this.codec, result);
      return result;
   }

   public static void scanDirectory(final ResourceManager manager, final ResourceKey registryKey, final DynamicOps ops, final Codec codec, final Map result) {
      scanDirectory(manager, FileToIdConverter.registry(registryKey), ops, codec, result);
   }

   public static void scanDirectory(final ResourceManager manager, final FileToIdConverter lister, final DynamicOps ops, final Codec codec, final Map result) {
      for(Map.Entry entry : lister.listMatchingResources(manager).entrySet()) {
         Identifier location = (Identifier)entry.getKey();
         Identifier id = lister.fileToId(location);

         try {
            Reader reader = ((Resource)entry.getValue()).openAsReader();

            try {
               codec.parse(ops, StrictJsonParser.parse(reader)).ifSuccess((parsed) -> {
                  if (result.putIfAbsent(id, parsed) != null) {
                     throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf(id));
                  }
               }).ifError((error) -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", new Object[]{id, location, error}));
            } catch (Throwable var13) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (IllegalArgumentException | IOException | JsonParseException e) {
            LOGGER.error("Couldn't parse data file '{}' from '{}'", new Object[]{id, location, e});
         }
      }

   }
}
