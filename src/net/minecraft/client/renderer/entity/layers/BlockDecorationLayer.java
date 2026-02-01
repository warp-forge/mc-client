package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

public class BlockDecorationLayer extends RenderLayer {
   private final Function blockState;
   private final Consumer transform;

   public BlockDecorationLayer(final RenderLayerParent renderer, final Function blockState, final Consumer transform) {
      super(renderer);
      this.blockState = blockState;
      this.transform = transform;
   }

   public void submit(final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, final EntityRenderState state, final float yRot, final float xRot) {
      Optional<BlockState> optionalBlockState = (Optional)this.blockState.apply(state);
      if (!optionalBlockState.isEmpty()) {
         BlockState blockState = (BlockState)optionalBlockState.get();
         Block block = blockState.getBlock();
         boolean isCopperGolemStatue = block instanceof CopperGolemStatueBlock;
         poseStack.pushPose();
         this.transform.accept(poseStack);
         if (!isCopperGolemStatue) {
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0F));
         }

         if (isCopperGolemStatue || block instanceof AbstractSkullBlock || block instanceof AbstractBannerBlock || block instanceof AbstractChestBlock) {
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0F));
         }

         if (block instanceof FlowerBedBlock) {
            poseStack.translate((double)-0.25F, (double)-1.5F, (double)-0.25F);
         } else if (!isCopperGolemStatue) {
            poseStack.translate((double)-0.5F, (double)-1.5F, (double)-0.5F);
         } else {
            poseStack.translate((double)-0.5F, (double)0.0F, (double)-0.5F);
         }

         submitNodeCollector.submitBlock(poseStack, blockState, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
         poseStack.popPose();
      }
   }
}
