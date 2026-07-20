package ml.mypals.minecartrevolution.entity.minecarts;

import java.util.List;
import java.util.function.Consumer;

import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class RailMinecartEntity extends VariantBlockMinecartEntity {

  public RailMinecartEntity(EntityType<RailMinecartEntity> entityType, Level world) {
    super(entityType, world);
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.RAIL.defaultBlockState();
  }
  @Override
  public void moveEntitiesAbove(Consumer<Entity> consumer) {
  }
  public RailMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      Item content) {
    super(minecart, world, x, y, z, content);
  }

  protected static final EntityDataAccessor<Integer> STACK_COOLDOWN =
      SynchedEntityData.defineId(RailMinecartEntity.class, EntityDataSerializers.INT);

  @Override
  protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
    super.defineSynchedData(builder);
    builder.define(STACK_COOLDOWN, 0);
  }

  public int getStackCooldown() {
    return this.getEntityData().get(STACK_COOLDOWN);
  }

  public void setStackCooldown(int cooldown) {
    this.getEntityData().set(STACK_COOLDOWN, cooldown);
  }

  @Override
  public void tick() {
    super.tick();

    if(level().isClientSide()) return;
    if (this.getStackCooldown() > 0) {
      this.setStackCooldown(this.getStackCooldown() - 1);
    }

    if (this.getPassengers().isEmpty() && this.getStackCooldown() == 0) {
      AABB hitbox = this.getBoundingBox().inflate(0.3F, 0.1F, 0.3F);
      List<AbstractMinecart> minecarts =
          this.level()
              .getEntitiesOfClass(
                  AbstractMinecart.class,
                  hitbox,
                  entity -> !(entity == this) && entity.getVehicle() == null);
      if (!minecarts.isEmpty()) {
        AbstractMinecart first = minecarts.getFirst();
        if (!(first instanceof RailMinecartEntity railCart && railCart.getStackCooldown() > 0)) {
          first.startRiding(this, true, true);
        }
      }
    }

    if (this.getVehicle() == null && !this.level().isClientSide()) {
      int layers = 1;
      Entity current = this;
      while (!current.getPassengers().isEmpty()) {
        current = current.getPassengers().getFirst();
        layers++;
      }

      if (layers > 10 && level().getGameTime() % 100 == 0) {
        AABB bounds = this.getBoundingBox().inflate(20);
        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, bounds);
        for (ServerPlayer player : players) {
          MRModCriteria.IS_THAT_BABEL_TOWER.get().trigger(player);
        }
      }

      if (layers > 24) {
        current = this;
        while (current != null) {
          if (current instanceof AbstractMinecart cart && random.nextInt(10) < 2) {
            cart.setHurtTime(random.nextInt(10));
            cart.setDamage(random.nextInt(40));
            cart.setHurtDir(-cart.getHurtDir());
            current.needsSync = true;
          }
          current = current.getPassengers().isEmpty() ? null : current.getPassengers().getFirst();
        }

        if (this.getRandom().nextInt(60) == 0) {
          Vec3 scatterDir =
              new Vec3(
                      this.getRandom().nextDouble() - 0.5, 0.5, this.getRandom().nextDouble() - 0.5)
                  .normalize();

          current = this;
          while (current != null) {
            Entity next =
                current.getPassengers().isEmpty() ? null : current.getPassengers().getFirst();
            current.stopRiding();
            current.ejectPassengers();

            current.setDeltaMovement(scatterDir.scale(0.1).add(0, 0.1, 0));
            current.hurtMarked = true;
            current.needsSync = true;

            if (current instanceof RailMinecartEntity railCart) {
              railCart.setStackCooldown(500);
            }

            current = next;
          }

          AABB bounds = this.getBoundingBox().inflate(50);
          List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, bounds);
          for (ServerPlayer player : players) {
            MRModCriteria.BABEL.get().trigger(player);
            // TODO umm not cool enough packetDistributor.sendToPlayer(player, new
            // BabelScramblePacket());
          }
        }
      }
    }
  }

  @Override
  public @NonNull Vec3 getPassengerRidingPosition(@NonNull Entity passenger) {
    if (!(passenger instanceof AbstractMinecart)) {
      return super.getPassengerRidingPosition(passenger);
    } else {
      return this.position().add(0, this.getBbHeight(), 0);
    }
  }
}
