package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MRModItems;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.advancements.criterion.FilledBucketTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static net.minecraft.world.level.block.Block.dropResources;

public class SpongeMinecartEntity extends ItemBoundBlockMinecartEntity {
    public static final int ABSORB_RADIUS = 6;
    public static final int ABSORB_LIMIT = 64;
    private static final Direction[] DIRECTIONS = Direction.values();

    private int absorbRadius = ABSORB_RADIUS;
    private int absorbLimit = ABSORB_RADIUS;

    protected SpongeMinecartEntity(EntityType<? extends ItemBoundBlockMinecartEntity> entityType, Level world, int absorbRadius, int absorbLimit) {
        super(entityType, world);
        this.absorbRadius = absorbRadius;
        this.absorbLimit = absorbLimit;
    }

    public SpongeMinecartEntity(EntityType<? extends ItemBoundBlockMinecartEntity> minecart, Level world, double x, double y, double z, int absorbRadius, int absorbLimit, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        this.absorbRadius = absorbRadius;
        this.absorbLimit = absorbLimit;
    }

    public SpongeMinecartEntity(EntityType<SpongeMinecartEntity> spongeMinecartEntityEntityType, Level world) {
        super(spongeMinecartEntityEntityType, world);
        this.absorbRadius = ABSORB_RADIUS;
        this.absorbLimit = ABSORB_LIMIT;
    }

    @Override
    public void tick() {
        super.tick();
        this.update(level(),blockPosition());
    }
    protected void update(Level world, BlockPos pos) {
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        Block block = blockState.getBlock();
        if(block instanceof SpongeBlock spongeBlock) {
            if (this.absorbWater(world, pos)) {
                this.setCustomDisplayBlockState(Optional.of(Blocks.WET_SPONGE.defaultBlockState()));
                world.playSound(this, pos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.setCorrespondingItem(MRModItems.WET_SPONGE_MINECART);
            }
        } else if (block instanceof WetSpongeBlock) {

            if (level().dimensionType().attributes().contains(EnvironmentAttributes.WATER_EVAPORATES)) {
                this.setCustomDisplayBlockState(Optional.of(Blocks.SPONGE.defaultBlockState()));
                world.playSound(this, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.setCorrespondingItem(MRModItems.SPONGE_MINECART);

            }else if(level() instanceof ServerLevel serverLevel &&
                    serverLevel.getRandom().nextInt(4096)
                    < serverLevel.getGameRules().get(GameRules.RANDOM_TICK_SPEED)) {
                this.setCustomDisplayBlockState(Optional.of(Blocks.SPONGE.defaultBlockState()));
                world.playSound(this, pos, SoundEvents.WET_SPONGE_DRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {

        }
    }
    @Override
    protected @NonNull Vec3 applyNaturalSlowdown(@NonNull Vec3 movement) {
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        if(blockState.getBlock() instanceof WetSpongeBlock)
            movement = this.getDeltaMovement().multiply(0.5, 0.0, 0.5);
        return super.applyNaturalSlowdown(movement);
    }
    @Override
    public void load(ValueInput nbt) {
        super.load(nbt);
        this.absorbRadius = nbt.getInt("absort_radius").orElse(6);
        this.absorbRadius = nbt.getInt("absort_limit").orElse(64);
    }
    @Override
    public void saveWithoutId(ValueOutput nbt) {
        nbt.putInt("absort_radius", this.absorbRadius);
        nbt.putInt("absort_limit", this.absorbLimit);
        super.saveWithoutId(nbt);
    }
    private boolean absorbWater(Level level, BlockPos startPos) {
        BlockState spongeState = level.getBlockState(startPos);
        return BlockPos.breadthFirstTraversal(startPos, 6, 65, (pos, consumer) -> {
            for (Direction direction : DIRECTIONS) {
                consumer.accept(pos.relative(direction));
            }
        }, pos -> {
            if (pos.equals(startPos)) {
                return BlockPos.TraversalNodeStatus.ACCEPT;
            } else {
                BlockState state = level.getBlockState(pos);
                FluidState fluidState = level.getFluidState(pos);
                if (!spongeState.canBeHydrated(level, startPos, fluidState, pos)) {
                    return BlockPos.TraversalNodeStatus.SKIP;
                } else if (state.getBlock() instanceof BucketPickup bucketPickup && !bucketPickup.pickupBlock(null, level, pos, state).isEmpty()) {
                    return BlockPos.TraversalNodeStatus.ACCEPT;
                } else {
                    if (state.getBlock() instanceof LiquidBlock) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        if (!state.is(Blocks.KELP) && !state.is(Blocks.KELP_PLANT) && !state.is(Blocks.SEAGRASS) && !state.is(Blocks.TALL_SEAGRASS)) {
                            return BlockPos.TraversalNodeStatus.SKIP;
                        }

                        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                        dropResources(state, level, pos, blockEntity);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }

                    return BlockPos.TraversalNodeStatus.ACCEPT;
                }
            }
        }) > 1;
    }
}
