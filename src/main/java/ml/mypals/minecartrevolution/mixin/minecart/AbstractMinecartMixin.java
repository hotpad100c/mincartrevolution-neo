package ml.mypals.minecartrevolution.mixin.minecart;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

    super.remove(reason);
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private void tick(CallbackInfo ci) {
    if (this.level() instanceof ServerLevel serverLevelx) {
      Leashable.tickLeash(serverLevelx, (Entity & Leashable) this);
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
    cir.setReturnValue(0.3d);
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
