package ml.mypals.minecartrevolution.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.minecartrevolution.client.CameraShakeManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobView", at = @At("TAIL"))
    private void minecartrevolution$applyEnderShake(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (!CameraShakeManager.isActive()) return;

        float intensity = CameraShakeManager.getCurrentIntensity();
        if (intensity <= 0) return;

        long time = Util.getMillis();

        float transX = (float) (Math.sin(time * 0.05) * Math.sin(time * 0.01) * intensity * 0.2);
        float transY = (float) (Math.cos(time * 0.06) * Math.sin(time * 0.015) * intensity * 0.2);
        float transZ = (float) (Math.sin(time * 0.04) * intensity * 0.1);
        
        float rotX = (float) (Math.cos(time * 0.07) * intensity * 1.5);
        float rotY = (float) (Math.sin(time * 0.08) * intensity * 1.5);
        float rotZ = (float) (Math.sin(time * 0.05) * Math.cos(time * 0.02) * intensity * 1.5);

        poseStack.translate(transX, transY, transZ);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotZ));
    }
}
