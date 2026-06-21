package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import java.util.List;
import ml.mypals.minecartrevolution.entity.minecarts.SingleBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class MagnetMinecartEntity extends SingleBlockMinecartEntity {
  public MagnetMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
    super(entityType, world);
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.LODESTONE.defaultBlockState();
  }

  public MagnetMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      MinecartWithBlockItem correspondingItem) {
    super(minecart, world, x, y, z, correspondingItem);
  }

  public MagnetMinecartEntity(
      EntityType<? extends AbstractMinecart> entityType,
      Level world,
      MinecartWithBlockItem correspondingItem) {
    super(entityType, world, correspondingItem);
  }

  @Override
  public void tick() {
    super.tick();
    BlockState block = level().getBlockState(blockPosition());
    if (!block.is(Blocks.ACTIVATOR_RAIL)
        || (block.is(Blocks.ACTIVATOR_RAIL) && !block.getValue(PoweredRailBlock.POWERED))) {
      attractItems();
      repelOtherMagnets();
    }
  }

  @Override
  public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {

    handleActive(level, x, y, z, powered);
  }

  private void attractItems() {
    double radius = 8.0;
    List<ItemEntity> items =
        this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(radius));

    for (ItemEntity itemEntity : items) {
      if (isIronItem(itemEntity.getItem())) {
        Vec3 vec3 = this.position().subtract(itemEntity.position());
        double distanceSq = vec3.lengthSqr();
        if (distanceSq > 0.01) {
          double distance = Math.sqrt(distanceSq);
          double force = 0.1 * (1.0 - distance / radius);
          itemEntity.setDeltaMovement(
              itemEntity.getDeltaMovement().add(vec3.normalize().scale(force)));
        }
      }
    }

    List<LivingEntity> livingEntities =
        this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius));

    for (LivingEntity livingEntity : livingEntities) {
      if (hasIron(livingEntity)) {
        Vec3 vec3 = this.position().subtract(livingEntity.position());
        double distanceSq = vec3.lengthSqr();
        if (distanceSq > 0.01) {
          double distance = Math.sqrt(distanceSq);
          double force = 0.1 * (1.0 - distance / radius);
          livingEntity.setDeltaMovement(
              livingEntity.getDeltaMovement().add(vec3.normalize().scale(force)));
          livingEntity.hurtMarked = true;
        }
      }
    }

    List<AbstractMinecart> minecarts =
        this.level()
            .getEntitiesOfClass(AbstractMinecart.class, this.getBoundingBox().inflate(radius));

    for (AbstractMinecart minecart : minecarts) {
      if (isIronItem(minecart.getDisplayBlockState().getBlock().asItem().getDefaultInstance())) {
        Vec3 vec3 = this.position().subtract(minecart.position());
        double distanceSq = vec3.lengthSqr();
        if (distanceSq > 0.01) {
          double distance = Math.sqrt(distanceSq);
          double force = 0.05 * (1.0 - distance / radius);
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
    List<MagnetMinecartEntity> others =
        this.level()
            .getEntitiesOfClass(MagnetMinecartEntity.class, this.getBoundingBox().inflate(radius));

    for (MagnetMinecartEntity other : others) {
      if (other != this) {
        Vec3 vec3 = other.position().subtract(this.position());
        double distanceSq = vec3.lengthSqr();
        if (distanceSq > 0.01) {
          double distance = Math.sqrt(distanceSq);
          double force = Math.max(0, 0.6 * (1.0 - distance / radius));
          Vec3 repulsion = vec3.normalize().scale(force);
          Vec3 currentVec = new Vec3(repulsion.x(), 0, repulsion.z());
          other.setDeltaMovement(other.getDeltaMovement().add(currentVec));
          if (!isBlockedBehind()) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(currentVec));
          } else {
            this.setDeltaMovement(Vec3.ZERO);
          }
        }
      }
    }
  }

  private boolean isBlockedBehind() {
    AABB predictedBox = this.getBoundingBox().inflate(0.1);
    return this.level().getBlockCollisions(this, predictedBox).iterator().hasNext();
  }

  private boolean isIronItem(ItemStack stack) {
    Item item = stack.getItem();
    return item.getDescriptionId().contains("iron")
        || stack.is(ItemTags.ANVIL)
        || item == Items.HOPPER;
  }
}
