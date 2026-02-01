package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;

public record Criterion(CriterionTrigger trigger, CriterionTriggerInstance triggerInstance) {
   private static final MapCodec MAP_CODEC;
   public static final Codec CODEC;

   private static Codec criterionCodec(final CriterionTrigger trigger) {
      return trigger.codec().xmap((instance) -> new Criterion(trigger, instance), Criterion::triggerInstance);
   }

   static {
      MAP_CODEC = ExtraCodecs.dispatchOptionalValue("trigger", "conditions", CriteriaTriggers.CODEC, Criterion::trigger, Criterion::criterionCodec);
      CODEC = MAP_CODEC.codec();
   }
}
