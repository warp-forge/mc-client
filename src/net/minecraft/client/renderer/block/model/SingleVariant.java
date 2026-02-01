package net.minecraft.client.renderer.block.model;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RandomSource;

public class SingleVariant implements BlockStateModel {
   private final BlockModelPart model;

   public SingleVariant(final BlockModelPart model) {
      this.model = model;
   }

   public void collectParts(final RandomSource random, final List output) {
      output.add(this.model);
   }

   public TextureAtlasSprite particleIcon() {
      return this.model.particleIcon();
   }

   public static record Unbaked(Variant variant) implements BlockStateModel.Unbaked {
      public static final Codec CODEC;

      public BlockStateModel bake(final ModelBaker modelBakery) {
         return new SingleVariant(this.variant.bake(modelBakery));
      }

      public void resolveDependencies(final ResolvableModel.Resolver resolver) {
         this.variant.resolveDependencies(resolver);
      }

      static {
         CODEC = Variant.CODEC.xmap(Unbaked::new, Unbaked::variant);
      }
   }
}
