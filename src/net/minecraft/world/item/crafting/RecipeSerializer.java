package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record RecipeSerializer(MapCodec codec, StreamCodec streamCodec) {
   public RecipeSerializer(MapCodec codec, @Deprecated StreamCodec streamCodec) {
      this.codec = codec;
      this.streamCodec = streamCodec;
   }

   /** @deprecated */
   @Deprecated
   public StreamCodec streamCodec() {
      return this.streamCodec;
   }
}
