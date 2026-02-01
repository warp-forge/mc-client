package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class CycleButton extends AbstractButton implements ResettableOptionWidget {
   public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = () -> Minecraft.getInstance().hasAltDown();
   private static final List BOOLEAN_OPTIONS;
   private final Supplier defaultValueSupplier;
   private final Component name;
   private int index;
   private Object value;
   private final ValueListSupplier values;
   private final Function valueStringifier;
   private final Function narrationProvider;
   private final OnValueChange onValueChange;
   private final DisplayState displayState;
   private final OptionInstance.TooltipSupplier tooltipSupplier;
   private final SpriteSupplier spriteSupplier;

   private CycleButton(final int x, final int y, final int width, final int height, final Component message, final Component name, final int index, final Object value, final Supplier defaultValueSupplier, final ValueListSupplier values, final Function valueStringifier, final Function narrationProvider, final OnValueChange onValueChange, final OptionInstance.TooltipSupplier tooltipSupplier, final DisplayState displayState, final SpriteSupplier spriteSupplier) {
      super(x, y, width, height, message);
      this.name = name;
      this.index = index;
      this.defaultValueSupplier = defaultValueSupplier;
      this.value = value;
      this.values = values;
      this.valueStringifier = valueStringifier;
      this.narrationProvider = narrationProvider;
      this.onValueChange = onValueChange;
      this.displayState = displayState;
      this.tooltipSupplier = tooltipSupplier;
      this.spriteSupplier = spriteSupplier;
      this.updateTooltip();
   }

   protected void renderContents(final GuiGraphics graphics, final int mouseX, final int mouseY, final float a) {
      Identifier sprite = this.spriteSupplier.apply(this, this.getValue());
      if (sprite != null) {
         graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
      } else {
         this.renderDefaultSprite(graphics);
      }

      if (this.displayState != CycleButton.DisplayState.HIDE) {
         this.renderDefaultLabel(graphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
      }

   }

   private void updateTooltip() {
      this.setTooltip(this.tooltipSupplier.apply(this.value));
   }

   public void onPress(final InputWithModifiers input) {
      if (input.hasShiftDown()) {
         this.cycleValue(-1);
      } else {
         this.cycleValue(1);
      }

   }

   private void cycleValue(final int delta) {
      List<T> list = this.values.getSelectedList();
      this.index = Mth.positiveModulo(this.index + delta, list.size());
      T newValue = (T)list.get(this.index);
      this.updateValue(newValue);
      this.onValueChange.onValueChange(this, newValue);
   }

   private Object getCycledValue(final int delta) {
      List<T> list = this.values.getSelectedList();
      return list.get(Mth.positiveModulo(this.index + delta, list.size()));
   }

   public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
      if (scrollY > (double)0.0F) {
         this.cycleValue(-1);
      } else if (scrollY < (double)0.0F) {
         this.cycleValue(1);
      }

      return true;
   }

   public void setValue(final Object newValue) {
      List<T> list = this.values.getSelectedList();
      int newIndex = list.indexOf(newValue);
      if (newIndex != -1) {
         this.index = newIndex;
      }

      this.updateValue(newValue);
   }

   public void resetValue() {
      this.setValue(this.defaultValueSupplier.get());
   }

   private void updateValue(final Object newValue) {
      Component newMessage = this.createLabelForValue(newValue);
      this.setMessage(newMessage);
      this.value = newValue;
      this.updateTooltip();
   }

   private Component createLabelForValue(final Object newValue) {
      return (Component)(this.displayState == CycleButton.DisplayState.VALUE ? (Component)this.valueStringifier.apply(newValue) : this.createFullName(newValue));
   }

   private MutableComponent createFullName(final Object newValue) {
      return CommonComponents.optionNameValue(this.name, (Component)this.valueStringifier.apply(newValue));
   }

   public Object getValue() {
      return this.value;
   }

   protected MutableComponent createNarrationMessage() {
      return (MutableComponent)this.narrationProvider.apply(this);
   }

   public void updateWidgetNarration(final NarrationElementOutput output) {
      output.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
      if (this.active) {
         T nextValue = (T)this.getCycledValue(1);
         Component nextValueText = this.createLabelForValue(nextValue);
         if (this.isFocused()) {
            output.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.focused", nextValueText));
         } else {
            output.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.hovered", nextValueText));
         }
      }

   }

   public MutableComponent createDefaultNarrationMessage() {
      return wrapDefaultNarrationMessage((Component)(this.displayState == CycleButton.DisplayState.VALUE ? this.createFullName(this.value) : this.getMessage()));
   }

   public static Builder builder(final Function valueStringifier, final Supplier defaultValueSupplier) {
      return new Builder(valueStringifier, defaultValueSupplier);
   }

   public static Builder builder(final Function valueStringifier, final Object defaultValue) {
      return new Builder(valueStringifier, () -> defaultValue);
   }

   public static Builder booleanBuilder(final Component trueText, final Component falseText, final boolean defaultValue) {
      return (new Builder((b) -> b == Boolean.TRUE ? trueText : falseText, () -> defaultValue)).withValues((Collection)BOOLEAN_OPTIONS);
   }

   public static Builder onOffBuilder(final boolean initialValue) {
      return (new Builder((b) -> b == Boolean.TRUE ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF, () -> initialValue)).withValues((Collection)BOOLEAN_OPTIONS);
   }

   static {
      BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
   }

   public static class Builder {
      private final Supplier defaultValueSupplier;
      private final Function valueStringifier;
      private OptionInstance.TooltipSupplier tooltipSupplier = (value) -> null;
      private SpriteSupplier spriteSupplier = (button, value) -> null;
      private Function narrationProvider = CycleButton::createDefaultNarrationMessage;
      private ValueListSupplier values = CycleButton.ValueListSupplier.create(ImmutableList.of());
      private DisplayState displayState;

      public Builder(final Function valueStringifier, final Supplier defaultValueSupplier) {
         this.displayState = CycleButton.DisplayState.NAME_AND_VALUE;
         this.valueStringifier = valueStringifier;
         this.defaultValueSupplier = defaultValueSupplier;
      }

      public Builder withValues(final Collection values) {
         return this.withValues(CycleButton.ValueListSupplier.create(values));
      }

      @SafeVarargs
      public final Builder withValues(final Object... values) {
         return this.withValues((Collection)ImmutableList.copyOf(values));
      }

      public Builder withValues(final List values, final List altValues) {
         return this.withValues(CycleButton.ValueListSupplier.create(CycleButton.DEFAULT_ALT_LIST_SELECTOR, values, altValues));
      }

      public Builder withValues(final BooleanSupplier altCondition, final List values, final List altValues) {
         return this.withValues(CycleButton.ValueListSupplier.create(altCondition, values, altValues));
      }

      public Builder withValues(final ValueListSupplier valueListSupplier) {
         this.values = valueListSupplier;
         return this;
      }

      public Builder withTooltip(final OptionInstance.TooltipSupplier tooltipSupplier) {
         this.tooltipSupplier = tooltipSupplier;
         return this;
      }

      public Builder withCustomNarration(final Function narrationProvider) {
         this.narrationProvider = narrationProvider;
         return this;
      }

      public Builder withSprite(final SpriteSupplier spriteSupplier) {
         this.spriteSupplier = spriteSupplier;
         return this;
      }

      public Builder displayState(final DisplayState state) {
         this.displayState = state;
         return this;
      }

      public Builder displayOnlyValue() {
         return this.displayState(CycleButton.DisplayState.VALUE);
      }

      public CycleButton create(final Component name, final OnValueChange valueChangeListener) {
         return this.create(0, 0, 150, 20, name, valueChangeListener);
      }

      public CycleButton create(final int x, final int y, final int width, final int height, final Component name) {
         return this.create(x, y, width, height, name, (button, value) -> {
         });
      }

      public CycleButton create(final int x, final int y, final int width, final int height, final Component name, final OnValueChange valueChangeListener) {
         List<T> values = this.values.getDefaultList();
         if (values.isEmpty()) {
            throw new IllegalStateException("No values for cycle button");
         } else {
            T initialValue = (T)this.defaultValueSupplier.get();
            int initialIndex = values.indexOf(initialValue);
            Component valueText = (Component)this.valueStringifier.apply(initialValue);
            Component initialTitle = (Component)(this.displayState == CycleButton.DisplayState.VALUE ? valueText : CommonComponents.optionNameValue(name, valueText));
            return new CycleButton(x, y, width, height, initialTitle, name, initialIndex, initialValue, this.defaultValueSupplier, this.values, this.valueStringifier, this.narrationProvider, valueChangeListener, this.tooltipSupplier, this.displayState, this.spriteSupplier);
         }
      }
   }

   public interface ValueListSupplier {
      List getSelectedList();

      List getDefaultList();

      static ValueListSupplier create(final Collection values) {
         final List<T> copy = ImmutableList.copyOf(values);
         return new ValueListSupplier() {
            public List getSelectedList() {
               return copy;
            }

            public List getDefaultList() {
               return copy;
            }
         };
      }

      static ValueListSupplier create(final BooleanSupplier altSelector, final List defaultList, final List altList) {
         final List<T> defaultCopy = ImmutableList.copyOf(defaultList);
         final List<T> altCopy = ImmutableList.copyOf(altList);
         return new ValueListSupplier() {
            public List getSelectedList() {
               return altSelector.getAsBoolean() ? altCopy : defaultCopy;
            }

            public List getDefaultList() {
               return defaultCopy;
            }
         };
      }
   }

   public static enum DisplayState {
      NAME_AND_VALUE,
      VALUE,
      HIDE;

      // $FF: synthetic method
      private static DisplayState[] $values() {
         return new DisplayState[]{NAME_AND_VALUE, VALUE, HIDE};
      }
   }

   @FunctionalInterface
   public interface OnValueChange {
      void onValueChange(CycleButton button, Object value);
   }

   @FunctionalInterface
   public interface SpriteSupplier {
      @Nullable Identifier apply(CycleButton button, Object value);
   }
}
