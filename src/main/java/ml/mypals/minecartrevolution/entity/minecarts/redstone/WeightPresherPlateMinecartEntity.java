package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import ml.mypals.minecartrevolution.entity.minecarts.SingleBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class WeightPresherPlateMinecartEntity extends SingleBlockMinecartEntity implements PowerEmitterMinecartEntity {
    private static final int MAX_BOUNDING_BOX = 64; // Ghast

    public WeightPresherPlateMinecartEntity(EntityType<? extends WeightPresherPlateMinecartEntity> entityType, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(entityType, world, x, y, z, correspondingItem);
    }

    public WeightPresherPlateMinecartEntity(EntityType<WeightPresherPlateMinecartEntity> weightPresherPlateMinecartEntityEntityType, Level world) {
        super(weightPresherPlateMinecartEntityEntityType, world);
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        super.interact(player, hand, pos);
        if (this.isVehicle()) {
            return InteractionResult.PASS;
        } else if (!this.level().isClientSide()) {
            BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
            updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
            updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public int getPowerStrength(Direction direction, BlockPos pos) {
        if (!this.isAlive()) {
            return 0;
        }
        double maxLength = 0;
        Entity firstPassenger = this.getFirstPassenger();
        if (firstPassenger != null) {
            double size = this.getFirstPassenger().getDimensions(firstPassenger.getPose()).height() *
                    this.getFirstPassenger().getDimensions(firstPassenger.getPose()).width() *
                    this.getFirstPassenger().getDimensions(firstPassenger.getPose()).width() * 10;
            size = Math.min(size, MAX_BOUNDING_BOX);
            maxLength = (size * 15) / MAX_BOUNDING_BOX;
        }
        return (int) Math.max(this.isVehicle() ? 1 : 0, maxLength);
    }
}
