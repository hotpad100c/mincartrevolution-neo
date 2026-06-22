package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class DamageCausingMinecartEntity extends SingleBlockMinecartEntity {

  public float damageAmount;
  public DamageSource damageSource;
  public ResourceKey<DamageType> damageType;

  public DamageCausingMinecartEntity(
      EntityType<? extends SingleBlockMinecartEntity> entityType, Level world) {
    super(entityType, world);
    this.damageAmount = 0;
    this.damageType = DamageTypes.GENERIC;
    this.damageSource = this.damageSources().source(this.damageType);
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.CACTUS.defaultBlockState();
  }

  public DamageCausingMinecartEntity(
      EntityType<? extends SingleBlockMinecartEntity> minecart,
      Level world,
      double x,
      double y,
      double z,
      float damageAmount,
      MinecartWithBlockItem correspondingItem,
      ResourceKey<DamageType> damageType) {
    super(minecart, world, x, y, z, correspondingItem);
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.damageSource = this.damageSources().source(damageType);
  }

  @Override
  public void push(Entity entity) {
    super.push(entity);
    entity.hurt(this.damageSource, this.damageAmount);
  }

  public DamageSource getDamageSource() {
    return this.damageSource;
  }

  @Override
  public void load(@NonNull ValueInput nbt) {
    super.load(nbt);
    this.damageAmount = nbt.getFloatOr("damageAmount", 1.0f);
    try {
      String damageTypePath =
          nbt.getStringOr("damageType", DamageTypes.GENERIC.identifier().getPath());
      this.damageType = resolveDamageType(damageTypePath);
      this.damageSource = this.damageSources().source(this.damageType);
    } catch (Exception ignored) {
    }
  }

  @Override
  public void saveWithoutId(ValueOutput nbt) {
    nbt.putString("damageType", this.damageType.identifier().getPath());
    nbt.putFloat("damageAmount", this.damageAmount);
    super.saveWithoutId(nbt);
  }

  /** Maps a damage type path string back to its {@link ResourceKey}. */
  private static ResourceKey<DamageType> resolveDamageType(String path) {
    return switch (path) {
      case "cactus" -> DamageTypes.CACTUS;
      case "hot_floor" -> DamageTypes.HOT_FLOOR;
      case "campfire" -> DamageTypes.CAMPFIRE;
      default -> DamageTypes.GENERIC;
    };
  }

  @Override
  public void tick() {
    super.tick();

    if (!this.level().isClientSide() && this.damageAmount > 0.0f) {
      if (!this.getPassengers().isEmpty()) {
        for (Entity passenger : this.getPassengers()) {
          passenger.hurt(this.damageSource, this.damageAmount);
        }
      }
      if (DamageTypes.CACTUS.equals(this.damageType)) {
        AABB touchArea = this.getBoundingBox().inflate(0.05);
        List<Entity> touchingEntities = this.level().getEntities(this, touchArea);

        for (Entity entity : touchingEntities) {
          entity.hurt(this.damageSource, this.damageAmount);
        }
      }
      else if (DamageTypes.HOT_FLOOR.equals(this.damageType) || DamageTypes.CAMPFIRE.equals(this.damageType)) {
        AABB topArea = this.getBoundingBox().move(0, 0.05, 0);
        List<Entity> topEntities = this.level().getEntities(this, topArea);
        for (Entity entity : topEntities) {
          boolean isSteppingOnTop = entity.getY() >= this.getY() + 0.1;
          boolean isNotSneaking = !entity.isSteppingCarefully();
          if (isSteppingOnTop && isNotSneaking) {
            entity.hurt(this.damageSource, this.damageAmount);
          }
        }
      }
    }
  }
}
