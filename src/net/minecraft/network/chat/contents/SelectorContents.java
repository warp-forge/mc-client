package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public record SelectorContents(SelectorPattern selector, Optional separator) implements ComponentContents {
   public static final MapCodec MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(SelectorPattern.CODEC.fieldOf("selector").forGetter(SelectorContents::selector), ComponentSerialization.CODEC.optionalFieldOf("separator").forGetter(SelectorContents::separator)).apply(i, SelectorContents::new));

   public MapCodec codec() {
      return MAP_CODEC;
   }

   public MutableComponent resolve(final @Nullable CommandSourceStack source, final @Nullable Entity entity, final int recursionDepth) throws CommandSyntaxException {
      if (source == null) {
         return Component.empty();
      } else {
         Optional<? extends Component> resolvedSeparator = ComponentUtils.updateForEntity(source, this.separator, entity, recursionDepth);
         return ComponentUtils.formatList(this.selector.resolved().findEntities(source), (Optional)resolvedSeparator, Entity::getDisplayName);
      }
   }

   public Optional visit(final FormattedText.StyledContentConsumer output, final Style currentStyle) {
      return output.accept(currentStyle, this.selector.pattern());
   }

   public Optional visit(final FormattedText.ContentConsumer output) {
      return output.accept(this.selector.pattern());
   }

   public String toString() {
      return "pattern{" + String.valueOf(this.selector) + "}";
   }
}
