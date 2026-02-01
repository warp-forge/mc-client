package net.minecraft.client.renderer.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class CameraRenderState {
   public BlockPos blockPos;
   public Vec3 pos;
   public boolean initialized;
   public Vec3 entityPos;
   public Quaternionf orientation;

   public CameraRenderState() {
      this.blockPos = BlockPos.ZERO;
      this.pos = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
      this.entityPos = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
      this.orientation = new Quaternionf();
   }
}
