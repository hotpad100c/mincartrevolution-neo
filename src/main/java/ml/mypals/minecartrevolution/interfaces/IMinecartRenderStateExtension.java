package ml.mypals.minecartrevolution.interfaces;

import net.minecraft.world.level.block.state.BlockState;

public interface IMinecartRenderStateExtension {
  float minecartrevolution$getOpenness();

  void minecartrevolution$setOpenness(float viewers);

  BlockState minecartrevolution$getDisplayBlock();

  void minecartrevolution$setDisplayBlock(BlockState block);
}
