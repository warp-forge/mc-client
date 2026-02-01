package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;

public interface RangeArgument extends ArgumentType {
   static Ints intRange() {
      return new Ints();
   }

   static Floats floatRange() {
      return new Floats();
   }

   public static class Ints implements RangeArgument {
      private static final Collection EXAMPLES = Arrays.asList("0..5", "0", "-5", "-100..", "..100");

      public static MinMaxBounds.Ints getRange(final CommandContext context, final String name) {
         return (MinMaxBounds.Ints)context.getArgument(name, MinMaxBounds.Ints.class);
      }

      public MinMaxBounds.Ints parse(final StringReader reader) throws CommandSyntaxException {
         return MinMaxBounds.Ints.fromReader(reader);
      }

      public Collection getExamples() {
         return EXAMPLES;
      }
   }

   public static class Floats implements RangeArgument {
      private static final Collection EXAMPLES = Arrays.asList("0..5.2", "0", "-5.4", "-100.76..", "..100");

      public static MinMaxBounds.Doubles getRange(final CommandContext context, final String name) {
         return (MinMaxBounds.Doubles)context.getArgument(name, MinMaxBounds.Doubles.class);
      }

      public MinMaxBounds.Doubles parse(final StringReader reader) throws CommandSyntaxException {
         return MinMaxBounds.Doubles.fromReader(reader);
      }

      public Collection getExamples() {
         return EXAMPLES;
      }
   }
}
