package net.minecraft.world.entity.variant;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VariantUtils {
   public static final String TAG_VARIANT = "variant";

   public static Holder getDefaultOrAny(final RegistryAccess registryAccess, final ResourceKey id) {
      Registry<T> registry = registryAccess.lookupOrThrow(id.registryKey());
      Optional var10000 = registry.get(id);
      Objects.requireNonNull(registry);
      return (Holder)var10000.or(registry::getAny).orElseThrow();
   }

   public static Holder getAny(final RegistryAccess registryAccess, final ResourceKey registryId) {
      return (Holder)registryAccess.lookupOrThrow(registryId).getAny().orElseThrow();
   }

   public static void writeVariant(final ValueOutput output, final Holder holder) {
      holder.unwrapKey().ifPresent((k) -> output.store("variant", Identifier.CODEC, k.identifier()));
   }

   public static Optional readVariant(final ValueInput input, final ResourceKey registryId) {
      Optional var10000 = input.read("variant", Identifier.CODEC).map((id) -> ResourceKey.create(registryId, id));
      HolderLookup.Provider var10001 = input.lookup();
      Objects.requireNonNull(var10001);
      return var10000.flatMap(var10001::get);
   }

   public static Optional selectVariantToSpawn(final SpawnContext context, final ResourceKey variantRegistry) {
      ServerLevelAccessor level = context.level();
      Stream<Holder.Reference<T>> entries = level.registryAccess().lookupOrThrow(variantRegistry).listElements();
      return PriorityProvider.pick(entries, Holder::value, level.getRandom(), context);
   }
}
