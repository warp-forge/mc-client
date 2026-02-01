package net.minecraft.world.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MobEffects {
   private static final int DARKNESS_EFFECT_FACTOR_PADDING_DURATION_TICKS = 22;
   public static final Holder SPEED;
   public static final Holder SLOWNESS;
   public static final Holder HASTE;
   public static final Holder MINING_FATIGUE;
   public static final Holder STRENGTH;
   public static final Holder INSTANT_HEALTH;
   public static final Holder INSTANT_DAMAGE;
   public static final Holder JUMP_BOOST;
   public static final Holder NAUSEA;
   public static final Holder REGENERATION;
   public static final Holder RESISTANCE;
   public static final Holder FIRE_RESISTANCE;
   public static final Holder WATER_BREATHING;
   public static final Holder INVISIBILITY;
   public static final Holder BLINDNESS;
   public static final Holder NIGHT_VISION;
   public static final Holder HUNGER;
   public static final Holder WEAKNESS;
   public static final Holder POISON;
   public static final Holder WITHER;
   public static final Holder HEALTH_BOOST;
   public static final Holder ABSORPTION;
   public static final Holder SATURATION;
   public static final Holder GLOWING;
   public static final Holder LEVITATION;
   public static final Holder LUCK;
   public static final Holder UNLUCK;
   public static final Holder SLOW_FALLING;
   public static final Holder CONDUIT_POWER;
   public static final Holder DOLPHINS_GRACE;
   public static final Holder BAD_OMEN;
   public static final Holder HERO_OF_THE_VILLAGE;
   public static final Holder DARKNESS;
   public static final Holder TRIAL_OMEN;
   public static final Holder RAID_OMEN;
   public static final Holder WIND_CHARGED;
   public static final Holder WEAVING;
   public static final Holder OOZING;
   public static final Holder INFESTED;
   public static final Holder BREATH_OF_THE_NAUTILUS;

   private static Holder register(final String name, final MobEffect mobEffect) {
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, (Identifier)Identifier.withDefaultNamespace(name), mobEffect);
   }

   public static Holder bootstrap(final Registry registry) {
      return SPEED;
   }

   static {
      SPEED = register("speed", (new MobEffect(MobEffectCategory.BENEFICIAL, 3402751)).addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.withDefaultNamespace("effect.speed"), (double)0.2F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      SLOWNESS = register("slowness", (new MobEffect(MobEffectCategory.HARMFUL, 9154528)).addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.withDefaultNamespace("effect.slowness"), (double)-0.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      HASTE = register("haste", (new MobEffect(MobEffectCategory.BENEFICIAL, 14270531)).addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.withDefaultNamespace("effect.haste"), (double)0.1F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      MINING_FATIGUE = register("mining_fatigue", (new MobEffect(MobEffectCategory.HARMFUL, 4866583)).addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.withDefaultNamespace("effect.mining_fatigue"), (double)-0.1F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      STRENGTH = register("strength", (new MobEffect(MobEffectCategory.BENEFICIAL, 16762624)).addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.strength"), (double)3.0F, AttributeModifier.Operation.ADD_VALUE));
      INSTANT_HEALTH = register("instant_health", new HealOrHarmMobEffect(MobEffectCategory.BENEFICIAL, 16262179, false));
      INSTANT_DAMAGE = register("instant_damage", new HealOrHarmMobEffect(MobEffectCategory.HARMFUL, 11101546, true));
      JUMP_BOOST = register("jump_boost", (new MobEffect(MobEffectCategory.BENEFICIAL, 16646020)).addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Identifier.withDefaultNamespace("effect.jump_boost"), (double)1.0F, AttributeModifier.Operation.ADD_VALUE));
      NAUSEA = register("nausea", (new MobEffect(MobEffectCategory.HARMFUL, 5578058)).setBlendDuration(150, 20, 60));
      REGENERATION = register("regeneration", new RegenerationMobEffect(MobEffectCategory.BENEFICIAL, 13458603));
      RESISTANCE = register("resistance", new MobEffect(MobEffectCategory.BENEFICIAL, 9520880));
      FIRE_RESISTANCE = register("fire_resistance", new MobEffect(MobEffectCategory.BENEFICIAL, 16750848));
      WATER_BREATHING = register("water_breathing", new MobEffect(MobEffectCategory.BENEFICIAL, 10017472));
      INVISIBILITY = register("invisibility", (new MobEffect(MobEffectCategory.BENEFICIAL, 16185078)).addAttributeModifier(Attributes.WAYPOINT_TRANSMIT_RANGE, Identifier.withDefaultNamespace("effect.waypoint_transmit_range_hide"), (double)-1.0F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      BLINDNESS = register("blindness", new MobEffect(MobEffectCategory.HARMFUL, 2039587));
      NIGHT_VISION = register("night_vision", new MobEffect(MobEffectCategory.BENEFICIAL, 12779366));
      HUNGER = register("hunger", new HungerMobEffect(MobEffectCategory.HARMFUL, 5797459));
      WEAKNESS = register("weakness", (new MobEffect(MobEffectCategory.HARMFUL, 4738376)).addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.weakness"), (double)-4.0F, AttributeModifier.Operation.ADD_VALUE));
      POISON = register("poison", new PoisonMobEffect(MobEffectCategory.HARMFUL, 8889187));
      WITHER = register("wither", new WitherMobEffect(MobEffectCategory.HARMFUL, 7561558));
      HEALTH_BOOST = register("health_boost", (new MobEffect(MobEffectCategory.BENEFICIAL, 16284963)).addAttributeModifier(Attributes.MAX_HEALTH, Identifier.withDefaultNamespace("effect.health_boost"), (double)4.0F, AttributeModifier.Operation.ADD_VALUE));
      ABSORPTION = register("absorption", (new AbsorptionMobEffect(MobEffectCategory.BENEFICIAL, 2445989)).addAttributeModifier(Attributes.MAX_ABSORPTION, Identifier.withDefaultNamespace("effect.absorption"), (double)4.0F, AttributeModifier.Operation.ADD_VALUE));
      SATURATION = register("saturation", new SaturationMobEffect(MobEffectCategory.BENEFICIAL, 16262179));
      GLOWING = register("glowing", new MobEffect(MobEffectCategory.NEUTRAL, 9740385));
      LEVITATION = register("levitation", new MobEffect(MobEffectCategory.HARMFUL, 13565951));
      LUCK = register("luck", (new MobEffect(MobEffectCategory.BENEFICIAL, 5882118)).addAttributeModifier(Attributes.LUCK, Identifier.withDefaultNamespace("effect.luck"), (double)1.0F, AttributeModifier.Operation.ADD_VALUE));
      UNLUCK = register("unluck", (new MobEffect(MobEffectCategory.HARMFUL, 12624973)).addAttributeModifier(Attributes.LUCK, Identifier.withDefaultNamespace("effect.unluck"), (double)-1.0F, AttributeModifier.Operation.ADD_VALUE));
      SLOW_FALLING = register("slow_falling", new MobEffect(MobEffectCategory.BENEFICIAL, 15978425));
      CONDUIT_POWER = register("conduit_power", new MobEffect(MobEffectCategory.BENEFICIAL, 1950417));
      DOLPHINS_GRACE = register("dolphins_grace", new MobEffect(MobEffectCategory.BENEFICIAL, 8954814));
      BAD_OMEN = register("bad_omen", (new BadOmenMobEffect(MobEffectCategory.NEUTRAL, 745784)).withSoundOnAdded(SoundEvents.APPLY_EFFECT_BAD_OMEN));
      HERO_OF_THE_VILLAGE = register("hero_of_the_village", new MobEffect(MobEffectCategory.BENEFICIAL, 4521796));
      DARKNESS = register("darkness", (new MobEffect(MobEffectCategory.HARMFUL, 2696993)).setBlendDuration(22));
      TRIAL_OMEN = register("trial_omen", (new MobEffect(MobEffectCategory.NEUTRAL, 1484454, ParticleTypes.TRIAL_OMEN)).withSoundOnAdded(SoundEvents.APPLY_EFFECT_TRIAL_OMEN));
      RAID_OMEN = register("raid_omen", (new RaidOmenMobEffect(MobEffectCategory.NEUTRAL, 14565464, ParticleTypes.RAID_OMEN)).withSoundOnAdded(SoundEvents.APPLY_EFFECT_RAID_OMEN));
      WIND_CHARGED = register("wind_charged", new WindChargedMobEffect(MobEffectCategory.HARMFUL, 12438015));
      WEAVING = register("weaving", new WeavingMobEffect(MobEffectCategory.HARMFUL, 7891290, (random) -> Mth.randomBetweenInclusive(random, 2, 3)));
      OOZING = register("oozing", new OozingMobEffect(MobEffectCategory.HARMFUL, 10092451, (random) -> 2));
      INFESTED = register("infested", new InfestedMobEffect(MobEffectCategory.HARMFUL, 9214860, 0.1F, (random) -> Mth.randomBetweenInclusive(random, 1, 2)));
      BREATH_OF_THE_NAUTILUS = register("breath_of_the_nautilus", new MobEffect(MobEffectCategory.BENEFICIAL, 65518));
   }
}
