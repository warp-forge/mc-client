package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.FriendlyByteBuf;

public interface ArgumentTypeInfo {
   void serializeToNetwork(Template template, FriendlyByteBuf out);

   Template deserializeFromNetwork(FriendlyByteBuf in);

   void serializeToJson(Template template, JsonObject out);

   Template unpack(final ArgumentType argument);

   public interface Template {
      ArgumentType instantiate(CommandBuildContext context);

      ArgumentTypeInfo type();
   }
}
