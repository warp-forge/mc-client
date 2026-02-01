package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import org.jspecify.annotations.Nullable;

public class FunctionCommand {
   private static final DynamicCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new DynamicCommandExceptionType((type) -> Component.translatableEscape("commands.function.error.argument_not_compound", type));
   private static final DynamicCommandExceptionType ERROR_NO_FUNCTIONS = new DynamicCommandExceptionType((name) -> Component.translatableEscape("commands.function.scheduled.no_functions", name));
   @VisibleForTesting
   public static final Dynamic2CommandExceptionType ERROR_FUNCTION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType((id, reason) -> Component.translatableEscape("commands.function.instantiationFailure", id, reason));
   public static final SuggestionProvider SUGGEST_FUNCTION = (c, p) -> {
      ServerFunctionManager manager = ((CommandSourceStack)c.getSource()).getServer().getFunctions();
      SharedSuggestionProvider.suggestResource(manager.getTagNames(), p, "#");
      return SharedSuggestionProvider.suggestResource(manager.getFunctionNames(), p);
   };
   private static final Callbacks FULL_CONTEXT_CALLBACKS = new Callbacks() {
      public void signalResult(final CommandSourceStack originalSource, final Identifier id, final int newValue) {
         originalSource.sendSuccess(() -> Component.translatable("commands.function.result", Component.translationArg(id), newValue), true);
      }
   };

