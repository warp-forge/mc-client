package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class EyesLayer extends RenderLayer {
   public EyesLayer(final RenderLayerParent renderer) {
      super(renderer);
   }

   public void submit(final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final EntityRenderState state, final float yRot, final float xRot) {
      submitNodeCollector.order(1).submitModel(this.getParentModel(), state, poseStack, this.renderType(), lightCoords, OverlayTexture.NO_OVERLAY, -1, (TextureAtlasSprite)null, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
   }

   public abstract RenderType renderType();
}
