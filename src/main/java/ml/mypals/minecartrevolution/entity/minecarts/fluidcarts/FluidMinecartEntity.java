package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class FluidMinecartEntity extends VariantBlockMinecartEntity {

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.WATER.defaultBlockState();
    }

    public static BlockState getFluidBlockStateFromBucket(Item item) {
        if (item instanceof BucketItem bucketItem) {
            Fluid fluid = bucketItem.getContent();
            if (fluid != Fluids.EMPTY) {
                return fluid.defaultFluidState().createLegacyBlock();
            }
        }
        Block block = Block.byItem(item);
        if (block != Blocks.AIR) return block.defaultBlockState();
        return Blocks.AIR.defaultBlockState();
    }

    private static Fluid getFluidFromDisplayBlock(BlockState blockState) {
        return blockState.getFluidState().getType();
    }

    public FluidMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world, Item item) {
        super(entityType, world);
        this.setCustomDisplayBlockState(Optional.of(getFluidBlockStateFromBucket(item)));
    }

    public FluidMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z);
        this.setCustomDisplayBlockState(Optional.of(getFluidBlockStateFromBucket(item)));
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        ItemStack stack = player.getItemInHand(hand);
        BlockState currentBlock = getDisplayBlockState();

        if (player.isSecondaryUseActive()) {
            if (stack.is(Items.BUCKET)) {
                Fluid containedFluid = getFluidFromDisplayBlock(currentBlock);
                if (containedFluid != Fluids.EMPTY) {
                    if (!level().isClientSide()) {
                        stack.split(1);
                        Item bucketItem = containedFluid.getFluidType().
                                getBucket(new FluidStack(containedFluid, containedFluid.getAmount(containedFluid.defaultFluidState()))).getItem();
                        player.getInventory().add(new ItemStack(bucketItem));
                        this.setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
                        transformTo(Blocks.AIR);
                    }
                    playBucketSound(currentBlock.getBlock());
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.interact(player, hand, pos);
    }

    @Override
    public void tick() {
        super.tick();

        BlockState blockState = getDisplayBlockState();
        if (!blockState.getFluidState().isEmpty()) {
            applyFluidEffects();
        }
    }

    private void applyFluidEffects() {
        BlockState blockState = getDisplayBlockState();
        AABB aabb = getBoundingBox().inflate(0.2);
        for (Entity entity : level().getEntities(this, aabb)) {
            if (blockState.getFluidState().is(FluidTags.LAVA)) {
                entity.igniteForSeconds(5);
            } else if (blockState.getFluidState().is(FluidTags.WATER)) {
                entity.clearFire();
            }
        }

        for (Entity passenger : getPassengers()) {
            if (blockState.getFluidState().is(FluidTags.LAVA)) {
                passenger.igniteForSeconds(5);
            } else if (blockState.getFluidState().is(FluidTags.WATER)) {
                passenger.clearFire();
            }
        }
    }

    private void applyAOEFluidEffects() {
        BlockState blockState = getDisplayBlockState();
        AABB aabb = getBoundingBox().inflate(1.5);
        for (Entity entity : level().getEntitiesOfClass(Entity.class, aabb)) {
            if (entity instanceof LivingEntity living) {
                if (blockState.getFluidState().is(FluidTags.LAVA)) {
                    living.igniteForSeconds(3);
                } else if (blockState.getFluidState().is(FluidTags.WATER)) {
                    living.clearFire();
                }
            }
        }
        for (BlockPos blockPos : BlockPos.betweenClosed(aabb.inflate(0.5, 0, 0.5))) {
            if (blockState.getFluidState().is(FluidTags.LAVA)) {
                if (level().isEmptyBlock(blockPos) && !level().isEmptyBlock(blockPos.below()) && this.getRandom().nextInt(10) == 0) {
                    level().setBlockAndUpdate(blockPos, BaseFireBlock.getState(level(), blockPos));
                }
            } else if (blockState.getFluidState().is(FluidTags.WATER)) {
                BlockState targetBlock = level().getBlockState(blockPos);
                if (targetBlock.is(Blocks.FARMLAND)) {
                    level().setBlockAndUpdate(blockPos, targetBlock.setValue(FarmlandBlock.MOISTURE, 7));
                }
            }
        }
    }

    @Override
    public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {
        if (powered) {
            this.ejectPassengers();
            if (this.getHurtTime() == 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.setDamage(50.0F);
                this.markHurt();
                BlockState blockState = getDisplayBlockState();
                Vec3 dir = new Vec3(random.nextFloat(), random.nextFloat(), random.nextFloat());
                if (blockState.getFluidState().is(FluidTags.LAVA)) {
                    level.sendParticles(ParticleTypes.LAVA, position().x(), position().y() + 0.5, position().z(), 1, dir.x(), dir.y(), dir.z(), 0.5);
                    level.sendParticles(ParticleTypes.FALLING_LAVA, position().x(), position().y() + 0.5, position().z(), 2, dir.x(), dir.y(), dir.z(), 0.5);
                } else if (blockState.getFluidState().is(FluidTags.WATER)) {
                    level.sendParticles(ParticleTypes.SPLASH, position().x(), position().y() + 0.5, position().z(), 10, dir.x(), dir.y(), dir.z(), 0.5);
                }
                applyAOEFluidEffects();
            }
        }
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        BlockState blockState = getDisplayBlockState();
        if (blockState.getFluidState().is(FluidTags.LAVA)) {
            return MRMinecarts.LAVA_MINECART.item().asItem().getDefaultInstance();
        } else if (blockState.getFluidState().is(FluidTags.WATER)) {
            return MRMinecarts.WATER_MINECART.item().asItem().getDefaultInstance();
        } else if (!blockState.getFluidState().isEmpty()) {
            return MRMinecarts.WATER_MINECART.item().asItem().getDefaultInstance();
        }
        return Items.MINECART.getDefaultInstance();
    }
}
