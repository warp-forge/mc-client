package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec implements Codec {
   private final ResourceKey registryKey;
   private final Codec elementCodec;
   private final Codec homogenousListCodec;
   private final Codec registryAwareCodec;

   private static Codec homogenousList(final Codec elementCodec, final boolean alwaysUseList) {
      Codec<List<Holder<E>>> listCodec = elementCodec.listOf().validate(ExtraCodecs.ensureHomogenous(Holder::kind));
      return alwaysUseList ? listCodec : ExtraCodecs.compactListCodec(elementCodec, listCodec);
   }

   public static Codec create(final ResourceKey registryKey, final Codec elementCodec, final boolean alwaysUseList) {
      return new HolderSetCodec(registryKey, elementCodec, alwaysUseList);
   }

   private HolderSetCodec(final ResourceKey registryKey, final Codec elementCodec, final boolean alwaysUseList) {
      this.registryKey = registryKey;
      this.elementCodec = elementCodec;
      this.homogenousListCodec = homogenousList(elementCodec, alwaysUseList);
      this.registryAwareCodec = Codec.either(TagKey.hashedCodec(registryKey), this.homogenousListCodec);
   }

   public DataResult decode(final DynamicOps ops, final Object input) {
      if (ops instanceof RegistryOps registryOps) {
         Optional<HolderGetter<E>> registryOptional = registryOps.getter(this.registryKey);
         if (registryOptional.isPresent()) {
            HolderGetter<E> registry = (HolderGetter)registryOptional.get();
            return this.registryAwareCodec.decode(ops, input).flatMap((p) -> {
               DataResult<HolderSet<E>> result = (DataResult)((Either)p.getFirst()).map((tag) -> lookupTag(registry, tag), (values) -> DataResult.success(HolderSet.direct(values)));
               return result.map((holders) -> Pair.of(holders, p.getSecond()));
            });
         }
      }

      return this.decodeWithoutRegistry(ops, input);
   }

   private static DataResult lookupTag(final HolderGetter registry, final TagKey key) {
      return (DataResult)registry.get(key).map(DataResult::success).orElseGet(() -> DataResult.error(() -> {
            String var10000 = String.valueOf(key.location());
            return "Missing tag: '" + var10000 + "' in '" + String.valueOf(key.registry().identifier()) + "'";
         }));
   }

   public DataResult encode(final HolderSet input, final DynamicOps ops, final Object prefix) {
      if (ops instanceof RegistryOps registryOps) {
         Optional<HolderOwner<E>> maybeOwner = registryOps.owner(this.registryKey);
         if (maybeOwner.isPresent()) {
            if (!input.canSerializeIn((HolderOwner)maybeOwner.get())) {
               return DataResult.error(() -> "HolderSet " + String.valueOf(input) + " is not valid in current registry set");
            }

            return this.registryAwareCodec.encode(input.unwrap().mapRight(List::copyOf), ops, prefix);
         }
      }

      return this.encodeWithoutRegistry(input, ops, prefix);
   }

   private DataResult decodeWithoutRegistry(final DynamicOps ops, final Object input) {
      return this.elementCodec.listOf().decode(ops, input).flatMap((p) -> {
         List<Holder.Direct<E>> directHolders = new ArrayList();

         for(Holder holder : (List)p.getFirst()) {
            if (!(holder instanceof Holder.Direct)) {
               return DataResult.error(() -> "Can't decode element " + String.valueOf(holder) + " without registry");
            }

            Holder.Direct<E> direct = (Holder.Direct)holder;
            directHolders.add(direct);
         }

         return DataResult.success(new Pair(HolderSet.direct(directHolders), p.getSecond()));
      });
   }

   private DataResult encodeWithoutRegistry(final HolderSet input, final DynamicOps ops, final Object prefix) {
      return this.homogenousListCodec.encode(input.stream().toList(), ops, prefix);
   }
}
