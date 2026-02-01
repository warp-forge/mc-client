package net.minecraft.world.item.alchemy;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class Potions {
   public static final Holder.Reference WATER = register("water", new Potion("water", new MobEffectInstance[0]));
   public static final Holder.Reference MUNDANE = register("mundane", new Potion("mundane", new MobEffectInstance[0]));
   public static final Holder.Reference THICK = register("thick", new Potion("thick", new MobEffectInstance[0]));
   public static final Holder.Reference AWKWARD = register("awkward", new Potion("awkward", new MobEffectInstance[0]));
   public static final Holder.Reference NIGHT_VISION;
   public static final Holder.Reference LONG_NIGHT_VISION;
   public static final Holder.Reference INVISIBILITY;
   public static final Holder.Reference LONG_INVISIBILITY;
   public static final Holder.Reference LEAPING;
   public static final Holder.Reference LONG_LEAPING;
   public static final Holder.Reference STRONG_LEAPING;
   public static final Holder.Reference FIRE_RESISTANCE;
   public static final Holder.Reference LONG_FIRE_RESISTANCE;
   public static final Holder.Reference SWIFTNESS;
   public static final Holder.Reference LONG_SWIFTNESS;
   public static final Holder.Reference STRONG_SWIFTNESS;
   public static final Holder.Reference SLOWNESS;
   public static final Holder.Reference LONG_SLOWNESS;
   public static final Holder.Reference STRONG_SLOWNESS;
   public static final Holder.Reference TURTLE_MASTER;
   public static final Holder.Reference LONG_TURTLE_MASTER;
   public static final Holder.Reference STRONG_TURTLE_MASTER;
   public static final Holder.Reference WATER_BREATHING;
   public static final Holder.Reference LONG_WATER_BREATHING;
   public static final Holder.Reference HEALING;
   public static final Holder.Reference STRONG_HEALING;
   public static final Holder.Reference HARMING;
   public static final Holder.Reference STRONG_HARMING;
   public static final Holder.Reference POISON;
   public static final Holder.Reference LONG_POISON;
   public static final Holder.Reference STRONG_POISON;
   public static final Holder.Reference REGENERATION;
   public static final Holder.Reference LONG_REGENERATION;
   public static final Holder.Reference STRONG_REGENERATION;
   public static final Holder.Reference STRENGTH;
   public static final Holder.Reference LONG_STRENGTH;
   public static final Holder.Reference STRONG_STRENGTH;
   public static final Holder.Reference WEAKNESS;
   public static final Holder.Reference LONG_WEAKNESS;
   public static final Holder.Reference LUCK;
   public static final Holder.Reference SLOW_FALLING;
   public static final Holder.Reference LONG_SLOW_FALLING;
   public static final Holder.Reference WIND_CHARGED;
   public static final Holder.Reference WEAVING;
   public static final Holder.Reference OOZING;
   public static final Holder.Reference INFESTED;

   private static Holder.Reference register(final String name, final Potion potion) {
      return Registry.registerForHolder(BuiltInRegistries.POTION, (Identifier)Identifier.withDefaultNamespace(name), potion);
   }

   public static Holder.Reference bootstrap(final Registry registry) {
      return WATER;
   }

   static {
      NIGHT_VISION = register("night_vision", new Potion("night_vision", new MobEffectInstance[]{new MobEffectInstance(MobEffects.NIGHT_VISION, 3600)}));
      LONG_NIGHT_VISION = register("long_night_vision", new Potion("night_vision", new MobEffectInstance[]{new MobEffectInstance(MobEffects.NIGHT_VISION, 9600)}));
      INVISIBILITY = register("invisibility", new Potion("invisibility", new MobEffectInstance[]{new MobEffectInstance(MobEffects.INVISIBILITY, 3600)}));
      LONG_INVISIBILITY = register("long_invisibility", new Potion("invisibility", new MobEffectInstance[]{new MobEffectInstance(MobEffects.INVISIBILITY, 9600)}));
      LEAPING = register("leaping", new Potion("leaping", new MobEffectInstance[]{new MobEffectInstance(MobEffects.JUMP_BOOST, 3600)}));
      LONG_LEAPING = register("long_leaping", new Potion("leaping", new MobEffectInstance[]{new MobEffectInstance(MobEffects.JUMP_BOOST, 9600)}));
      STRONG_LEAPING = register("strong_leaping", new Potion("leaping", new MobEffectInstance[]{new MobEffectInstance(MobEffects.JUMP_BOOST, 1800, 1)}));
      FIRE_RESISTANCE = register("fire_resistance", new Potion("fire_resistance", new MobEffectInstance[]{new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600)}));
      LONG_FIRE_RESISTANCE = register("long_fire_resistance", new Potion("fire_resistance", new MobEffectInstance[]{new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600)}));
      SWIFTNESS = register("swiftness", new Potion("swiftness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SPEED, 3600)}));
      LONG_SWIFTNESS = register("long_swiftness", new Potion("swiftness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SPEED, 9600)}));
      STRONG_SWIFTNESS = register("strong_swiftness", new Potion("swiftness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SPEED, 1800, 1)}));
      SLOWNESS = register("slowness", new Potion("slowness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOWNESS, 1800)}));
      LONG_SLOWNESS = register("long_slowness", new Potion("slowness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOWNESS, 4800)}));
      STRONG_SLOWNESS = register("strong_slowness", new Potion("slowness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOWNESS, 400, 3)}));
      TURTLE_MASTER = register("turtle_master", new Potion("turtle_master", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOWNESS, 400, 3), new MobEffectInstance(MobEffects.RESISTANCE, 400, 2)}));
      LONG_TURTLE_MASTER = register("long_turtle_master", new Potion("turtle_master", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOWNESS, 800, 3), new MobEffectInstance(MobEffects.RESISTANCE, 800, 2)}));
      STRONG_TURTLE_MASTER = register("strong_turtle_master", new Potion("turtle_master", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOWNESS, 400, 5), new MobEffectInstance(MobEffects.RESISTANCE, 400, 3)}));
      WATER_BREATHING = register("water_breathing", new Potion("water_breathing", new MobEffectInstance[]{new MobEffectInstance(MobEffects.WATER_BREATHING, 3600)}));
      LONG_WATER_BREATHING = register("long_water_breathing", new Potion("water_breathing", new MobEffectInstance[]{new MobEffectInstance(MobEffects.WATER_BREATHING, 9600)}));
      HEALING = register("healing", new Potion("healing", new MobEffectInstance[]{new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1)}));
      STRONG_HEALING = register("strong_healing", new Potion("healing", new MobEffectInstance[]{new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 1)}));
      HARMING = register("harming", new Potion("harming", new MobEffectInstance[]{new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1)}));
      STRONG_HARMING = register("strong_harming", new Potion("harming", new MobEffectInstance[]{new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1)}));
      POISON = register("poison", new Potion("poison", new MobEffectInstance[]{new MobEffectInstance(MobEffects.POISON, 900)}));
      LONG_POISON = register("long_poison", new Potion("poison", new MobEffectInstance[]{new MobEffectInstance(MobEffects.POISON, 1800)}));
      STRONG_POISON = register("strong_poison", new Potion("poison", new MobEffectInstance[]{new MobEffectInstance(MobEffects.POISON, 432, 1)}));
      REGENERATION = register("regeneration", new Potion("regeneration", new MobEffectInstance[]{new MobEffectInstance(MobEffects.REGENERATION, 900)}));
      LONG_REGENERATION = register("long_regeneration", new Potion("regeneration", new MobEffectInstance[]{new MobEffectInstance(MobEffects.REGENERATION, 1800)}));
      STRONG_REGENERATION = register("strong_regeneration", new Potion("regeneration", new MobEffectInstance[]{new MobEffectInstance(MobEffects.REGENERATION, 450, 1)}));
      STRENGTH = register("strength", new Potion("strength", new MobEffectInstance[]{new MobEffectInstance(MobEffects.STRENGTH, 3600)}));
      LONG_STRENGTH = register("long_strength", new Potion("strength", new MobEffectInstance[]{new MobEffectInstance(MobEffects.STRENGTH, 9600)}));
      STRONG_STRENGTH = register("strong_strength", new Potion("strength", new MobEffectInstance[]{new MobEffectInstance(MobEffects.STRENGTH, 1800, 1)}));
      WEAKNESS = register("weakness", new Potion("weakness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.WEAKNESS, 1800)}));
      LONG_WEAKNESS = register("long_weakness", new Potion("weakness", new MobEffectInstance[]{new MobEffectInstance(MobEffects.WEAKNESS, 4800)}));
      LUCK = register("luck", new Potion("luck", new MobEffectInstance[]{new MobEffectInstance(MobEffects.LUCK, 6000)}));
      SLOW_FALLING = register("slow_falling", new Potion("slow_falling", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOW_FALLING, 1800)}));
      LONG_SLOW_FALLING = register("long_slow_falling", new Potion("slow_falling", new MobEffectInstance[]{new MobEffectInstance(MobEffects.SLOW_FALLING, 4800)}));
      WIND_CHARGED = register("wind_charged", new Potion("wind_charged", new MobEffectInstance[]{new MobEffectInstance(MobEffects.WIND_CHARGED, 3600)}));
      WEAVING = register("weaving", new Potion("weaving", new MobEffectInstance[]{new MobEffectInstance(MobEffects.WEAVING, 3600)}));
      OOZING = register("oozing", new Potion("oozing", new MobEffectInstance[]{new MobEffectInstance(MobEffects.OOZING, 3600)}));
      INFESTED = register("infested", new Potion("infested", new MobEffectInstance[]{new MobEffectInstance(MobEffects.INFESTED, 3600)}));
   }
}
