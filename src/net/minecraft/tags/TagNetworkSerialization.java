package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;

public class TagNetworkSerialization {
   public static Map serializeTagsToNetwork(final LayeredRegistryAccess registries) {
      return (Map)RegistrySynchronization.networkSafeRegistries(registries).map((e) -> Pair.of(e.key(), serializeToNetwork(e.value()))).filter((e) -> !((NetworkPayload)e.getSecond()).isEmpty()).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
   }

   private static NetworkPayload serializeToNetwork(final Registry registry) {
      Map<Identifier, IntList> result = new HashMap();
      registry.getTags().forEach((tag) -> {
         IntList ids = new IntArrayList(tag.size());

         for(Holder holder : tag) {
            if (holder.kind() != Holder.Kind.REFERENCE) {
               throw new IllegalStateException("Can't serialize unregistered value " + String.valueOf(holder));
            }

            ids.add(registry.getId(holder.value()));
         }

         result.put(tag.key().location(), ids);
      });
      return new NetworkPayload(result);
   }

   private static TagLoader.LoadResult deserializeTagsFromNetwork(final Registry registry, final NetworkPayload payload) {
      ResourceKey<? extends Registry<T>> registryKey = registry.key();
      Map<TagKey<T>, List<Holder<T>>> tags = new HashMap();
      payload.tags.forEach((key, ids) -> {
         TagKey<T> tagKey = TagKey.create(registryKey, key);
         IntStream var10000 = ids.intStream();
         Objects.requireNonNull(registry);
         List<Holder<T>> values = (List)var10000.mapToObj(registry::get).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
         tags.put(tagKey, values);
      });
      return new TagLoader.LoadResult(registryKey, tags);
   }

   public static final class NetworkPayload {
      public static final NetworkPayload EMPTY = new NetworkPayload(Map.of());
      private final Map tags;

      NetworkPayload(final Map tags) {
         this.tags = tags;
      }

      public void write(final FriendlyByteBuf buf) {
         buf.writeMap(this.tags, FriendlyByteBuf::writeIdentifier, FriendlyByteBuf::writeIntIdList);
      }

      public static NetworkPayload read(final FriendlyByteBuf buf) {
         return new NetworkPayload(buf.readMap(FriendlyByteBuf::readIdentifier, FriendlyByteBuf::readIntIdList));
      }

      public boolean isEmpty() {
         return this.tags.isEmpty();
      }

      public int size() {
         return this.tags.size();
      }

      public TagLoader.LoadResult resolve(final Registry registry) {
         return TagNetworkSerialization.deserializeTagsFromNetwork(registry, this);
      }
   }
}
