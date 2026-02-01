package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger {
   public Codec codec() {
      return ChanneledLightningTrigger.TriggerInstance.CODEC;
   }

   public void trigger(final ServerPlayer player, final Collection victims) {
      List<LootContext> victimsContexts = (List)victims.stream().map((v) -> EntityPredicate.createContext(player, v)).collect(Collectors.toList());
      this.trigger(player, (t) -> t.matches(victimsContexts));
   }

   public static record TriggerInstance(Optional player, List victims) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec CODEC = RecordCodecBuilder.create((i) -> i.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(TriggerInstance::victims)).apply(i, TriggerInstance::new));

      public static Criterion channeledLightning(final EntityPredicate.Builder... victims) {
         return CriteriaTriggers.CHANNELED_LIGHTNING.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(victims)));
      }

      public boolean matches(final Collection victims) {
         for(ContextAwarePredicate predicate : this.victims) {
            boolean found = false;

            for(LootContext victim : victims) {
               if (predicate.matches(victim)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         return true;
      }

      public void validate(final ValidationContextSource validator) {
         SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
         Validatable.validate(validator.entityContext(), "victims", this.victims);
      }
   }
}
