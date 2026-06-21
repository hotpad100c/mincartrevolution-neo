package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.PickerMinecartEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class PickerMinecartRenderer
    extends AbstractMinecartRenderer<PickerMinecartEntity, MinecartRenderState> {
  private static final Identifier TEXTURE =
      Identifier.fromNamespaceAndPath(
          MinecartRevolution.MODID, "textures/entity/minecart/picker_minecart.png");

  public PickerMinecartRenderer(EntityRendererProvider.Context context) {
    super(context, ModelLayers.MINECART);
  }

  public @NonNull MinecartRenderState createRenderState() {
    return new MinecartRenderState();
  }

  public void submit(
      @NonNull MinecartRenderState state,
      @NonNull PoseStack poseStack,
      @NonNull SubmitNodeCollector submitNodeCollector,
      @NonNull CameraRenderState camera) {
    super.submit(state, poseStack, submitNodeCollector, camera);
    poseStack.pushPose();
    long seed = state.offsetSeed;
    float offsetX = (((float) (seed >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
    float offsetY = (((float) (seed >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
    float offsetZ = (((float) (seed >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
    poseStack.translate(offsetX, offsetY, offsetZ);
    if (state.isNewRender) {
      newRender(state, poseStack);
    } else {
      oldRender(state, poseStack);
    }

    float hurt = state.hurtTime;
    if (hurt > 0.0F) {
      poseStack.mulPose(
          Axis.XP.rotationDegrees(
              Mth.sin((double) hurt) * hurt * state.damageTime / 10.0F * (float) state.hurtDir));
    }

    BlockModelRenderState displayBlockModel = state.displayBlockModel;
    if (!displayBlockModel.isEmpty()) {
      poseStack.pushPose();
      poseStack.scale(0.75F, 0.75F, 0.75F);
      poseStack.translate(-0.5F, (float) (state.displayOffset - 8) / 16.0F, 0.5F);
      poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      this.submitMinecartContents(
          state, displayBlockModel, poseStack, submitNodeCollector, state.lightCoords);
      poseStack.popPose();
    }

    poseStack.scale(-1.0F, -1.0F, 1.0F);
    submitNodeCollector.submitModel(
        this.model,
        state,
        poseStack,
        TEXTURE,
        state.lightCoords,
        OverlayTexture.NO_OVERLAY,
        state.outlineColor,
        (ModelFeatureRenderer.CrumblingOverlay) null);
    poseStack.popPose();
  }

  private static <S extends MinecartRenderState> void newRender(S state, PoseStack poseStack) {
    poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
    poseStack.mulPose(Axis.ZP.rotationDegrees(-state.xRot));
    poseStack.translate(0.0F, 0.375F, 0.0F);
  }

  private static <S extends MinecartRenderState> void oldRender(S state, PoseStack poseStack) {
    double entityX = state.x;
    double entityY = state.y;
    double entityZ = state.z;
    float xRot = state.xRot;
    float rotation = state.yRot;
    if (state.posOnRail != null && state.frontPos != null && state.backPos != null) {
      Vec3 frontPos = state.frontPos;
      Vec3 backPos = state.backPos;
      poseStack.translate(
          state.posOnRail.x - entityX,
          (frontPos.y + backPos.y) / (double) 2.0F - entityY,
          state.posOnRail.z - entityZ);
      Vec3 direction = backPos.add(-frontPos.x, -frontPos.y, -frontPos.z);
      if (direction.length() != (double) 0.0F) {
        direction = direction.normalize();
        rotation = (float) (Math.atan2(direction.z, direction.x) * (double) 180.0F / Math.PI);
        xRot = (float) (Math.atan(direction.y) * (double) 73.0F);
      }
    }

    poseStack.translate(0.0F, 0.375F, 0.0F);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotation));
    poseStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
  }
}
