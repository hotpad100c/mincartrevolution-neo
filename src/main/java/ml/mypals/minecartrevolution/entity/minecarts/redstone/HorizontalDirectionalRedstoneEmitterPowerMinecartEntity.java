package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.util.MinecartRotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class HorizontalDirectionalRedstoneEmitterPowerMinecartEntity extends RedstoneBlockMinecartEntity {
    public HorizontalDirectionalRedstoneEmitterPowerMinecartEntity(EntityType<? extends RedstoneBlockMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    public HorizontalDirectionalRedstoneEmitterPowerMinecartEntity(EntityType<? extends RedstoneBlockMinecartEntity> entityType, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(entityType, world, x, y, z, correspondingItem);
        this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).ifPresent(blockState -> {
            if (blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                this.setCustomDisplayBlockState(Optional.of(blockState.setValue(HorizontalDirectionalBlock.FACING, Direction.WEST)));
            }
        });
    }

    public boolean shouldPowerSide(Direction direction) {
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        if (blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            Direction blockFacing = blockState.getValue(HorizontalDirectionalBlock.FACING);
            return calculateBlockFacing(blockFacing) == direction;
        }
        return false;
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        if (blockState.hasProperty(HorizontalDirectionalBlock.FACING))
            return blockState.setValue(HorizontalDirectionalBlock.FACING, Direction.WEST);
        else return blockState;
    }

    public Direction calculateBlockFacing(Direction blockFacing) {
        return MinecartRotationUtils.getAbsoluteDirection(blockFacing, this.getYRot());
    }

    @Override
    public void tick() {
        super.tick();
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

        if (blockState.hasProperty(DiodeBlock.POWERED) && getPower(this.level(), this.blockPosition(), blockState) > 0) {
            this.setCustomDisplayBlockState(Optional.of(getDefaultDisplayBlockState().setValue(DiodeBlock.POWERED, true)));
            this.updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
        } else {
            this.setCustomDisplayBlockState(Optional.of(getDefaultDisplayBlockState().setValue(DiodeBlock.POWERED, false)));
            this.updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
        }
    }

    @Override
    public int getPowerStrength(Direction direction, BlockPos pos) {
        if (!this.isAlive() || !shouldPowerSide(direction)) {
            return 0;
        }
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        if (!blockState.hasProperty(DiodeBlock.POWERED)) return 0;

        return blockState.getValue(DiodeBlock.POWERED) ? 15 : 0;
    }

    protected int getPower(Level world, BlockPos pos, BlockState state) {
        if (!state.hasProperty(HorizontalDirectionalBlock.FACING)) return 0;
        Direction direction = calculateBlockFacing(state.getValue(HorizontalDirectionalBlock.FACING));
        BlockPos blockPos = pos.relative(direction);
        int i = Math.max(world.getDirectSignal(blockPos, direction), world.getSignal(blockPos, direction));
        return i > 0 ? 15 : 0;
    }
}
