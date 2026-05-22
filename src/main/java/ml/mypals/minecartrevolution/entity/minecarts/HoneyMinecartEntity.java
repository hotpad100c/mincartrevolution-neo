package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class HoneyMinecartEntity extends SingleBlockMinecartEntity {
    @Nullable
    private BlockPos stuckBlock = null;

    public HoneyMinecartEntity(EntityType<HoneyMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    public HoneyMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        setCustomDisplayBlockState(Optional.of(Blocks.HONEY_BLOCK.defaultBlockState()));
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.stuckBlock != null) {
                if (this.level().getBlockState(this.stuckBlock).isAir() || !this.level().getBlockState(this.stuckBlock).isSolid()) {
                    this.stuckBlock = null;
                    this.setNoGravity(false);
                } else {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.setNoGravity(true);
                }
            }
        }
    }

    @Override
    public void move(@NonNull MoverType moverType, @NonNull Vec3 delta) {
        Vec3 toPosition = this.position().add(delta);
        super.move(moverType, delta);
        Vec3 posNow = this.position();

        if (!this.level().isClientSide() && this.stuckBlock == null) {

        }
    }
    @Override
    public boolean onCollision(Vec3 delta, Vec3 target, Vec3 actual){

        AABB box = this.getBoundingBox().expandTowards(delta.normalize().scale(0.5));
        BlockPos.betweenClosedStream(box).filter(pos -> !this.level().getBlockState(pos).isAir() && this.level().getBlockState(pos).isSolid()).findFirst().ifPresent(pos -> {
            this.stuckBlock = pos.immutable();
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()),
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        20,
                        0.3, 0.3, 0.3,
                        0.05
                );
            }
        });
        return super.onCollision(delta, target, actual);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        if (this.stuckBlock != null) {
            compound.putLong("StuckBlock", this.stuckBlock.asLong());
        }
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput compound) {
        super.readAdditionalSaveData(compound);
        this.stuckBlock = BlockPos.of(compound.getLongOr("StuckBlock", (Long)null));
    }

    @Override
    protected @NonNull Vec3 applyNaturalSlowdown(@NonNull Vec3 movement) {
        if(!this.isInLava()) return super.applyNaturalSlowdown(movement);

        return movement;
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.HONEY_MINECART.item().get().getDefaultInstance();
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.HONEY_MINECART.item().get();
    }
}