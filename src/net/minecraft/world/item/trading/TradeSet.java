package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class TradeSet {
   public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(RegistryCodecs.homogeneousList(Registries.VILLAGER_TRADE).fieldOf("trades").forGetter((tradeSet) -> tradeSet.trades), NumberProviders.CODEC.fieldOf("amount").forGetter((tradeSet) -> tradeSet.amount), Codec.BOOL.optionalFieldOf("allow_duplicates", false).forGetter((tradeSet) -> tradeSet.allowDuplicates), Identifier.CODEC.optionalFieldOf("random_sequence").forGetter((t) -> t.randomSequence)).apply(i, TradeSet::new));
   private final HolderSet trades;
   private final NumberProvider amount;
   private final boolean allowDuplicates;
   private final Optional randomSequence;

   public TradeSet(final HolderSet trades, final NumberProvider amount, final boolean allowDuplicates, final Optional randomSequence) {
      this.trades = trades;
      this.amount = amount;
      this.allowDuplicates = allowDuplicates;
      this.randomSequence = randomSequence;
   }

   public HolderSet getTrades() {
      return this.trades;
   }

   public int calculateNumberOfTrades(final LootContext lootContext) {
      return this.amount.getInt(lootContext);
   }

   public boolean allowDuplicates() {
      return this.allowDuplicates;
   }

   public Optional randomSequence() {
      return this.randomSequence;
   }
}
