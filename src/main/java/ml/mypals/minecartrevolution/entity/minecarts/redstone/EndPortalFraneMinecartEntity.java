package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import ml.mypals.minecartrevolution.entity.minecarts.SingleBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class EndPortalFraneMinecartEntity extends RedstoneBlockMinecartEntity implements PowerEmitterMinecartEntity {
    private int entitiesAbove = 0;
    public EndPortalFraneMinecartEntity(EntityType<? extends @NotNull EndPortalFraneMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.HAS_EYE, true);
    }

    @Override
    public @NonNull BlockState getDisplayBlockState() {
        return Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.HAS_EYE, true);
    }

    public EndPortalFraneMinecartEntity(EntityType<? extends SingleBlockMinecartEntity> entityType, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(entityType, world, x, y, z, correspondingItem);
        this.setPreviousBlockPos(this.blockPosition());
    }

    @Override
    public void destroy(@NonNull ServerLevel serverLevel, DamageSource source) {
        super.destroy(serverLevel, source);
        BlockState blockState = getDisplayBlockState();

        updateNeighbors(this.level(), getPreviousBlockPos(), blockState.getBlock());
        updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
    }

    @Override
    public void tick() {
        super.tick();
        BlockState blockState = getDisplayBlockState();
        if(level().getGameTime() % 5 ==0){
            int entitiesAboveNow = level().getEntities(this, this.getBoundingBox().inflate(0,15,0)).size();
            if(entitiesAbove != entitiesAboveNow){
                entitiesAbove = entitiesAboveNow;
                updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
            }

        }
        if (!updatedBlocks || !this.isAlive()) {
            if (getPreviousBlockPos() != null) updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
            updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
        }
        if (this.getPreviousBlockPos() == null || !this.getPreviousBlockPos().equals(this.blockPosition())) {
            if (this.getPreviousBlockPos() == null) this.setPreviousBlockPos(this.blockPosition());
            updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
            this.setPreviousBlockPos(this.blockPosition());
            updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
        }
    }

    @Override
    public int getPowerStrength(Direction direction, BlockPos pos) {

        return Math.min(15, entitiesAbove);
    }

}
