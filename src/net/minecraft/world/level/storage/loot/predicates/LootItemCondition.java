package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemCondition extends LootContextUser, Predicate {
   Codec TYPED_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE.byNameCodec().dispatch("condition", LootItemCondition::codec, (c) -> c);
   Codec DIRECT_CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(TYPED_CODEC, AllOfCondition.INLINE_CODEC));
   Codec CODEC = RegistryFileCodec.create(Registries.PREDICATE, DIRECT_CODEC);

   MapCodec codec();

   @FunctionalInterface
   public interface Builder {
      LootItemCondition build();

      default Builder invert() {
         return InvertedLootItemCondition.invert(this);
      }

      default AnyOfCondition.Builder or(final Builder other) {
         return AnyOfCondition.anyOf(this, other);
      }

      default AllOfCondition.Builder and(final Builder other) {
         return AllOfCondition.allOf(this, other);
      }
   }
}
