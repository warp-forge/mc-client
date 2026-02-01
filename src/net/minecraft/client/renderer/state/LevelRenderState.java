package net.minecraft.client.renderer.state;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class LevelRenderState {
   public CameraRenderState cameraRenderState = new CameraRenderState();
   public final List entityRenderStates = new ArrayList();
   public final List blockEntityRenderStates = new ArrayList();
   public boolean haveGlowingEntities;
   public @Nullable BlockOutlineRenderState blockOutlineRenderState;
   public final List blockBreakingRenderStates = new ArrayList();
   public final WeatherRenderState weatherRenderState = new WeatherRenderState();
   public final WorldBorderRenderState worldBorderRenderState = new WorldBorderRenderState();
   public final SkyRenderState skyRenderState = new SkyRenderState();
   public long gameTime;
   public int lastEntityRenderStateCount;

   public void reset() {
      this.entityRenderStates.clear();
      this.blockEntityRenderStates.clear();
      this.blockBreakingRenderStates.clear();
      this.haveGlowingEntities = false;
      this.blockOutlineRenderState = null;
      this.weatherRenderState.reset();
      this.worldBorderRenderState.reset();
      this.skyRenderState.reset();
      this.gameTime = 0L;
   }
}
