package ml.mypals.minecartrevolution.mixin.blocks;

import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {DecoratedPotBlock.class, BannerBlock.class,
        PlayerHeadBlock.class, SkullBlock.class})
public abstract class UnrenderableBlocksMixin extends BaseEntityBlock {
    public UnrenderableBlocksMixin(Properties settings) {
        super(settings);
    }
    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }
}
