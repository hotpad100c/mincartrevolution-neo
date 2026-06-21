package ml.mypals.minecartrevolution.client.renderer.state;

import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.level.block.state.BlockState;

public class WorkingMinecartRenderState extends MinecartRenderState {
  public float flip;
  public float open;
  public float yRot;
  public float time;
  public BlockState displayBlock;
}
