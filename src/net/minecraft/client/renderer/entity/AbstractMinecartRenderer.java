package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public abstract class AbstractMinecartRenderer extends EntityRenderer {
   private static final Identifier MINECART_LOCATION = Identifier.withDefaultNamespace("textures/entity/minecart/minecart.png");
   private static final float DISPLAY_BLOCK_SCALE = 0.75F;
   protected final MinecartModel model;

   public AbstractMinecartRenderer(final EntityRendererProvider.Context context, final ModelLayerLocation model) {
      super(context);
      this.shadowRadius = 0.7F;
      this.model = new MinecartModel(context.bakeLayer(model));
   }

   public void submit(final MinecartRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
      super.submit(state, poseStack, submitNodeCollector, camera);
      poseStack.pushPose();
      long seed = state.offsetSeed;
      float offsetX = (((float)(seed >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float offsetY = (((float)(seed >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float offsetZ = (((float)(seed >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      poseStack.translate(offsetX, offsetY, offsetZ);
      if (state.isNewRender) {
         newRender(state, poseStack);
      } else {
         oldRender(state, poseStack);
      }

      float hurt = state.hurtTime;
      if (hurt > 0.0F) {
         poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.sin((double)hurt) * hurt * state.damageTime / 10.0F * (float)state.hurtDir));
      }

      BlockState blockState = state.displayBlockState;
      if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
         poseStack.pushPose();
         poseStack.scale(0.75F, 0.75F, 0.75F);
         poseStack.translate(-0.5F, (float)(state.displayOffset - 8) / 16.0F, 0.5F);
         poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0F));
         this.submitMinecartContents(state, blockState, poseStack, submitNodeCollector, state.lightCoords);
         poseStack.popPose();
      }

      poseStack.scale(-1.0F, -1.0F, 1.0F);
      submitNodeCollector.submitModel(this.model, state, poseStack, this.model.renderType(MINECART_LOCATION), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
      poseStack.popPose();
   }

   private static void newRender(final MinecartRenderState state, final PoseStack poseStack) {
      poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(state.yRot));
      poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-state.xRot));
      poseStack.translate(0.0F, 0.375F, 0.0F);
   }

   private static void oldRender(final MinecartRenderState state, final PoseStack poseStack) {
      double entityX = state.x;
      double entityY = state.y;
      double entityZ = state.z;
      float xRot = state.xRot;
      float rotation = state.yRot;
      if (state.posOnRail != null && state.frontPos != null && state.backPos != null) {
         Vec3 frontPos = state.frontPos;
         Vec3 backPos = state.backPos;
         poseStack.translate(state.posOnRail.x - entityX, (frontPos.y + backPos.y) / (double)2.0F - entityY, state.posOnRail.z - entityZ);
         Vec3 direction = backPos.add(-frontPos.x, -frontPos.y, -frontPos.z);
         if (direction.length() != (double)0.0F) {
            direction = direction.normalize();
            rotation = (float)(Math.atan2(direction.z, direction.x) * (double)180.0F / Math.PI);
            xRot = (float)(Math.atan(direction.y) * (double)73.0F);
         }
      }

      poseStack.translate(0.0F, 0.375F, 0.0F);
      poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0F - rotation));
      poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-xRot));
   }

   public void extractRenderState(final AbstractMinecart entity, final MinecartRenderState state, final float partialTicks) {
      super.extractRenderState(entity, state, partialTicks);
      MinecartBehavior var6 = entity.getBehavior();
      if (var6 instanceof NewMinecartBehavior behavior) {
         newExtractState(entity, behavior, state, partialTicks);
         state.isNewRender = true;
      } else {
         var6 = entity.getBehavior();
         if (var6 instanceof OldMinecartBehavior behavior) {
            oldExtractState(entity, behavior, state, partialTicks);
            state.isNewRender = false;
         }
      }

      long seed = (long)entity.getId() * 493286711L;
      state.offsetSeed = seed * seed * 4392167121L + seed * 98761L;
      state.hurtTime = (float)entity.getHurtTime() - partialTicks;
      state.hurtDir = entity.getHurtDir();
      state.damageTime = Math.max(entity.getDamage() - partialTicks, 0.0F);
      state.displayOffset = entity.getDisplayOffset();
      state.displayBlockState = entity.getDisplayBlockState();
   }

   private static void newExtractState(final AbstractMinecart entity, final NewMinecartBehavior behavior, final MinecartRenderState state, final float partialTicks) {
      if (behavior.cartHasPosRotLerp()) {
         state.renderPos = behavior.getCartLerpPosition(partialTicks);
         state.xRot = behavior.getCartLerpXRot(partialTicks);
         state.yRot = behavior.getCartLerpYRot(partialTicks);
      } else {
         state.renderPos = null;
         state.xRot = entity.getXRot();
         state.yRot = entity.getYRot();
      }

   }

   private static void oldExtractState(final AbstractMinecart entity, final OldMinecartBehavior behavior, final MinecartRenderState state, final float partialTicks) {
      float HALF_LENGTH = 0.3F;
      state.xRot = entity.getXRot(partialTicks);
      state.yRot = entity.getYRot(partialTicks);
      double entityX = state.x;
      double entityY = state.y;
      double entityZ = state.z;
      Vec3 pos = behavior.getPos(entityX, entityY, entityZ);
      if (pos != null) {
         state.posOnRail = pos;
         Vec3 p0 = behavior.getPosOffs(entityX, entityY, entityZ, (double)0.3F);
         Vec3 p1 = behavior.getPosOffs(entityX, entityY, entityZ, (double)-0.3F);
         state.frontPos = (Vec3)Objects.requireNonNullElse(p0, pos);
         state.backPos = (Vec3)Objects.requireNonNullElse(p1, pos);
      } else {
         state.posOnRail = null;
         state.frontPos = null;
         state.backPos = null;
      }

   }

   protected void submitMinecartContents(final MinecartRenderState state, final BlockState blockState, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords) {
      submitNodeCollector.submitBlock(poseStack, blockState, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
   }

   protected AABB getBoundingBoxForCulling(final AbstractMinecart entity) {
      AABB aabb = super.getBoundingBoxForCulling(entity);
      return !entity.getDisplayBlockState().isAir() ? aabb.expandTowards((double)0.0F, (double)((float)entity.getDisplayOffset() * 0.75F / 16.0F), (double)0.0F) : aabb;
   }

   public Vec3 getRenderOffset(final MinecartRenderState state) {
      Vec3 offset = super.getRenderOffset(state);
      return state.isNewRender && state.renderPos != null ? offset.add(state.renderPos.x - state.x, state.renderPos.y - state.y, state.renderPos.z - state.z) : offset;
   }
}
