package ml.mypals.minecartrevolution.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CompatFriendlyBlockRenderState extends MinecartRenderState {
    public BlockEntityRenderState beRenderState;
    public BlockEntity be;
    public LevelRenderState levelRenderState;
}
