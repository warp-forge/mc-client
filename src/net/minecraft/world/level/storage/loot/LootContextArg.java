package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public interface LootContextArg {
   Codec ENTITY_OR_BLOCK = createArgCodec((builder) -> builder.anyOf(LootContext.EntityTarget.values()).anyOf(LootContext.BlockEntityTarget.values()));

   @Nullable Object get(LootContext context);

   ContextKey contextParam();

   static LootContextArg cast(final LootContextArg original) {
      return original;
   }

   static Codec createArgCodec(final UnaryOperator consumer) {
      return ((ArgCodecBuilder)consumer.apply(new ArgCodecBuilder())).build();
   }

   public interface Getter extends LootContextArg {
      @Nullable Object get(Object value);

      ContextKey contextParam();

      default @Nullable Object get(final LootContext context) {
         T value = (T)context.getOptionalParameter(this.contextParam());
         return value != null ? this.get(value) : null;
      }
   }

   public interface SimpleGetter extends LootContextArg {
      ContextKey contextParam();

      default @Nullable Object get(final LootContext context) {
         return context.getOptionalParameter(this.contextParam());
      }
   }

   public static final class ArgCodecBuilder {
      private final ExtraCodecs.LateBoundIdMapper sources = new ExtraCodecs.LateBoundIdMapper();

      private ArgCodecBuilder() {
      }

      public ArgCodecBuilder anyOf(final Object[] targets, final Function nameGetter, final Function argFactory) {
         for(Object target : targets) {
            this.sources.put((String)nameGetter.apply(target), (LootContextArg)argFactory.apply(target));
         }

         return this;
      }

      public ArgCodecBuilder anyOf(final StringRepresentable[] targets, final Function argFactory) {
         return this.anyOf(targets, StringRepresentable::getSerializedName, argFactory);
      }

      public ArgCodecBuilder anyOf(final StringRepresentable[] targets) {
         return this.anyOf(targets, (x$0) -> LootContextArg.cast((LootContextArg)x$0));
      }

      public ArgCodecBuilder anyEntity(final Function function) {
         return this.anyOf(LootContext.EntityTarget.values(), (target) -> (LootContextArg)function.apply(target.contextParam()));
      }

      public ArgCodecBuilder anyBlockEntity(final Function function) {
         return this.anyOf(LootContext.BlockEntityTarget.values(), (target) -> (LootContextArg)function.apply(target.contextParam()));
      }

      public ArgCodecBuilder anyItemStack(final Function function) {
         return this.anyOf(LootContext.ItemStackTarget.values(), (target) -> (LootContextArg)function.apply(target.contextParam()));
      }

      private Codec build() {
         return this.sources.codec(Codec.STRING);
      }
   }
}
