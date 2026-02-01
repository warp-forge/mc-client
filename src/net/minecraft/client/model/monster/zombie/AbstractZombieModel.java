package net.minecraft.client.model.monster.zombie;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

public abstract class AbstractZombieModel extends HumanoidModel {
   protected AbstractZombieModel(final ModelPart root) {
      super(root);
   }

   public void setupAnim(final ZombieRenderState state) {
      super.setupAnim((HumanoidRenderState)state);
      AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, state.isAggressive, state);
   }
}
