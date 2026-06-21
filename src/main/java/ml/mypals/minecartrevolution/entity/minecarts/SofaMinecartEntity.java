package ml.mypals.minecartrevolution.entity.minecarts;

import java.util.Optional;
import ml.mypals.minecartrevolution.helper.SofaMinecartClientHelper;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class SofaMinecartEntity extends SingleBlockMinecartEntity {
  private int movingTicks = 0;

  public SofaMinecartEntity(EntityType<? extends SofaMinecartEntity> entityType, Level world) {
    super(entityType, world);
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.WHITE_CARPET.defaultBlockState();
  }

  public SofaMinecartEntity(
      EntityType<? extends SofaMinecartEntity> minecart,
      Level world,
      double x,
      double y,
      double z,
      MinecartWithBlockItem correspondingItem) {
    super(minecart, world, x, y, z, correspondingItem);
    setCustomDisplayBlockState(Optional.of(Blocks.WHITE_CARPET.defaultBlockState()));
  }

  @Override
  public void tick() {
    super.tick();

    if (this.level().isClientSide()) {
      SofaMinecartClientHelper.handleClientMusicLogic(this, this.movingTicks);
    } else {
      if (hasPassenger(e -> e instanceof Player)) {
        this.getPassengers()
            .forEach(
                entity -> {
                  if (entity instanceof Player player) {
                    boolean isFastEnough = this.getDeltaMovement().horizontalDistanceSqr() > 0.3;
                    if (movingTicks >= 100 && isFastEnough) {
                      MRModCriteria.SOFA_AWAY.get().trigger((ServerPlayer) player);
                    }
                    if (isFastEnough) {
                      movingTicks++;
                      if (movingTicks >= 100) movingTicks = 100;
                    } else {
                      movingTicks = 0;
                    }
                  }
                });
      }
    }
  }

  @Override
  public void remove(@NonNull RemovalReason reason) {
    if (this.level().isClientSide()) {
      SofaMinecartClientHelper.stopAndResetMusic();
    }
    super.remove(reason);
  }

  @Override
  public @NonNull Vec3 getPassengerRidingPosition(@NonNull Entity passenger) {
    return super.getPassengerRidingPosition(passenger).add(0, 0.3f, 0);
  }

  public void setMovingTicks(int ticks) {
    this.movingTicks = ticks;
  }
}
