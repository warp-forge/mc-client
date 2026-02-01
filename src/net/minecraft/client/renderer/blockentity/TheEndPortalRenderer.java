package net.minecraft.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;

public class TheEndPortalRenderer extends AbstractEndPortalRenderer {
   public EndPortalRenderState createRenderState() {
      return new EndPortalRenderState();
   }
}
