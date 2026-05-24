package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class AntMinecartEntity extends SingleBlockMinecartEntity {

    private Direction facing = Direction.NORTH;
    private int restTicks = 0;
    private static final int REST_DURATION = 2;
    private static final double MOVE_SPEED = 0.5;

    @Nullable
    private DyeColor targetDyeColor = null;
    public AntMinecartEntity(EntityType<AntMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.OBSERVER.defaultBlockState();
    }

    public AntMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world,
                             double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        setCustomDisplayBlockState(Optional.of(Blocks.OBSERVER.defaultBlockState().setValue(ObserverBlock.FACING, Direction.NORTH)));
    }
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        if (targetDyeColor != null) {
            tickColorFollow();
        } else {
            tickLangtonAnt();
        }
    }

    private void tickLangtonAnt() {
        if (restTicks > 0) {
            restTicks--;
            return;
        }

        BlockPos belowPos = this.blockPosition().below();
        BlockState belowState = level().getBlockState(belowPos);

        boolean isWhite = belowState.is(Blocks.WHITE_CONCRETE);
        boolean isBlack = belowState.is(Blocks.BLACK_CONCRETE);

        if (!isWhite && !isBlack) {
            return;
        }
        Direction newFacing;
        BlockState newBelowBlock;
        if (isWhite) {
            newFacing = facing.getClockWise();
            newBelowBlock = Blocks.BLACK_CONCRETE.defaultBlockState();
        } else {
            newFacing = facing.getCounterClockWise();
            newBelowBlock = Blocks.WHITE_CONCRETE.defaultBlockState();
        }
        BlockPos targetPos = this.blockPosition().relative(newFacing);
        BlockState targetState = level().getBlockState(targetPos);
        if (Block.isShapeFullBlock(targetState.getCollisionShape(level(), targetPos))) {
            return;
        }
        facing = newFacing;
        level().setBlock(belowPos, newBelowBlock, 3);
        Vec3 targetCenter = targetPos.getBottomCenter();
        teleportTo(targetCenter.x(), getY(), targetCenter.z());
        restTicks = REST_DURATION;
    }

    private void tickColorFollow() {
        if (restTicks > 0) {
            restTicks--;
            return;
        }

        BlockPos belowPos = this.blockPosition().below();
        if (!isTargetColorBlock(level().getBlockState(belowPos))) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        if (tryMoveInDirection(facing)) return;
        if (tryMoveInDirection(facing.getClockWise())) return;
        if (tryMoveInDirection(facing.getCounterClockWise())) return;

        this.setDeltaMovement(Vec3.ZERO);
    }

    private boolean tryMoveInDirection(Direction dir) {
        BlockPos targetPos = this.blockPosition().relative(dir);
        BlockState targetState = level().getBlockState(targetPos);
        if (Block.isShapeFullBlock(targetState.getCollisionShape(level(), targetPos))) {
            return false;
        }
        if (!isTargetColorBlock(level().getBlockState(targetPos.below()))) {
            return false;
        }

        facing = dir;
        this.setDeltaMovement(new Vec3(dir.getStepX() * MOVE_SPEED, 0, dir.getStepZ() * MOVE_SPEED));
        restTicks = REST_DURATION;
        return true;
    }

    private boolean isTargetColorBlock(BlockState state) {
        if (targetDyeColor == null) return false;
        String colorName = targetDyeColor.getName();
        Block block = state.getBlock();
        Block wool     = BuiltInRegistries.BLOCK.getValue(
                Identifier.fromNamespaceAndPath("minecraft", colorName + "_wool"));
        Block concrete = BuiltInRegistries.BLOCK.getValue(
                Identifier.fromNamespaceAndPath("minecraft", colorName + "_concrete"));
        return block == wool || block == concrete;
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player,
                                               @NonNull InteractionHand hand,
                                               @NonNull Vec3 pos) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof DyeItem dyeItem) {
            if (!level().isClientSide()) {
                targetDyeColor = stack.get(DataComponents.DYE);
                stack.consume(1, player);
                level().playSound(null, blockPosition(),
                        SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }

        return super.interact(player, hand, pos);
    }
    @Override
    public void saveWithoutId(@NonNull ValueOutput nbt) {
        super.saveWithoutId(nbt);
        nbt.putString("AntFacing", facing.getName());
        nbt.putInt("AntRestTicks", restTicks);
        nbt.putInt("TargetDyeColor", targetDyeColor != null ? targetDyeColor.getId() : -1);
    }

    @Override
    public void load(@NonNull ValueInput nbt) {
        super.load(nbt);
        String facingName = nbt.getStringOr("AntFacing", "north");
        Direction loaded = Direction.byName(facingName);
        facing = (loaded != null && loaded.getAxis() != Direction.Axis.Y) ? loaded : Direction.NORTH;
        restTicks = nbt.getIntOr("AntRestTicks", 0);
        int colorId = nbt.getIntOr("TargetDyeColor", -1);
        targetDyeColor = (colorId >= 0 && colorId < DyeColor.values().length)
                ? DyeColor.byId(colorId) : null;
    }
    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.OBSERVER_MINECART.item().get().getDefaultInstance();
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.OBSERVER_MINECART.item().get();
    }
}