package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.PortalMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.PortalMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class PortalMinecartRenderer
    extends AbstractMinecartRenderer<PortalMinecartEntity, PortalMinecartRenderState> {
  public PortalMinecartRenderer(EntityRendererProvider.Context context) {
    super(context, ModelLayers.MINECART);
  }

  @Override
  public @NonNull PortalMinecartRenderState createRenderState() {
    return new PortalMinecartRenderState();
  }

  @Override
  public void extractRenderState(
      @NonNull PortalMinecartEntity entity,
      @NonNull PortalMinecartRenderState state,
      float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.portalBlock = entity.getDisplayBlockState();
  }

  protected void renderPlane(
      BlockState portalBlock,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int light) {
    TextureAtlasSprite sprite =
        Minecraft.getInstance()
            .getModelManager()
            .getBlockStateModelSet()
            .get(portalBlock)
            .particleMaterial()
            .sprite();
    submitNodeCollector.submitCustomGeometry(
        poseStack,
        RenderTypes.entityTranslucent(sprite.atlasLocation()),
        (pose, buffer) -> {
          float height = 0.6f;
          buffer
              .addVertex(pose, -2.1f, height, 0.5f)
              .setColor(-1)
              .setUv(sprite.getU0(), sprite.getV0())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
          buffer
              .addVertex(pose, -2.1f, height, 1.9f)
              .setColor(-1)
              .setUv(sprite.getU0(), sprite.getV1())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
          buffer
              .addVertex(pose, -0.3f, height, 1.9f)
              .setColor(-1)
              .setUv(sprite.getU1(), sprite.getV1())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
          buffer
              .addVertex(pose, -0.3f, height, 0.5f)
              .setColor(-1)
              .setUv(sprite.getU1(), sprite.getV0())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
        });
  }

  @Override
  protected void submitMinecartContents(
      PortalMinecartRenderState state,
      @NonNull BlockModelRenderState blockModel,
      @NonNull PoseStack poseStack,
      @NonNull SubmitNodeCollector submitNodeCollector,
      int lightCoords) {
    BlockState portalBlock = state.portalBlock;
    if (portalBlock != null && !portalBlock.isAir()) {
      poseStack.pushPose();
      poseStack.scale(0.75F, 0.75F, 0.75F);
      poseStack.translate(-0.5F, (float) (state.displayOffset - 8) / 16.0F, -0.5F);
      poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

      renderPlane(portalBlock, poseStack, submitNodeCollector, state.lightCoords);

      poseStack.popPose();
    }
  }
}
