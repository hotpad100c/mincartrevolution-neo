package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import ml.mypals.minecartrevolution.entity.minecarts.SingleBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MagnetMinecartEntity extends SingleBlockMinecartEntity {
    public MagnetMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public MagnetMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
    }

    public MagnetMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world, MinecartWithBlockItem correspondingItem) {
        super(entityType, world, correspondingItem);
    }
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.activated) {
            attractItems();
            repelOtherMagnets();
        }
    }

    private void attractItems() {
        double radius = 8.0;
        List<ItemEntity> items = this.level().getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(radius)
        );

        for (ItemEntity itemEntity : items) {
            if (isIronItem(itemEntity.getItem())) {
                net.minecraft.world.phys.Vec3 vec3 = this.position().subtract(itemEntity.position());
                double distanceSq = vec3.lengthSqr();
                if (distanceSq > 0.01) {
                    double force = 0.05;
                    itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(vec3.normalize().scale(force)));
                }
            }
        }

        List<LivingEntity> livingEntities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius)
        );

        for (LivingEntity livingEntity : livingEntities) {
            if (hasIron(livingEntity)) {
                Vec3 vec3 = this.position().subtract(livingEntity.position());
                double distanceSq = vec3.lengthSqr();
                if (distanceSq > 0.01) {
                    double force = 0.02;
                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(vec3.normalize().scale(force)));
                    livingEntity.hurtMarked = true;
                }
            }
        }

        List<AbstractMinecart> minecarts = this.level().getEntitiesOfClass(
                AbstractMinecart.class,
                this.getBoundingBox().inflate(radius)
        );

        for (AbstractMinecart minecart : minecarts) {
            if (isIronItem(minecart.getDisplayBlockState().getBlock().asItem().getDefaultInstance())) {
                Vec3 vec3 = this.position().subtract(minecart.position());
                double distanceSq = vec3.lengthSqr();
                if (distanceSq > 0.01) {
                    double force = 0.02;
                    minecart.setDeltaMovement(minecart.getDeltaMovement().add(vec3.normalize().scale(force)));
                    minecart.hurtMarked = true;
                }
            }
        }
    }

    private boolean hasIron(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (isIronItem(entity.getItemBySlot(slot))) {
                return true;
            }
        }
        return false;
    }

    private void repelOtherMagnets() {
        double radius = 4.0;
        java.util.List<MagnetMinecartEntity> others = this.level().getEntitiesOfClass(
                MagnetMinecartEntity.class,
                this.getBoundingBox().inflate(radius)
        );

        for (MagnetMinecartEntity other : others) {
            if (other != this) {
                net.minecraft.world.phys.Vec3 vec3 = other.position().subtract(this.position());
                double distanceSq = vec3.lengthSqr();
                if (distanceSq > 0.01) {
                    double force = 0.02;
                    net.minecraft.world.phys.Vec3 repulsion = vec3.normalize().scale(force);
                    other.setDeltaMovement(other.getDeltaMovement().add(repulsion));
                    this.setDeltaMovement(this.getDeltaMovement().subtract(repulsion));
                }
            }
        }
    }

    private boolean isIronItem(net.minecraft.world.item.ItemStack stack) {
        net.minecraft.world.item.Item item = stack.getItem();
        return item == net.minecraft.world.item.Items.IRON_INGOT ||
               item == net.minecraft.world.item.Items.IRON_NUGGET ||
               item == net.minecraft.world.item.Items.IRON_BLOCK ||
               item == net.minecraft.world.item.Items.RAW_IRON ||
               item == net.minecraft.world.item.Items.IRON_ORE ||
               item == net.minecraft.world.item.Items.DEEPSLATE_IRON_ORE ||
               item == net.minecraft.world.item.Items.IRON_SWORD ||
               item == net.minecraft.world.item.Items.IRON_PICKAXE ||
               item == net.minecraft.world.item.Items.IRON_AXE ||
               item == net.minecraft.world.item.Items.IRON_SHOVEL ||
               item == net.minecraft.world.item.Items.IRON_HOE ||
               item == net.minecraft.world.item.Items.IRON_HELMET ||
               item == net.minecraft.world.item.Items.IRON_CHESTPLATE ||
               item == net.minecraft.world.item.Items.IRON_LEGGINGS ||
               item == net.minecraft.world.item.Items.IRON_BOOTS ||
               item == net.minecraft.world.item.Items.IRON_HORSE_ARMOR ||
               item == net.minecraft.world.item.Items.ANVIL ||
               item == net.minecraft.world.item.Items.CHIPPED_ANVIL ||
               item == net.minecraft.world.item.Items.DAMAGED_ANVIL ||
               item == net.minecraft.world.item.Items.HOPPER ||
               item == net.minecraft.world.item.Items.IRON_DOOR ||
               item == net.minecraft.world.item.Items.IRON_TRAPDOOR ||
               item == net.minecraft.world.item.Items.IRON_BARS;
    }
}
