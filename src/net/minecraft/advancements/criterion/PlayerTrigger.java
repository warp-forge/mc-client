package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger {
   public Codec codec() {
      return PlayerTrigger.TriggerInstance.CODEC;
   }

   public void trigger(final ServerPlayer player) {
      this.trigger(player, (t) -> true);
   }

   public static record TriggerInstance(Optional player) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)).apply(i, TriggerInstance::new));

      public static Criterion located(final LocationPredicate.Builder location) {
         return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().located(location)))));
      }

      public static Criterion located(final EntityPredicate.Builder player) {
         return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player.build()))));
      }

      public static Criterion located(final Optional player) {
         return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(EntityPredicate.wrap(player)));
      }

      public static Criterion sleptInBed() {
         return CriteriaTriggers.SLEPT_IN_BED.createCriterion(new TriggerInstance(Optional.empty()));
      }

      public static Criterion raidWon() {
         return CriteriaTriggers.RAID_WIN.createCriterion(new TriggerInstance(Optional.empty()));
      }

      public static Criterion avoidVibration() {
         return CriteriaTriggers.AVOID_VIBRATION.createCriterion(new TriggerInstance(Optional.empty()));
      }

      public static Criterion tick() {
         return CriteriaTriggers.TICK.createCriterion(new TriggerInstance(Optional.empty()));
      }

      public static Criterion walkOnBlockWithEquipment(final HolderGetter blocks, final HolderGetter items, final Block stepOnBlock, final Item requiredEquipment) {
         return located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(items, requiredEquipment))).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, stepOnBlock))));
      }
   }
}
