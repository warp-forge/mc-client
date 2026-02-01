package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.ItemLike;

public record PotDecorations(Optional back, Optional left, Optional right, Optional front) implements TooltipProvider {
   public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
   public static final Codec CODEC;
   public static final StreamCodec STREAM_CODEC;

   private PotDecorations(final List items) {
      this(getItem(items, 0), getItem(items, 1), getItem(items, 2), getItem(items, 3));
   }

   public PotDecorations(final Item back, final Item left, final Item right, final Item front) {
      this(List.of(back, left, right, front));
   }

   private static Optional getItem(final List sherds, final int i) {
      if (i >= sherds.size()) {
         return Optional.empty();
      } else {
         Item item = (Item)sherds.get(i);
         return item == Items.BRICK ? Optional.empty() : Optional.of(item);
      }
   }

   public List ordered() {
      return Stream.of(this.back, this.left, this.right, this.front).map((item) -> (Item)item.orElse(Items.BRICK)).toList();
   }

   public void addToTooltip(final Item.TooltipContext context, final Consumer consumer, final TooltipFlag flag, final DataComponentGetter components) {
      if (!this.equals(EMPTY)) {
         consumer.accept(CommonComponents.EMPTY);
         addSideDetailsToTooltip(consumer, this.front);
         addSideDetailsToTooltip(consumer, this.left);
         addSideDetailsToTooltip(consumer, this.right);
         addSideDetailsToTooltip(consumer, this.back);
      }
   }

   private static void addSideDetailsToTooltip(final Consumer consumer, final Optional side) {
      consumer.accept((new ItemStack((ItemLike)side.orElse(Items.BRICK), 1)).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY));
   }

   static {
      CODEC = BuiltInRegistries.ITEM.byNameCodec().sizeLimitedListOf(4).xmap(PotDecorations::new, PotDecorations::ordered);
      STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list(4)).map(PotDecorations::new, PotDecorations::ordered);
   }
}
