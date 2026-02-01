package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;

public abstract class AbstractZombieRenderer extends HumanoidMobRenderer {
   private static final Identifier ZOMBIE_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

   protected AbstractZombieRenderer(final EntityRendererProvider.Context context, final ZombieModel model, final ZombieModel babyModel, final ArmorModelSet armorSet, final ArmorModelSet babyArmorSet) {
      super(context, model, babyModel, 0.5F);
      this.addLayer(new HumanoidArmorLayer(this, armorSet, babyArmorSet, context.getEquipmentRenderer()));
   }

   public Identifier getTextureLocation(final ZombieRenderState state) {
      return ZOMBIE_LOCATION;
   }

   public void extractRenderState(final Zombie entity, final ZombieRenderState state, final float partialTicks) {
      super.extractRenderState((Mob)entity, (HumanoidRenderState)state, partialTicks);
      state.isAggressive = entity.isAggressive();
      state.isConverting = entity.isUnderWaterConverting();
   }

   protected boolean isShaking(final ZombieRenderState state) {
      return super.isShaking(state) || state.isConverting;
   }

   protected HumanoidModel.ArmPose getArmPose(final Zombie mob, final HumanoidArm arm) {
      SwingAnimation otherAnim = (SwingAnimation)mob.getItemHeldByArm(arm.getOpposite()).get(DataComponents.SWING_ANIMATION);
      return otherAnim != null && otherAnim.type() == SwingAnimationType.STAB ? HumanoidModel.ArmPose.SPEAR : super.getArmPose(mob, arm);
   }
}
