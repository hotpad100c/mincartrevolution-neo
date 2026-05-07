package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.entity.minecarts.HasVariantRegularBlockMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class FluidMinecartEntity extends HasVariantRegularBlockMinecartEntity {
    private static final Logger log = LoggerFactory.getLogger(FluidMinecartEntity.class);

    public FluidMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public FluidMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Block blockInside) {
        super(minecart, world, x, y, z, blockInside);
    }

    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        ItemStack stack = player.getItemInHand(hand);
        BlockState currentBlock = getDisplayBlockState();
        
        if (stack.is(Items.BUCKET)) {
            if (currentBlock.is(Blocks.WATER)) {
                if (!level().isClientSide()) {
                    stack.consume(1, player);
                    player.getInventory().add(new ItemStack(Items.WATER_BUCKET));
                    transformTo(Blocks.AIR);
                }
                playBucketSound(Blocks.WATER);
                return InteractionResult.SUCCESS;
            } else if (currentBlock.is(Blocks.LAVA)) {
                if (!level().isClientSide()) {
                    stack.consume(1, player);
                    player.getInventory().add(new ItemStack(Items.LAVA_BUCKET));
                    transformTo(Blocks.AIR);
                }
                playBucketSound(Blocks.LAVA);
                return InteractionResult.SUCCESS;
            }
        } else if (stack.is(Items.WATER_BUCKET)) {
            if (!currentBlock.is(Blocks.WATER)) {
                if (!level().isClientSide()) {
                    stack.consume(1, player);
                    player.getInventory().add(new ItemStack(Items.BUCKET));
                    transformTo(Blocks.WATER);
                }
                playBucketSound(Blocks.WATER);
                return InteractionResult.SUCCESS;
            }
        } else if (stack.is(Items.LAVA_BUCKET)) {
            if (!currentBlock.is(Blocks.LAVA)) {
                if (!level().isClientSide()) {
                    stack.consume(1, player);
                    player.getInventory().add(new ItemStack(Items.BUCKET));
                    transformTo(Blocks.LAVA);
                }
                playBucketSound(Blocks.LAVA);
                return InteractionResult.SUCCESS;
            }
        }

        return super.interact(player, hand, pos);
    }
    

    @Override
    public void tick() {
        super.tick();
        
        BlockState blockState = getDisplayBlockState();
        if (blockState.is(Blocks.LAVA) || blockState.is(Blocks.WATER)) {
            applyFluidEffects();
        }

    }

    private void applyFluidEffects() {
        BlockState blockState = getDisplayBlockState();
        AABB aabb = getBoundingBox().inflate(0.2);
        for (Entity entity : level().getEntities(this, aabb)) {
            if (entity instanceof LivingEntity living) {
                if (blockState.is(Blocks.LAVA)) {
                    living.igniteForSeconds(5);
                } else if (blockState.is(Blocks.WATER)) {
                    living.clearFire();
                }
            }
        }

        for (Entity passenger : getPassengers()) {
            if (passenger instanceof LivingEntity living) {
                if (blockState.is(Blocks.LAVA)) {
                    living.igniteForSeconds(5);
                } else if (blockState.is(Blocks.WATER)) {
                    living.clearFire();
                }
            }
        }
    }

    private void applyAOEFluidEffects() {
        BlockState blockState = getDisplayBlockState();
        AABB aabb = getBoundingBox().inflate(1.5);
        for (Entity entity : level().getEntitiesOfClass(Entity.class, aabb)) {
            if (entity instanceof LivingEntity living) {
                if (blockState.is(Blocks.LAVA)) {
                    living.igniteForSeconds(3);
                } else if (blockState.is(Blocks.WATER)) {
                    living.clearFire();
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
                Vec3 dir = new Vec3(random.nextFloat(),random.nextFloat(),random.nextFloat());
                if (blockState.is(Blocks.LAVA)) {
                    level.sendParticles(ParticleTypes.LAVA,position().x(),position().y()+0.5,position().z(),1,dir.x(),dir.y(),dir.z(),0.5);
                    level.sendParticles(ParticleTypes.FALLING_LAVA,position().x(),position().y()+0.5,position().z(),2,dir.x(),dir.y(),dir.z(),0.5);
                } else if (blockState.is(Blocks.WATER)) {
                    level.sendParticles(ParticleTypes.SPLASH,position().x(),position().y()+0.5,position().z(),10,dir.x(),dir.y(),dir.z(),0.5);
                }
                applyAOEFluidEffects();
            }
        }
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        BlockState blockState = getDisplayBlockState();
        if (blockState.is(Blocks.LAVA)) {
            return MRMinecarts.LAVA_MINECART.item().asItem().getDefaultInstance();
        }else if(blockState.is(Blocks.WATER)){
            return MRMinecarts.WATER_MINECART.item().asItem().getDefaultInstance();
        }
        return Items.MINECART.getDefaultInstance();
    }
}
