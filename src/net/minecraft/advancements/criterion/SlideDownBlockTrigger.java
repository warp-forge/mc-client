package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger {
   public Codec codec() {
      return SlideDownBlockTrigger.TriggerInstance.CODEC;
   }

   public void trigger(final ServerPlayer player, final BlockState state) {
      this.trigger(player, (t) -> t.matches(state));
   }

   public static record TriggerInstance(Optional player, Optional block, Optional state) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(TriggerInstance::block), StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(TriggerInstance::state)).apply(i, TriggerInstance::new)).validate(TriggerInstance::validate);

      private static DataResult validate(final TriggerInstance trigger) {
         return (DataResult)trigger.block.flatMap((block) -> trigger.state.flatMap((state) -> state.checkState(((Block)block.value()).getStateDefinition())).map((property) -> DataResult.error(() -> {
                  String var10000 = String.valueOf(block);
                  return "Block" + var10000 + " has no property " + property;
               }))).orElseGet(() -> DataResult.success(trigger));
      }

      public static Criterion slidesDownBlock(final Block block) {
         return CriteriaTriggers.HONEY_BLOCK_SLIDE.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
      }

      public boolean matches(final BlockState state) {
         if (this.block.isPresent() && !state.is((Holder)this.block.get())) {
            return false;
         } else {
            return !this.state.isPresent() || ((StatePropertiesPredicate)this.state.get()).matches(state);
         }
      }
   }
}
