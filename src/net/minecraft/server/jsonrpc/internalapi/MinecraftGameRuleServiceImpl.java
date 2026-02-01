package net.minecraft.server.jsonrpc.internalapi;

import java.util.stream.Stream;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.GameRulesService;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

public class MinecraftGameRuleServiceImpl implements MinecraftGameRuleService {
   private final DedicatedServer server;
   private final GameRules gameRules;
   private final JsonRpcLogger jsonrpcLogger;

   public MinecraftGameRuleServiceImpl(final DedicatedServer server, final JsonRpcLogger jsonrpcLogger) {
      this.server = server;
      this.gameRules = server.getWorldData().getGameRules();
      this.jsonrpcLogger = jsonrpcLogger;
   }

   public GameRulesService.GameRuleUpdate updateGameRule(final GameRulesService.GameRuleUpdate update, final ClientInfo clientInfo) {
      GameRule<T> gameRule = update.gameRule();
      T oldValue = (T)this.gameRules.get(gameRule);
      T newValue = (T)update.value();
      this.gameRules.set(gameRule, newValue, this.server);
      this.jsonrpcLogger.log(clientInfo, "Game rule '{}' updated from '{}' to '{}'", gameRule.id(), gameRule.serialize(oldValue), gameRule.serialize(newValue));
      return update;
   }

   public GameRulesService.GameRuleUpdate getTypedRule(final GameRule gameRule, final Object value) {
      return new GameRulesService.GameRuleUpdate(gameRule, value);
   }

   public Stream getAvailableGameRules() {
      return this.gameRules.availableRules();
   }

   public Object getRuleValue(final GameRule gameRule) {
      return this.gameRules.get(gameRule);
   }
}
