package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.EquipmentSlot;

public record ArmorModelSet(Object head, Object chest, Object legs, Object feet) {
   public Object get(final EquipmentSlot slot) {
      Object var10000;
      switch (slot) {
         case HEAD -> var10000 = this.head;
         case CHEST -> var10000 = this.chest;
         case LEGS -> var10000 = this.legs;
         case FEET -> var10000 = this.feet;
         default -> throw new IllegalStateException("No model for slot: " + String.valueOf(slot));
      }

      return var10000;
   }

   public ArmorModelSet map(final Function mapper) {
      return new ArmorModelSet(mapper.apply(this.head), mapper.apply(this.chest), mapper.apply(this.legs), mapper.apply(this.feet));
   }

   public void putFrom(final ArmorModelSet values, final ImmutableMap.Builder output) {
      output.put(this.head, (LayerDefinition)values.head);
      output.put(this.chest, (LayerDefinition)values.chest);
      output.put(this.legs, (LayerDefinition)values.legs);
      output.put(this.feet, (LayerDefinition)values.feet);
   }

   public static ArmorModelSet bake(final ArmorModelSet locations, final EntityModelSet modelSet, final Function factory) {
      return locations.map((id) -> (HumanoidModel)factory.apply(modelSet.bakeLayer(id)));
   }
}
