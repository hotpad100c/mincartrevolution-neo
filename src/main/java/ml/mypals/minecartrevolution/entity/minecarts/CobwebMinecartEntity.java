package ml.mypals.minecartrevolution.entity.minecarts;

import java.util.List;
import java.util.Optional;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class CobwebMinecartEntity extends SingleBlockMinecartEntity {
  private static int MAX_PASSENGERS_COUNT = 25;
  public static final TagKey<EntityType<?>> CATCHABLE_ENTITIES =
      TagKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath("minecartrevolution", "cobweb_minecart_catchable"));
  public static final TagKey<EntityType<?>> UNCATCHABLE_ENTITIES =
      TagKey.create(
          Registries.ENTITY_TYPE,
          Identifier.fromNamespaceAndPath("minecartrevolution", "cobweb_minecart_cant_catch"));

  public CobwebMinecartEntity(EntityType<CobwebMinecartEntity> entityType, Level world) {
    super(entityType, world);
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.COBWEB.defaultBlockState();
  }

  public CobwebMinecartEntity(
      EntityType<CobwebMinecartEntity> minecart,
      Level world,
      double x,
      double y,
      double z,
      MinecartWithBlockItem correspondingItem) {
    super(minecart, world, x, y, z, correspondingItem);
    setCustomDisplayBlockState(Optional.of(Blocks.COBWEB.defaultBlockState()));
  }

  public CobwebMinecartEntity(
      EntityType<CobwebMinecartEntity> entityType,
      Level world,
      MinecartWithBlockItem correspondingItem) {
    super(entityType, world, correspondingItem);
  }

  @Override
  public void activateMinecart(@NonNull ServerLevel level, int xt, int yt, int zt, boolean state) {
    if (state) {
      if (this.isVehicle()) {
        if (random.nextFloat() < 0.2) {
          this.getPassengers().getFirst().stopRiding();
        }
        if (this.getHurtTime() == 0) {
          this.setHurtDir(-this.getHurtDir());
          this.setHurtTime(10);
          this.setDamage(50.0F);
          this.markHurt();
        }
      }
    }
  }

  @Override
  public boolean isRideable() {
    return true;
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide()) return;
    AABB hitbox = this.getBoundingBox().inflate(0.3F, 0.3F, 0.3F);
    List<Entity> entities = this.level().getEntities(this, hitbox);
    if (!entities.isEmpty()) {
      for (Entity entity : entities) {
        if ((!(entity instanceof Player)
                || ((entity instanceof Player)
                    && this.getDeltaMovement().horizontalDistanceSqr() >= 0.01))
            && !(entity instanceof CobwebMinecartEntity)
            && this.getPassengers().size() < MAX_PASSENGERS_COUNT
            && !entity.is(UNCATCHABLE_ENTITIES)
            && (entity instanceof LivingEntity
                || entity instanceof ItemEntity
                || entity instanceof VehicleEntity
                || entity.is(CATCHABLE_ENTITIES))) {
          entity.startRiding(this, true, true);
        }
      }
    }
    if (this.getPassengers().size() >= 24) {
      for (Entity passenger : this.getPassengers()) {
        passenger.hurtServer((ServerLevel) this.level(), this.damageSources().cramming(), 1);
      }
    }
  }

  @Override
  protected void positionRider(@NonNull Entity passenger, Entity.MoveFunction moveFunction) {
    Vec3 position = this.getPassengerRidingPosition(passenger);
    Vec3 offset = passenger.getVehicleAttachmentPoint(this);
    int hash = passenger.getUUID().hashCode();

    double dx = (((hash >> 16) & 0xFF) / 255.0 - 0.5) * 0.7;
    double dy = (((hash >> 8) & 0xFF) / 255.0 - 0.5) * 0.4;
    double dz = (((hash >> 4) & 0xFF) / 255.0 - 0.5) * 0.7;

    moveFunction.accept(
        passenger,
        position.x - offset.x + dx,
        position.y - offset.y + dy,
        position.z - offset.z + dz);
  }

  @Override
  public @NonNull ItemStack getPickResult() {
    return MRMinecarts.COBWEB_MINECART.item().get().getDefaultInstance();
  }

  @Override
  public @NonNull Item getDropItem() {
    return MRMinecarts.COBWEB_MINECART.item().get();
  }
}
