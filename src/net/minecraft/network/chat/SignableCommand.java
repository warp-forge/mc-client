package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.SignedArgument;
import org.jspecify.annotations.Nullable;

public record SignableCommand(List arguments) {
   public static boolean hasSignableArguments(final ParseResults command) {
      return !of(command).arguments().isEmpty();
   }

   public static SignableCommand of(final ParseResults command) {
      String commandString = command.getReader().getString();
      CommandContextBuilder<S> rootContext = command.getContext();
      CommandContextBuilder<S> context = rootContext;

      List<Argument<S>> arguments;
      CommandContextBuilder<S> child;
      for(arguments = collectArguments(commandString, rootContext); (child = context.getChild()) != null && child.getRootNode() != rootContext.getRootNode(); context = child) {
         arguments.addAll(collectArguments(commandString, child));
      }

      return new SignableCommand(arguments);
   }

   private static List collectArguments(final String commandString, final CommandContextBuilder context) {
      List<Argument<S>> arguments = new ArrayList();

      for(ParsedCommandNode node : context.getNodes()) {
         CommandNode var6 = node.getNode();
         if (var6 instanceof ArgumentCommandNode argument) {
            if (argument.getType() instanceof SignedArgument) {
               ParsedArgument<S, ?> parsed = (ParsedArgument)context.getArguments().get(argument.getName());
               if (parsed != null) {
                  String value = parsed.getRange().get(commandString);
                  arguments.add(new Argument(argument, value));
               }
            }
         }
      }

      return arguments;
   }

   public @Nullable Argument getArgument(final String name) {
      for(Argument argument : this.arguments) {
         if (name.equals(argument.name())) {
            return argument;
         }
      }

      return null;
   }

   public static record Argument(ArgumentCommandNode node, String value) {
      public String name() {
         return this.node.getName();
      }
   }
}
