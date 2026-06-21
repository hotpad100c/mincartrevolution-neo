package ml.mypals.minecartrevolution.client.renderer.state;

import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;

public class ScaffoldMinecartRenderState extends MinecartRenderState {
  public ItemClusterRenderState[] items = new ItemClusterRenderState[8];

  public ScaffoldMinecartRenderState() {
    for (int i = 0; i < 8; i++) {
      items[i] = null;
    }
  }
}
