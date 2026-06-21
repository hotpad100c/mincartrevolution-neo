package ml.mypals.minecartrevolution.client.renderer;

import java.util.List;
import ml.mypals.minecartrevolution.entity.chain.ChainEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class ChainRenderState extends EntityRenderState {
  public List<ChainEntity.ChainSegment> segments;
  public AbstractMinecart cartA;
  public AbstractMinecart cartB;
  public float partialTicks;
}
