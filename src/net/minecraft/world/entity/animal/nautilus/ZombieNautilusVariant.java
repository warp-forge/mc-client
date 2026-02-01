package net.minecraft.world.entity.animal.nautilus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public record ZombieNautilusVariant(ModelAndTexture modelAndTexture, SpawnPrioritySelectors spawnConditions) implements PriorityProvider {
   public static final Codec DIRECT_CODEC = RecordCodecBuilder.create((i) -> i.group(ModelAndTexture.codec(ZombieNautilusVariant.ModelType.CODEC, ZombieNautilusVariant.ModelType.NORMAL).forGetter(ZombieNautilusVariant::modelAndTexture), SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(ZombieNautilusVariant::spawnConditions)).apply(i, ZombieNautilusVariant::new));
   public static final Codec NETWORK_CODEC = RecordCodecBuilder.create((i) -> i.group(ModelAndTexture.codec(ZombieNautilusVariant.ModelType.CODEC, ZombieNautilusVariant.ModelType.NORMAL).forGetter(ZombieNautilusVariant::modelAndTexture)).apply(i, ZombieNautilusVariant::new));
   public static final Codec CODEC;
   public static final StreamCodec STREAM_CODEC;

   private ZombieNautilusVariant(final ModelAndTexture assetInfo) {
      this(assetInfo, SpawnPrioritySelectors.EMPTY);
   }

   public List selectors() {
      return this.spawnConditions.selectors();
   }

   static {
      CODEC = RegistryFixedCodec.create(Registries.ZOMBIE_NAUTILUS_VARIANT);
      STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ZOMBIE_NAUTILUS_VARIANT);
   }

   public static enum ModelType implements StringRepresentable {
      NORMAL("normal"),
      WARM("warm");

      public static final Codec CODEC = StringRepresentable.fromEnum(ModelType::values);
      private final String name;

      private ModelType(final String name) {
         this.name = name;
      }

      public String getSerializedName() {
         return this.name;
      }

      // $FF: synthetic method
      private static ModelType[] $values() {
         return new ModelType[]{NORMAL, WARM};
      }
   }
}
