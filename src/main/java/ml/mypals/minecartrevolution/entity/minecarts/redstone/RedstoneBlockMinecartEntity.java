package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import ml.mypals.minecartrevolution.entity.minecarts.ItemBoundBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class RedstoneBlockMinecartEntity extends ItemBoundBlockMinecartEntity implements PowerEmitterMinecartEntity {
    public RedstoneBlockMinecartEntity(EntityType<? extends @NotNull RedstoneBlockMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    public RedstoneBlockMinecartEntity(EntityType<? extends ItemBoundBlockMinecartEntity> entityType, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(entityType, world, x, y, z, correspondingItem);
        this.setPreviousBlockPos(this.blockPosition());
    }

    @Override
    public void destroy(@NonNull ServerLevel serverLevel, DamageSource source) {
        super.destroy(serverLevel, source);
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

        updateNeighbors(this.level(), getPreviousBlockPos(), blockState.getBlock());
        updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
    }

    @Override
    public void tick() {
        super.tick();
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

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
        if (!this.isAlive()) {
            return 0;
        }
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

        return Math.max(blockState.getSignal(this.level(), pos, direction), blockState.getDirectSignal(this.level(), pos, direction));
    }

}
