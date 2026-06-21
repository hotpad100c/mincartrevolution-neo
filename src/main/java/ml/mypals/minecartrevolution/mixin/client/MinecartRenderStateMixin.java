package ml.mypals.minecartrevolution.mixin.client;

import ml.mypals.minecartrevolution.interfaces.IMinecartRenderStateExtension;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecartRenderState.class)
public class MinecartRenderStateMixin implements IMinecartRenderStateExtension {
  @Unique private float minecartrevolution$openess = 0;
  @Unique private BlockState minecartrevolution$displayBlock = Blocks.AIR.defaultBlockState();

  @Override
  public float minecartrevolution$getOpenness() {
    return minecartrevolution$openess;
  }

  @Override
  public void minecartrevolution$setOpenness(float openess) {
    minecartrevolution$openess = openess;
  }

  @Override
  public BlockState minecartrevolution$getDisplayBlock() {
    return minecartrevolution$displayBlock;
  }

  @Override
  public void minecartrevolution$setDisplayBlock(BlockState block) {
    minecartrevolution$displayBlock = block;
  }
}
