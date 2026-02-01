package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.repository.KnownPack;

public class RegistrySynchronization {
   private static final Set NETWORKABLE_REGISTRIES;

   public static void packRegistries(final DynamicOps ops, final RegistryAccess registries, final Set clientKnownPacks, final BiConsumer output) {
      RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach((registryEntry) -> packRegistry(ops, registryEntry, registries, clientKnownPacks, output));
   }

   private static void packRegistry(final DynamicOps ops, final RegistryDataLoader.RegistryData registryData, final RegistryAccess registries, final Set clientKnownPacks, final BiConsumer output) {
      registries.lookup(registryData.key()).ifPresent((registry) -> {
         List<PackedRegistryEntry> packedElements = new ArrayList(registry.size());
         registry.listElements().forEach((element) -> {
            Optional var10000 = registry.registrationInfo(element.key()).flatMap(RegistrationInfo::knownPackInfo);
            Objects.requireNonNull(clientKnownPacks);
            boolean canSkipContents = var10000.filter(clientKnownPacks::contains).isPresent();
            Optional<Tag> contents;
            if (canSkipContents) {
               contents = Optional.empty();
            } else {
               Tag encodedElement = (Tag)registryData.elementCodec().encodeStart(ops, element.value()).getOrThrow((s) -> {
                  String var10002 = String.valueOf(element.key());
                  return new IllegalArgumentException("Failed to serialize " + var10002 + ": " + s);
               });
               contents = Optional.of(encodedElement);
            }

            packedElements.add(new PackedRegistryEntry(element.key().identifier(), contents));
         });
         output.accept(registry.key(), packedElements);
      });
   }

   private static Stream ownedNetworkableRegistries(final RegistryAccess access) {
      return access.registries().filter((e) -> isNetworkable(e.key()));
   }

   public static Stream networkedRegistries(final LayeredRegistryAccess registries) {
      return ownedNetworkableRegistries(registries.getAccessFrom(RegistryLayer.WORLDGEN));
   }

   public static Stream networkSafeRegistries(final LayeredRegistryAccess registries) {
      Stream<RegistryAccess.RegistryEntry<?>> staticRegistries = registries.getLayer(RegistryLayer.STATIC).registries();
      Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries = networkedRegistries(registries);
      return Stream.concat(networkedRegistries, staticRegistries);
   }

   public static boolean isNetworkable(final ResourceKey key) {
      return NETWORKABLE_REGISTRIES.contains(key);
   }

   static {
      NETWORKABLE_REGISTRIES = (Set)RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream().map(RegistryDataLoader.RegistryData::key).collect(Collectors.toUnmodifiableSet());
   }

   public static record PackedRegistryEntry(Identifier id, Optional data) {
      public static final StreamCodec STREAM_CODEC;

      static {
         STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, PackedRegistryEntry::id, ByteBufCodecs.TAG.apply(ByteBufCodecs::optional), PackedRegistryEntry::data, PackedRegistryEntry::new);
      }
   }
}
