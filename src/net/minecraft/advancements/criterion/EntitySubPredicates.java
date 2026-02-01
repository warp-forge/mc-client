package net.minecraft.advancements.criterion;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntitySubPredicates {
   public static final MapCodec LIGHTNING;
   public static final MapCodec FISHING_HOOK;
   public static final MapCodec PLAYER;
   public static final MapCodec SLIME;
   public static final MapCodec RAIDER;
   public static final MapCodec SHEEP;

   private static MapCodec register(final String id, final MapCodec value) {
      return (MapCodec)Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, (String)id, value);
   }

   public static MapCodec bootstrap(final Registry registry) {
      return LIGHTNING;
   }

   static {
      LIGHTNING = register("lightning", LightningBoltPredicate.CODEC);
      FISHING_HOOK = register("fishing_hook", FishingHookPredicate.CODEC);
      PLAYER = register("player", PlayerPredicate.CODEC);
      SLIME = register("slime", SlimePredicate.CODEC);
      RAIDER = register("raider", RaiderPredicate.CODEC);
      SHEEP = register("sheep", SheepPredicate.CODEC);
   }
}
