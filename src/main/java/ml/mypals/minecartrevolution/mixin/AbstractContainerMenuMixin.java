package ml.mypals.minecartrevolution.mixin;

import ml.mypals.minecartrevolution.inventory.ContainerEntityAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
  @Inject(
      method =
          "stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z",
      at = @At(value = "HEAD"),
      cancellable = true)
  private static void stillValid(
      ContainerLevelAccess access,
      Player player,
      Block block,
      CallbackInfoReturnable<Boolean> cir) {
    if (access instanceof ContainerEntityAccess entityAccess) {
      Entity entity = entityAccess.getEntity();
      cir.setReturnValue(entity.isAlive());
    }
  }
}
