package net.minecraft.server.jsonrpc;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import org.jspecify.annotations.Nullable;

public interface OutgoingRpcMethod {
   String NOTIFICATION_PREFIX = "notification/";

   MethodInfo info();

   Attributes attributes();

   default @Nullable JsonElement encodeParams(final Object params) {
      return null;
   }

   default @Nullable Object decodeResult(final JsonElement result) {
      return null;
   }

   static OutgoingRpcMethodBuilder notification() {
      return new OutgoingRpcMethodBuilder(ParmeterlessNotification::new);
   }

   static OutgoingRpcMethodBuilder notificationWithParams() {
      return new OutgoingRpcMethodBuilder(Notification::new);
   }

   static OutgoingRpcMethodBuilder request() {
      return new OutgoingRpcMethodBuilder(ParameterlessMethod::new);
   }

   static OutgoingRpcMethodBuilder requestWithParams() {
      return new OutgoingRpcMethodBuilder(Method::new);
   }

   public static record Attributes(boolean discoverable) {
   }

   public static record ParmeterlessNotification(MethodInfo info, Attributes attributes) implements OutgoingRpcMethod {
   }

   public static record Notification(MethodInfo info, Attributes attributes) implements OutgoingRpcMethod {
      public @Nullable JsonElement encodeParams(final Object params) {
         if (this.info.params().isEmpty()) {
            throw new IllegalStateException("Method defined as having no parameters");
         } else {
            return (JsonElement)((ParamInfo)this.info.params().get()).schema().codec().encodeStart(JsonOps.INSTANCE, params).getOrThrow();
         }
      }
   }

   public static record ParameterlessMethod(MethodInfo info, Attributes attributes) implements OutgoingRpcMethod {
      public Object decodeResult(final JsonElement result) {
         if (this.info.result().isEmpty()) {
            throw new IllegalStateException("Method defined as having no result");
         } else {
            return ((ResultInfo)this.info.result().get()).schema().codec().parse(JsonOps.INSTANCE, result).getOrThrow();
         }
      }
   }

   public static record Method(MethodInfo info, Attributes attributes) implements OutgoingRpcMethod {
      public @Nullable JsonElement encodeParams(final Object params) {
         if (this.info.params().isEmpty()) {
            throw new IllegalStateException("Method defined as having no parameters");
         } else {
            return (JsonElement)((ParamInfo)this.info.params().get()).schema().codec().encodeStart(JsonOps.INSTANCE, params).getOrThrow();
         }
      }

      public Object decodeResult(final JsonElement result) {
         if (this.info.result().isEmpty()) {
            throw new IllegalStateException("Method defined as having no result");
         } else {
            return ((ResultInfo)this.info.result().get()).schema().codec().parse(JsonOps.INSTANCE, result).getOrThrow();
         }
      }
   }

   public static class OutgoingRpcMethodBuilder {
      public static final Attributes DEFAULT_ATTRIBUTES = new Attributes(true);
      private final Factory method;
      private String description = "";
      private @Nullable ParamInfo paramInfo;
      private @Nullable ResultInfo resultInfo;

      public OutgoingRpcMethodBuilder(final Factory method) {
         this.method = method;
      }

      public OutgoingRpcMethodBuilder description(final String description) {
         this.description = description;
         return this;
      }

      public OutgoingRpcMethodBuilder response(final String resultName, final Schema resultSchema) {
         this.resultInfo = new ResultInfo(resultName, resultSchema);
         return this;
      }

      public OutgoingRpcMethodBuilder param(final String paramName, final Schema paramSchema) {
         this.paramInfo = new ParamInfo(paramName, paramSchema);
         return this;
      }

      private OutgoingRpcMethod build() {
         MethodInfo<Params, Result> methodInfo = new MethodInfo(this.description, this.paramInfo, this.resultInfo);
         return this.method.create(methodInfo, DEFAULT_ATTRIBUTES);
      }

      public Holder.Reference register(final String key) {
         return this.register(Identifier.withDefaultNamespace("notification/" + key));
      }

      private Holder.Reference register(final Identifier id) {
         return Registry.registerForHolder(BuiltInRegistries.OUTGOING_RPC_METHOD, (Identifier)id, this.build());
      }
   }

   @FunctionalInterface
   public interface Factory {
      OutgoingRpcMethod create(MethodInfo info, Attributes attributes);
   }
}
