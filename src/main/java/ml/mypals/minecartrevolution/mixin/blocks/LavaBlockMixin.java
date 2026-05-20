package ml.mypals.minecartrevolution.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.MapCodec;
import ml.mypals.minecartrevolution.entity.minecarts.ObsidianMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(LiquidBlock.class)
public abstract class LavaBlockMixin {
    @Shadow
    protected abstract Optional<LivingEntity> ifMobIsColliding(CollisionContext context);

    @Shadow
    public abstract MapCodec<LiquidBlock> codec();

    @Shadow
    @Final
    public static IntegerProperty LEVEL;

    @WrapMethod(method = "getCollisionShape")
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, Operation<VoxelShape> original) {
        if (state.is(Blocks.LAVA) && mincartrevolution_neo$ifObsidianMinecartIsColliding(context)) {
            int lavaLevel = state.getValue(LEVEL);

            double fluidHeight;
            if (lavaLevel == 0) {
                fluidHeight = 0.7;
            } else {
                fluidHeight = ((double) (8 - lavaLevel) / 16) - 0.1;
            }

            double heightInVoxels = fluidHeight;

            return Shapes.create(0, 0, 0, 1, heightInVoxels, 1);
        }

        return original.call(state, level, pos, context);
    }
    @Unique
    private boolean mincartrevolution_neo$ifObsidianMinecartIsColliding(CollisionContext context) {
        return context instanceof EntityCollisionContext entityCollisionContext
               && entityCollisionContext.getEntity() instanceof ObsidianMinecartEntity minecart;
    }
}
