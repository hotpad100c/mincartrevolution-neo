package ml.mypals.minecartrevolution.mixin.blocks;

import com.llamalad7.mixinextras.sugar.Local;
import ml.mypals.minecartrevolution.entity.minecarts.container.DispenserMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.IMinecartSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileDispenseBehavior.class)
public class ProjectileDispenseBehaviorMixin {
  @Shadow @Final private ProjectileItem projectileItem;

  @Shadow @Final private ProjectileItem.DispenseConfig dispenseConfig;

  @Inject(
      method = "execute",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;"),
      cancellable = true)
  private void injectCustomShoot(
      BlockSource source,
      ItemStack dispensed,
      CallbackInfoReturnable<ItemStack> cir,
      @Local ServerLevel level,
      @Local Position position,
      @Local Direction direction) {
    Entity entity = ((IMinecartSource) (Object) source).mincartrevolution_neo$getMinecart();
    if (entity instanceof DispenserMinecartEntity minecart) {
      boolean isClockwise = !minecart.isOnRails();
      Vec3 shotVector = minecart.getOffsetPreciseVector(isClockwise);
      Projectile projectile =
          this.projectileItem.asProjectile(level, position, dispensed, direction);
      Projectile.spawnProjectileUsingShoot(
          projectile,
          level,
          dispensed,
          shotVector.x,
          shotVector.y,
          shotVector.z,
          this.dispenseConfig.power(),
          this.dispenseConfig.uncertainty());

      dispensed.shrink(1);
      cir.setReturnValue(dispensed);
    }
  }
}
