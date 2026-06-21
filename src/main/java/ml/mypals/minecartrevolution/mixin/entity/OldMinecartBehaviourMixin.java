package ml.mypals.minecartrevolution.mixin.entity;

import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviourMixin {

  @Inject(method = "getSlowdownFactor", at = @At("HEAD"), cancellable = true)
  public void getSlowdownFactor(CallbackInfoReturnable<Double> cir) {
    cir.setReturnValue(0.99);
  }
}
