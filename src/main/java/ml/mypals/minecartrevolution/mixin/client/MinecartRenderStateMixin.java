package ml.mypals.minecartrevolution.mixin.client;

import ml.mypals.minecartrevolution.interfaces.IMinecartRenderStateExtension;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecartRenderState.class)
public class MinecartRenderStateMixin implements IMinecartRenderStateExtension {
    @Unique
    private float minecartrevolution$openess = 0;
    @Unique
    private Block minecartrevolution$displayBlock = Blocks.AIR;
    @Override
    public float minecartrevolution$getOpenness() {
        return minecartrevolution$openess;
    }

    @Override
    public void minecartrevolution$setOpenness(float openess) {
        minecartrevolution$openess = openess;
    }

    @Override
    public Block minecartrevolution$getDisplayBlock() {
        return minecartrevolution$displayBlock;
    }

    @Override
    public void minecartrevolution$setDisplayBlock(Block block) {
        minecartrevolution$displayBlock = block;
    }
}
