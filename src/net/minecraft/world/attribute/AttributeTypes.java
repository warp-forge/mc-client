package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.TriState;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.level.MoonPhase;

public interface AttributeTypes {
   AttributeType BOOLEAN = register("boolean", AttributeType.ofNotInterpolated(Codec.BOOL, AttributeModifier.BOOLEAN_LIBRARY));
   AttributeType TRI_STATE = register("tri_state", AttributeType.ofNotInterpolated(TriState.CODEC));
   AttributeType FLOAT = register("float", AttributeType.ofInterpolated(Codec.FLOAT, AttributeModifier.FLOAT_LIBRARY, LerpFunction.ofFloat()));
   AttributeType ANGLE_DEGREES = register("angle_degrees", AttributeType.ofInterpolated(Codec.FLOAT, AttributeModifier.FLOAT_LIBRARY, LerpFunction.ofFloat(), LerpFunction.ofDegrees(90.0F)));
   AttributeType RGB_COLOR = register("rgb_color", AttributeType.ofInterpolated(ExtraCodecs.STRING_RGB_COLOR, AttributeModifier.RGB_COLOR_LIBRARY, LerpFunction.ofColor()));
   AttributeType ARGB_COLOR = register("argb_color", AttributeType.ofInterpolated(ExtraCodecs.STRING_ARGB_COLOR, AttributeModifier.ARGB_COLOR_LIBRARY, LerpFunction.ofColor()));
   AttributeType MOON_PHASE = register("moon_phase", AttributeType.ofNotInterpolated(MoonPhase.CODEC));
   AttributeType ACTIVITY = register("activity", AttributeType.ofNotInterpolated(BuiltInRegistries.ACTIVITY.byNameCodec()));
   AttributeType BED_RULE = register("bed_rule", AttributeType.ofNotInterpolated(BedRule.CODEC));
   AttributeType PARTICLE = register("particle", AttributeType.ofNotInterpolated(ParticleTypes.CODEC));
   AttributeType AMBIENT_PARTICLES = register("ambient_particles", AttributeType.ofNotInterpolated(AmbientParticle.CODEC.listOf()));
   AttributeType BACKGROUND_MUSIC = register("background_music", AttributeType.ofNotInterpolated(BackgroundMusic.CODEC));
   AttributeType AMBIENT_SOUNDS = register("ambient_sounds", AttributeType.ofNotInterpolated(AmbientSounds.CODEC));
   Codec CODEC = BuiltInRegistries.ATTRIBUTE_TYPE.byNameCodec();

   static AttributeType bootstrap(final Registry registry) {
      return BOOLEAN;
   }

   static AttributeType register(final String name, final AttributeType type) {
      Registry.register(BuiltInRegistries.ATTRIBUTE_TYPE, (Identifier)Identifier.withDefaultNamespace(name), type);
      return type;
   }
}
