package net.minecraft.server.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.api.MethodInfo;
import net.minecraft.server.jsonrpc.api.ParamInfo;
import net.minecraft.server.jsonrpc.api.ResultInfo;
import net.minecraft.server.jsonrpc.api.Schema;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.EncodeJsonRpcException;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import org.jspecify.annotations.Nullable;

public interface IncomingRpcMethod {
   MethodInfo info();

   Attributes attributes();

   JsonElement apply(MinecraftApi minecraftApi, @Nullable JsonElement paramsJson, ClientInfo clientInfo);

   static IncomingRpcMethodBuilder method(final ParameterlessRpcMethodFunction function) {
      return new IncomingRpcMethodBuilder(function);
   }

   static IncomingRpcMethodBuilder method(final RpcMethodFunction function) {
      return new IncomingRpcMethodBuilder(function);
   }

   static IncomingRpcMethodBuilder method(final Function supplier) {
      return new IncomingRpcMethodBuilder(supplier);
   }

   public static record Attributes(boolean runOnMainThread, boolean discoverable) {
   }

   public static record ParameterlessMethod(MethodInfo info, Attributes attributes, ParameterlessRpcMethodFunction supplier) implements IncomingRpcMethod {
      public JsonElement apply(final MinecraftApi minecraftApi, final @Nullable JsonElement paramsJson, final ClientInfo clientInfo) {
         if (paramsJson == null || paramsJson.isJsonArray() && paramsJson.getAsJsonArray().isEmpty()) {
            if (this.info.params().isPresent()) {
               throw new IllegalArgumentException("Parameterless method unexpectedly has parameter description");
            } else {
               Result result = (Result)this.supplier.apply(minecraftApi, clientInfo);
               if (this.info.result().isEmpty()) {
                  throw new IllegalStateException("No result codec defined");
               } else {
                  return (JsonElement)((ResultInfo)this.info.result().get()).schema().codec().encodeStart(JsonOps.INSTANCE, result).getOrThrow(InvalidParameterJsonRpcException::new);
               }
            }
         } else {
            throw new InvalidParameterJsonRpcException("Expected no params, or an empty array");
         }
      }
   }

   public static record Method(MethodInfo info, Attributes attributes, RpcMethodFunction function) implements IncomingRpcMethod {
      public JsonElement apply(final MinecraftApi minecraftApi, final @Nullable JsonElement paramsJson, final ClientInfo clientInfo) {
         if (paramsJson != null && (paramsJson.isJsonArray() || paramsJson.isJsonObject())) {
            if (this.info.params().isEmpty()) {
               throw new IllegalArgumentException("Method defined as having parameters without describing them");
            } else {
               JsonElement paramsJsonElement;
               if (paramsJson.isJsonObject()) {
                  String parameterName = ((ParamInfo)this.info.params().get()).name();
                  JsonElement jsonElement = paramsJson.getAsJsonObject().get(parameterName);
                  if (jsonElement == null) {
                     throw new InvalidParameterJsonRpcException(String.format(Locale.ROOT, "Params passed by-name, but expected param [%s] does not exist", parameterName));
                  }

                  paramsJsonElement = jsonElement;
               } else {
                  JsonArray jsonArray = paramsJson.getAsJsonArray();
                  if (jsonArray.isEmpty() || jsonArray.size() > 1) {
                     throw new InvalidParameterJsonRpcException("Expected exactly one element in the params array");
                  }

                  paramsJsonElement = jsonArray.get(0);
               }

               Params params = (Params)((ParamInfo)this.info.params().get()).schema().codec().parse(JsonOps.INSTANCE, paramsJsonElement).getOrThrow(InvalidParameterJsonRpcException::new);
               Result result = (Result)this.function.apply(minecraftApi, params, clientInfo);
               if (this.info.result().isEmpty()) {
                  throw new IllegalStateException("No result codec defined");
               } else {
                  return (JsonElement)((ResultInfo)this.info.result().get()).schema().codec().encodeStart(JsonOps.INSTANCE, result).getOrThrow(EncodeJsonRpcException::new);
               }
            }
         } else {
            throw new InvalidParameterJsonRpcException("Expected params as array or named");
         }
      }
   }

   public static class IncomingRpcMethodBuilder {
      private String description = "";
      private @Nullable ParamInfo paramInfo;
      private @Nullable ResultInfo resultInfo;
      private boolean discoverable = true;
      private boolean runOnMainThread = true;
      private @Nullable ParameterlessRpcMethodFunction parameterlessFunction;
      private @Nullable RpcMethodFunction parameterFunction;

      public IncomingRpcMethodBuilder(final ParameterlessRpcMethodFunction function) {
         this.parameterlessFunction = function;
      }

      public IncomingRpcMethodBuilder(final RpcMethodFunction function) {
         this.parameterFunction = function;
      }

      public IncomingRpcMethodBuilder(final Function supplier) {
         this.parameterlessFunction = (apiService, clientInfo) -> supplier.apply(apiService);
      }

      public IncomingRpcMethodBuilder description(final String description) {
         this.description = description;
         return this;
      }

      public IncomingRpcMethodBuilder response(final String resultName, final Schema resultSchema) {
         this.resultInfo = new ResultInfo(resultName, resultSchema.info());
         return this;
      }

      public IncomingRpcMethodBuilder param(final String paramName, final Schema paramSchema) {
         this.paramInfo = new ParamInfo(paramName, paramSchema.info());
         return this;
      }

      public IncomingRpcMethodBuilder undiscoverable() {
         this.discoverable = false;
         return this;
      }

      public IncomingRpcMethodBuilder notOnMainThread() {
         this.runOnMainThread = false;
         return this;
      }

      public IncomingRpcMethod build() {
         if (this.resultInfo == null) {
            throw new IllegalStateException("No response defined");
         } else {
            Attributes attributes = new Attributes(this.runOnMainThread, this.discoverable);
            MethodInfo<Params, Result> methodInfo = new MethodInfo(this.description, this.paramInfo, this.resultInfo);
            if (this.parameterlessFunction != null) {
               return new ParameterlessMethod(methodInfo, attributes, this.parameterlessFunction);
            } else if (this.parameterFunction != null) {
               if (this.paramInfo == null) {
                  throw new IllegalStateException("No param schema defined");
               } else {
                  return new Method(methodInfo, attributes, this.parameterFunction);
               }
            } else {
               throw new IllegalStateException("No method defined");
            }
         }
      }

      public IncomingRpcMethod register(final Registry methodRegistry, final String key) {
         return this.register(methodRegistry, Identifier.withDefaultNamespace(key));
      }

      private IncomingRpcMethod register(final Registry methodRegistry, final Identifier id) {
         return (IncomingRpcMethod)Registry.register(methodRegistry, (Identifier)id, this.build());
      }
   }

   @FunctionalInterface
   public interface ParameterlessRpcMethodFunction {
      Object apply(MinecraftApi api, ClientInfo clientInfo);
   }

   @FunctionalInterface
   public interface RpcMethodFunction {
      Object apply(MinecraftApi api, Object params, ClientInfo clientInfo);
   }
}
