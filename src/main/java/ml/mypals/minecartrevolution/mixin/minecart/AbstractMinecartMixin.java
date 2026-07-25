package ml.mypals.minecartrevolution.mixin.minecart;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.RedstoneMinecartManager;
import ml.mypals.minecartrevolution.interfaces.IServerLevelExt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity implements Leashable {

  @Shadow
  protected abstract double getMaxSpeed(ServerLevel level);

  @Shadow @Final private MinecartBehavior behavior;
  @Unique private @Nullable LeashData mincartrevolution_neo$leashData;

  protected AbstractMinecartMixin(EntityType<?> entityType, Level world) {
    super(entityType, world);
  }

  @Override
  public @Nullable LeashData getLeashData() {
    return this.mincartrevolution_neo$leashData;
  }

  @Override
  public void setLeashData(@Nullable LeashData leashData) {
    this.mincartrevolution_neo$leashData = leashData;
  }

  @Override
  public @NonNull Vec3 getLeashOffset() {
    return new Vec3(0.5F * this.getBbWidth(), 0.88F * this.getBbHeight(), 0);
  }

  @Override
  public boolean supportQuadLeash() {
    return true;
  }

  @Override
  public Vec3 @NonNull [] getQuadLeashOffsets() {
    return Leashable.createQuadLeashOffsets(this, 0.0, 0.64, 0.382, 0.88);
  }

  @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
  protected void addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
    this.writeLeashData(output, mincartrevolution_neo$leashData);
  }

  @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
  protected void readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
    this.readLeashData(input);
  }

  public void remove(Entity.@NonNull RemovalReason reason) {
    if (!this.level().isClientSide() && reason.shouldDestroy() && this.isLeashed()) {
      this.dropLeash();
    }
    if (this.level() instanceof ServerLevel serverLevelx) {
      if (this instanceof PowerEmitterMinecartEntity powerEmitter) {
        RedstoneMinecartManager manager =
            ((IServerLevelExt) serverLevelx).mincartrevolution_neo$getRedstoneMinecartManager();
        manager.remove(powerEmitter);
        if (reason.shouldDestroy()) {
          BlockPos currentPos = this.blockPosition();
          serverLevelx.updateNeighborsAt(currentPos, net.minecraft.world.level.block.Blocks.AIR);
          for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            serverLevelx.updateNeighborsAt(
                currentPos.relative(dir), net.minecraft.world.level.block.Blocks.AIR);
          }
        }
      }
    }
    super.remove(reason);
  }

  @Unique private @Nullable BlockPos mincartrevolution_neo$lastBlockPos = null;

  @Inject(method = "tick", at = @At("TAIL"))
  private void tick(CallbackInfo ci) {
    if (this.level() instanceof ServerLevel serverLevelx) {
      Leashable.tickLeash(serverLevelx, (Entity & Leashable) this);

      if (this instanceof PowerEmitterMinecartEntity powerEmitter) {
        BlockPos currentPos = this.blockPosition();
        if (mincartrevolution_neo$lastBlockPos == null) {
          mincartrevolution_neo$lastBlockPos = currentPos;
          serverLevelx.updateNeighborsAt(currentPos, net.minecraft.world.level.block.Blocks.AIR);
          for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            serverLevelx.updateNeighborsAt(
                currentPos.relative(dir), net.minecraft.world.level.block.Blocks.AIR);
          }
        } else if (!currentPos.equals(mincartrevolution_neo$lastBlockPos)) {
          BlockPos oldPos = mincartrevolution_neo$lastBlockPos;
          mincartrevolution_neo$lastBlockPos = currentPos;
          RedstoneMinecartManager manager =
              ((IServerLevelExt) serverLevelx).mincartrevolution_neo$getRedstoneMinecartManager();
          manager.onCartMoved(powerEmitter, oldPos, currentPos);

          serverLevelx.updateNeighborsAt(oldPos, net.minecraft.world.level.block.Blocks.AIR);
          for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            serverLevelx.updateNeighborsAt(
                oldPos.relative(dir), net.minecraft.world.level.block.Blocks.AIR);
          }
          serverLevelx.updateNeighborsAt(currentPos, net.minecraft.world.level.block.Blocks.AIR);
          for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            serverLevelx.updateNeighborsAt(
                currentPos.relative(dir), net.minecraft.world.level.block.Blocks.AIR);
          }
        }
      }
    }
  }

  @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
  private void isPushable(CallbackInfoReturnable<Boolean> cir) {
    if (!this.isOnRails() && !this.onGround()) {
      cir.setReturnValue(false);
    }
  }

  @Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
  private void getMaxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
    cir.setReturnValue(Math.max(0.4d, behavior.getMaxSpeed(level)));
  }

  @WrapMethod(method = "comeOffTrack")
  protected void comeOffTrack(ServerLevel level, Operation<Void> original) {

    double maxSpeed = getMaxSpeed(level);
    Vec3 movement = this.getDeltaMovement();
    this.setDeltaMovement(
        Mth.clamp(movement.x, -maxSpeed, maxSpeed),
        movement.y,
        Mth.clamp(movement.z, -maxSpeed, maxSpeed));

    if (this.onGround()) {
      this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
    }

    this.move(MoverType.SELF, this.getDeltaMovement());

    if (!this.onGround()) {
      this.setDeltaMovement(this.getDeltaMovement().scale(0.99));
    }
  }
}
