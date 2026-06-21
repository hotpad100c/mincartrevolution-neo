package ml.mypals.minecartrevolution.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.BeaconMinecartEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
  @WrapOperation(
      method = "extractVisibleEntities",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
  private boolean minecartrevolution$horizontalOnlyCull(
      EntityRenderDispatcher instance,
      Entity entity,
      Frustum culler,
      double camX,
      double camY,
      double camZ,
      Operation<Boolean> original) {
    if (entity instanceof BeaconMinecartEntity) {
      return true;
    }
    return instance.shouldRender(entity, culler, camX, camY, camZ);
  }
}
