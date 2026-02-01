package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface SpecialModelRenderer {
   void submit(@Nullable Object argument, ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, final int outlineColor);

   void getExtents(Consumer output);

   @Nullable Object extractArgument(ItemStack stack);

   public interface BakingContext {
      EntityModelSet entityModelSet();

      MaterialSet materials();

      PlayerSkinRenderCache playerSkinRenderCache();

      public static record Simple(EntityModelSet entityModelSet, MaterialSet materials, PlayerSkinRenderCache playerSkinRenderCache) implements BakingContext {
      }
   }

   public interface Unbaked {
      @Nullable SpecialModelRenderer bake(BakingContext context);

      MapCodec type();
   }
}
