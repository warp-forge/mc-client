package net.minecraft.world.entity.animal.wolf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEvent;

public record WolfSoundVariant(WolfSoundSet adultSounds, WolfSoundSet babySounds) {
   public static final Codec DIRECT_CODEC = getWolfSoundVariantCodec();
   public static final Codec NETWORK_CODEC = getWolfSoundVariantCodec();
   public static final Codec CODEC;
   public static final StreamCodec STREAM_CODEC;

   private static Codec getWolfSoundVariantCodec() {
      return RecordCodecBuilder.create((i) -> i.group(WolfSoundVariant.WolfSoundSet.CODEC.fieldOf("adult_sounds").forGetter(WolfSoundVariant::adultSounds), WolfSoundVariant.WolfSoundSet.CODEC.fieldOf("baby_sounds").forGetter(WolfSoundVariant::babySounds)).apply(i, WolfSoundVariant::new));
   }

   static {
      CODEC = RegistryFixedCodec.create(Registries.WOLF_SOUND_VARIANT);
      STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_SOUND_VARIANT);
   }

   public static record WolfSoundSet(Holder ambientSound, Holder deathSound, Holder growlSound, Holder hurtSound, Holder pantSound, Holder whineSound, Holder stepSound) {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(SoundEvent.CODEC.fieldOf("ambient_sound").forGetter(WolfSoundSet::ambientSound), SoundEvent.CODEC.fieldOf("death_sound").forGetter(WolfSoundSet::deathSound), SoundEvent.CODEC.fieldOf("growl_sound").forGetter(WolfSoundSet::growlSound), SoundEvent.CODEC.fieldOf("hurt_sound").forGetter(WolfSoundSet::hurtSound), SoundEvent.CODEC.fieldOf("pant_sound").forGetter(WolfSoundSet::pantSound), SoundEvent.CODEC.fieldOf("whine_sound").forGetter(WolfSoundSet::whineSound), SoundEvent.CODEC.fieldOf("step_sound").forGetter(WolfSoundSet::stepSound)).apply(i, WolfSoundSet::new));
   }
}
