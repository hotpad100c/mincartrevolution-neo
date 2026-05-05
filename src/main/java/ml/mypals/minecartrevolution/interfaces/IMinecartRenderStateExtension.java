package ml.mypals.minecartrevolution.interfaces;

import net.minecraft.world.level.block.Block;

public interface IMinecartRenderStateExtension {
    float minecartrevolution$getOpenness();
    void minecartrevolution$setOpenness(float viewers);
    Block minecartrevolution$getDisplayBlock();
    void minecartrevolution$setDisplayBlock(Block block);
}
