package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;

public class MobHeadMinecartEntity extends VariantBlockMinecartEntity {
    public MobHeadMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public MobHeadMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.activated) {
            handleMobInteractions();
        }
    }

    private void handleMobInteractions() {
        net.minecraft.world.level.block.state.BlockState blockState = this.getDisplayBlockState();
        net.minecraft.world.level.block.Block block = blockState.getBlock();

        if (block == net.minecraft.world.level.block.Blocks.DRAGON_HEAD || block == net.minecraft.world.level.block.Blocks.DRAGON_WALL_HEAD) {
            scareMobs(net.minecraft.world.entity.monster.Monster.class, 12.0, 0.1);
        } else if (isMonsterHead(block)) {
            scareMobs(Villager.class, 10.0, 0.1);
            attractMobs(IronGolem.class, 16.0, 0.05);
        }
    }

    private boolean isMonsterHead(net.minecraft.world.level.block.Block block) {
        return block == net.minecraft.world.level.block.Blocks.ZOMBIE_HEAD || block == net.minecraft.world.level.block.Blocks.ZOMBIE_WALL_HEAD ||
               block == net.minecraft.world.level.block.Blocks.SKELETON_SKULL || block == net.minecraft.world.level.block.Blocks.SKELETON_WALL_SKULL ||
               block == net.minecraft.world.level.block.Blocks.WITHER_SKELETON_SKULL || block == net.minecraft.world.level.block.Blocks.WITHER_SKELETON_WALL_SKULL ||
               block == net.minecraft.world.level.block.Blocks.CREEPER_HEAD || block == net.minecraft.world.level.block.Blocks.CREEPER_WALL_HEAD ||
               block == net.minecraft.world.level.block.Blocks.PIGLIN_HEAD || block == net.minecraft.world.level.block.Blocks.PIGLIN_WALL_HEAD;
    }

    private <T extends net.minecraft.world.entity.LivingEntity> void scareMobs(Class<T> entityClass, double radius, double force) {
        java.util.List<T> entities = this.level().getEntitiesOfClass(entityClass, this.getBoundingBox().inflate(radius));
        for (T entity : entities) {
            net.minecraft.world.phys.Vec3 vec3 = entity.position().subtract(this.position());
            if (vec3.lengthSqr() > 0.01) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(vec3.normalize().scale(force)));
            }
        }
    }

    private <T extends net.minecraft.world.entity.LivingEntity> void attractMobs(Class<T> entityClass, double radius, double force) {
        java.util.List<T> entities = this.level().getEntitiesOfClass(entityClass, this.getBoundingBox().inflate(radius));
        for (T entity : entities) {
            net.minecraft.world.phys.Vec3 vec3 = this.position().subtract(entity.position());
            if (vec3.lengthSqr() > 0.01) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(vec3.normalize().scale(force)));
            }
        }
    }
}
