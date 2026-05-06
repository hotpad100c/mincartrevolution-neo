package ml.mypals.minecartrevolution.client.renderer.state;

import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.item.DyeColor;
import org.jspecify.annotations.Nullable;

public class ShulkerMinecartRenderState extends MinecartRenderState {
    public float progress;
    @Nullable
    public DyeColor color;
}