   public static void register(final CommandDispatcher dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> sources = Commands.literal("with");

      for(DataCommands.DataProvider provider : DataCommands.SOURCE_PROVIDERS) {
         provider.wrap(sources, (p) -> p.executes(new FunctionCustomExecutor() {
               protected CompoundTag arguments(final CommandContext context) throws CommandSyntaxException {
                  return provider.access(context).getData();
               }
            }).then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(new FunctionCustomExecutor() {
               protected CompoundTag arguments(final CommandContext context) throws CommandSyntaxException {
                  return FunctionCommand.getArgumentTag(NbtPathArgument.getPath(context, "path"), provider.access(context));
               }
            })));
      }

      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("function").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("name", FunctionArgument.functions()).suggests(SUGGEST_FUNCTION).executes(new FunctionCustomExecutor() {
         protected @Nullable CompoundTag arguments(final CommandContext context) {
            return null;
         }
      })).then(Commands.argument("arguments", CompoundTagArgument.compoundTag()).executes(new FunctionCustomExecutor() {
         protected CompoundTag arguments(final CommandContext context) {
            return CompoundTagArgument.getCompoundTag(context, "arguments");
         }
      }))).then(sources)));
   }

   private static CompoundTag getArgumentTag(final NbtPathArgument.NbtPath path, final DataAccessor accessor) throws CommandSyntaxException {
      Tag tag = DataCommands.getSingleTag(path, accessor);
      if (tag instanceof CompoundTag compoundTag) {
         return compoundTag;
      } else {
         throw ERROR_ARGUMENT_NOT_COMPOUND.create(tag.getType().getName());
      }
   }

   public static CommandSourceStack modifySenderForExecution(final CommandSourceStack sender) {
      return sender.withSuppressedOutput().withMaximumPermission(LevelBasedPermissionSet.GAMEMASTER);
   }

   public static void queueFunctions(final Collection functions, final @Nullable CompoundTag arguments, final ExecutionCommandSource originalSource, final ExecutionCommandSource functionSource, final ExecutionControl output, final Callbacks callbacks, final ChainModifiers modifiers) throws CommandSyntaxException {
      if (modifiers.isReturn()) {
         queueFunctionsAsReturn(functions, arguments, originalSource, functionSource, output, callbacks);
      } else {
         queueFunctionsNoReturn(functions, arguments, originalSource, functionSource, output, callbacks);
      }

   }

   private static void instantiateAndQueueFunctions(final @Nullable CompoundTag arguments, final ExecutionControl output, final CommandDispatcher dispatcher, final ExecutionCommandSource noCallbackSource, final CommandFunction function, final Identifier id, final CommandResultCallback functionResultCollector, final boolean returnParentFrame) throws CommandSyntaxException {
      try {
         InstantiatedFunction<T> instantiatedFunction = function.instantiate(arguments, dispatcher);
         output.queueNext((new CallFunction(instantiatedFunction, functionResultCollector, returnParentFrame)).bind(noCallbackSource));
      } catch (FunctionInstantiationException exception) {
         throw ERROR_FUNCTION_INSTANTATION_FAILURE.create(id, exception.messageComponent());
      }
   }

   private static CommandResultCallback decorateOutputIfNeeded(final ExecutionCommandSource originalSource, final Callbacks callbacks, final Identifier id, final CommandResultCallback callback) {
      return originalSource.isSilent() ? callback : (success, result) -> {
         callbacks.signalResult(originalSource, id, result);
         callback.onResult(success, result);
      };
   }

   private static void queueFunctionsAsReturn(final Collection functions, final @Nullable CompoundTag arguments, final ExecutionCommandSource originalSource, final ExecutionCommandSource functionSource, final ExecutionControl output, final Callbacks callbacks) throws CommandSyntaxException {
      CommandDispatcher<T> dispatcher = originalSource.dispatcher();
      T noCallbackSource = (T)functionSource.clearCallbacks();
      CommandResultCallback functionCommandOutputCallback = CommandResultCallback.chain(originalSource.callback(), output.currentFrame().returnValueConsumer());

      for(CommandFunction function : functions) {
         Identifier id = function.id();
         CommandResultCallback functionResultCollector = decorateOutputIfNeeded(originalSource, callbacks, id, functionCommandOutputCallback);
         instantiateAndQueueFunctions(arguments, output, dispatcher, noCallbackSource, function, id, functionResultCollector, true);
      }

      output.queueNext(FallthroughTask.instance());
   }

   private static void queueFunctionsNoReturn(final Collection functions, final @Nullable CompoundTag arguments, final ExecutionCommandSource originalSource, final ExecutionCommandSource functionSource, final ExecutionControl output, final Callbacks callbacks) throws CommandSyntaxException {
      CommandDispatcher<T> dispatcher = originalSource.dispatcher();
      T noCallbackSource = (T)functionSource.clearCallbacks();
      CommandResultCallback originalCallback = originalSource.callback();
      if (!functions.isEmpty()) {
         if (functions.size() == 1) {
            CommandFunction<T> function = (CommandFunction)functions.iterator().next();
            Identifier id = function.id();
            CommandResultCallback functionResultCollector = decorateOutputIfNeeded(originalSource, callbacks, id, originalCallback);
            instantiateAndQueueFunctions(arguments, output, dispatcher, noCallbackSource, function, id, functionResultCollector, false);
         } else if (originalCallback == CommandResultCallback.EMPTY) {
            for(CommandFunction function : functions) {
               Identifier id = function.id();
               CommandResultCallback functionResultCollector = decorateOutputIfNeeded(originalSource, callbacks, id, originalCallback);
               instantiateAndQueueFunctions(arguments, output, dispatcher, noCallbackSource, function, id, functionResultCollector, false);
            }
         } else {
            class Accumulator {
               private boolean anyResult;
               private int sum;

               public void add(final int result) {
                  this.anyResult = true;
                  this.sum += result;
               }
            }

            Accumulator accumulator = new Accumulator();
            CommandResultCallback partialResultCallback = (success, result) -> accumulator.add(result);

            for(CommandFunction function : functions) {
               Identifier id = function.id();
               CommandResultCallback functionResultCollector = decorateOutputIfNeeded(originalSource, callbacks, id, partialResultCallback);
               instantiateAndQueueFunctions(arguments, output, dispatcher, noCallbackSource, function, id, functionResultCollector, false);
            }

            output.queueNext((context, frame) -> {
               if (accumulator.anyResult) {
                  originalCallback.onSuccess(accumulator.sum);
               }

            });
         }

      }
   }

   private abstract static class FunctionCustomExecutor extends CustomCommandExecutor.WithErrorHandling implements CustomCommandExecutor.CommandAdapter {
      protected abstract @Nullable CompoundTag arguments(final CommandContext context) throws CommandSyntaxException;

      public void runGuarded(final CommandSourceStack sender, final ContextChain currentStep, final ChainModifiers modifiers, final ExecutionControl output) throws CommandSyntaxException {
         CommandContext<CommandSourceStack> currentContext = currentStep.getTopContext().copyFor(sender);
         Pair<Identifier, Collection<CommandFunction<CommandSourceStack>>> nameAndFunctions = FunctionArgument.getFunctionCollection(currentContext, "name");
         Collection<CommandFunction<CommandSourceStack>> functions = (Collection)nameAndFunctions.getSecond();
         if (functions.isEmpty()) {
            throw FunctionCommand.ERROR_NO_FUNCTIONS.create(Component.translationArg((Identifier)nameAndFunctions.getFirst()));
         } else {
            CompoundTag arguments = this.arguments(currentContext);
            CommandSourceStack commonFunctionContext = FunctionCommand.modifySenderForExecution(sender);
            if (functions.size() == 1) {
               sender.sendSuccess(() -> Component.translatable("commands.function.scheduled.single", Component.translationArg(((CommandFunction)functions.iterator().next()).id())), true);
            } else {
               sender.sendSuccess(() -> Component.translatable("commands.function.scheduled.multiple", ComponentUtils.formatList(functions.stream().map(CommandFunction::id).toList(), (Function)(Component::translationArg))), true);
            }

            FunctionCommand.queueFunctions(functions, arguments, sender, commonFunctionContext, output, FunctionCommand.FULL_CONTEXT_CALLBACKS, modifiers);
         }
      }
   }

   public interface Callbacks {
      void signalResult(Object originalSource, Identifier functionId, int newValue);
   }
}
