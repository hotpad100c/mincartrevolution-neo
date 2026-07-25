package ml.mypals.minecartrevolution.mixin.compat.yuushya;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.yuushya.modelling.client.anvilcraft.rendering.CachedRegion$RebuildTask")
public class ItemBlockMixin {
  @WrapOperation(
      method = "run()V",
      at =
          @At(
              value = "INVOKE",
              target = "Lorg/joml/Matrix4f;transform(Lorg/joml/Vector4f;)Lorg/joml/Vector4f;"),
      expect = 0)
  private Vector4f transform(
      Matrix4f instance,
      Vector4f v,
      Operation<Vector4f> original,
      @Local(name = "poseStack2") PoseStack poseStack2,
      @Local(name = "layer") ItemStackRenderState.LayerRenderState layer,
      @Local(name = "itemRenderState") ItemStackRenderState itemRenderState,
      @Local(name = "vector4f") Vector4f vector4f) {
    if (itemRenderState.layers.length > 0) {
      ItemStackRenderState.LayerRenderState l1Marker =
          itemRenderState.layers[itemRenderState.layers.length - 1];
      if (l1Marker.argumentForSpecialRendering != null
          && l1Marker.argumentForSpecialRendering.equals(
              "YuushyaModelingCompatTransformCorrectionMarker")) {
        PoseStack.Pose pose = poseStack2.last().copy();
        ((ItemStackRenderStateInvoker) layer).mrXyuushya$applyTransform(pose);
        return pose.pose().transform(vector4f);
      }
    }
    return original.call(instance, v);
  }
}
