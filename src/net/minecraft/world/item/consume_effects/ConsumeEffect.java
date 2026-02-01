package net.minecraft.world.item.consume_effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ConsumeEffect {
   Codec CODEC = BuiltInRegistries.CONSUME_EFFECT_TYPE.byNameCodec().dispatch(ConsumeEffect::getType, Type::codec);
   StreamCodec STREAM_CODEC = ByteBufCodecs.registry(Registries.CONSUME_EFFECT_TYPE).dispatch(ConsumeEffect::getType, Type::streamCodec);

   Type getType();

   boolean apply(final Level level, final ItemStack stack, final LivingEntity user);

   public static record Type(MapCodec codec, StreamCodec streamCodec) {
      public static final Type APPLY_EFFECTS;
      public static final Type REMOVE_EFFECTS;
      public static final Type CLEAR_ALL_EFFECTS;
      public static final Type TELEPORT_RANDOMLY;
      public static final Type PLAY_SOUND;

      private static Type register(final String name, final MapCodec codec, final StreamCodec streamCodec) {
         return (Type)Registry.register(BuiltInRegistries.CONSUME_EFFECT_TYPE, (String)name, new Type(codec, streamCodec));
      }

      static {
         APPLY_EFFECTS = register("apply_effects", ApplyStatusEffectsConsumeEffect.CODEC, ApplyStatusEffectsConsumeEffect.STREAM_CODEC);
         REMOVE_EFFECTS = register("remove_effects", RemoveStatusEffectsConsumeEffect.CODEC, RemoveStatusEffectsConsumeEffect.STREAM_CODEC);
         CLEAR_ALL_EFFECTS = register("clear_all_effects", ClearAllStatusEffectsConsumeEffect.CODEC, ClearAllStatusEffectsConsumeEffect.STREAM_CODEC);
         TELEPORT_RANDOMLY = register("teleport_randomly", TeleportRandomlyConsumeEffect.CODEC, TeleportRandomlyConsumeEffect.STREAM_CODEC);
         PLAY_SOUND = register("play_sound", PlaySoundConsumeEffect.CODEC, PlaySoundConsumeEffect.STREAM_CODEC);
      }
   }
}
