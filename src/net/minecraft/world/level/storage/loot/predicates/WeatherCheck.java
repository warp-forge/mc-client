package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;

public record WeatherCheck(Optional isRaining, Optional isThundering) implements LootItemCondition {
   public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(Codec.BOOL.optionalFieldOf("raining").forGetter(WeatherCheck::isRaining), Codec.BOOL.optionalFieldOf("thundering").forGetter(WeatherCheck::isThundering)).apply(i, WeatherCheck::new));

   public MapCodec codec() {
      return MAP_CODEC;
   }

   public boolean test(final LootContext context) {
      ServerLevel level = context.getLevel();
      if (this.isRaining.isPresent() && (Boolean)this.isRaining.get() != level.isRaining()) {
         return false;
      } else {
         return !this.isThundering.isPresent() || (Boolean)this.isThundering.get() == level.isThundering();
      }
   }

   public static Builder weather() {
      return new Builder();
   }

   public static class Builder implements LootItemCondition.Builder {
      private Optional isRaining = Optional.empty();
      private Optional isThundering = Optional.empty();

      public Builder setRaining(final boolean raining) {
         this.isRaining = Optional.of(raining);
         return this;
      }

      public Builder setThundering(final boolean thundering) {
         this.isThundering = Optional.of(thundering);
         return this;
      }

      public WeatherCheck build() {
         return new WeatherCheck(this.isRaining, this.isThundering);
      }
   }
}
