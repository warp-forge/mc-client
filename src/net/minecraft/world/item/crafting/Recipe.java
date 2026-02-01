package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface Recipe {
   Codec CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
   Codec KEY_CODEC = ResourceKey.codec(Registries.RECIPE);
   StreamCodec STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER).dispatch(Recipe::getSerializer, RecipeSerializer::streamCodec);

   boolean matches(RecipeInput input, Level level);

   ItemStack assemble(RecipeInput input);

   default boolean isSpecial() {
      return false;
   }

   boolean showNotification();

   String group();

   RecipeSerializer getSerializer();

   RecipeType getType();

   PlacementInfo placementInfo();

   default List display() {
      return List.of();
   }

   RecipeBookCategory recipeBookCategory();

   public static record CommonInfo(boolean showNotification) {
      public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(CommonInfo::showNotification)).apply(i, CommonInfo::new));
      public static final StreamCodec STREAM_CODEC;

      static {
         STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, CommonInfo::showNotification, CommonInfo::new);
      }
   }

   public interface BookInfo {
      Object category();

      String group();

      static MapCodec mapCodec(final Codec categoryCodec, final Object defaultCategory, final Constructor constructor) {
         return RecordCodecBuilder.mapCodec((i) -> i.group(categoryCodec.fieldOf("category").orElse(defaultCategory).forGetter(BookInfo::category), Codec.STRING.optionalFieldOf("group", "").forGetter(BookInfo::group)).apply(i, constructor));
      }

      static StreamCodec streamCodec(final StreamCodec categoryCodec, final Constructor constructor) {
         return StreamCodec.composite(categoryCodec, BookInfo::category, ByteBufCodecs.STRING_UTF8, BookInfo::group, constructor);
      }

      @FunctionalInterface
      public interface Constructor extends BiFunction {
      }
   }
}
