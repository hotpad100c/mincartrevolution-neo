package ml.mypals.minecartrevolution.client.renderer.state;

import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import java.util.List;

public class BeaconMinecartRenderState extends MinecartRenderState {
    public float animationTime;
    public List<Section> sections;
    public float alpha = 1.0f;

    public record Section(int color, int height) {}
}
