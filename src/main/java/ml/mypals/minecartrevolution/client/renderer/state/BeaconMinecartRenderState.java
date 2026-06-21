package ml.mypals.minecartrevolution.client.renderer.state;

import java.util.List;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;

public class BeaconMinecartRenderState extends MinecartRenderState {
  public float animationTime;
  public List<Section> sections;
  public float alpha = 1.0f;

  public record Section(int color, int height) {}
}
