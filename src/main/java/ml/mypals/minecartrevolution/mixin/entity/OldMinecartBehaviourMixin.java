package ml.mypals.minecartrevolution.mixin.entity;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviourMixin extends MinecartBehavior {

  protected OldMinecartBehaviourMixin(AbstractMinecart minecart) {
    super(minecart);
  }

  @Inject(method = "getSlowdownFactor", at = @At("HEAD"), cancellable = true)
  public void getSlowdownFactor(CallbackInfoReturnable<Double> cir) {

    double slow = this.minecart.onGround() ? this.minecart.isVehicle() ? 0.997 : 0.96 : 0.99;
    cir.setReturnValue(slow);
  }
}
