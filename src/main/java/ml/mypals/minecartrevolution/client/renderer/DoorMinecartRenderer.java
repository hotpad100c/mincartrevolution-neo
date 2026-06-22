package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import ml.mypals.minecartrevolution.client.renderer.state.DoorMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.DoorMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class DoorMinecartRenderer
    extends AbstractMinecartRenderer<DoorMinecartEntity, DoorMinecartRenderState> {

  public DoorMinecartRenderer(EntityRendererProvider.Context context) {
    super(context, ModelLayers.MINECART);
  }

  @Override
  public @NonNull DoorMinecartRenderState createRenderState() {
    return new DoorMinecartRenderState();
  }

  @Override
  public void extractRenderState(
      @NonNull DoorMinecartEntity entity,
      @NonNull DoorMinecartRenderState state,
      float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.doorState = entity.getDisplayBlockState();
  }

  @Override
  protected void submitMinecartContents(
      DoorMinecartRenderState state,
      @NonNull BlockModelRenderState blockModel,
      @NonNull PoseStack poseStack,
      @NonNull SubmitNodeCollector submitNodeCollector,
      int lightCoords) {
    var doorState = state.doorState;
    if (doorState == null || doorState.isAir()) {
      super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);
      return;
    }

    var lowerState =
        doorState.setValue(
            net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF,
            net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
    var upperState =
        doorState.setValue(
            net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF,
            net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);

    var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
    var rand =
        Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getRandom() : null;
    if (rand == null) {
      super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);
      return;
    }

    List<BlockStateModelPart> lowerParts = new ArrayList<>();
    modelSet.get(lowerState).collectParts(rand, lowerParts);
    blockModel.modelParts = lowerParts;
    super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);

    List<BakedQuad> upperQuads = new ArrayList<>();
    List<BlockStateModelPart> upperParts = new ArrayList<>();
    modelSet.get(upperState).collectParts(rand, upperParts);
    for (BlockStateModelPart part : upperParts) {
      for (Direction dir : Direction.values()) {
        upperQuads.addAll(part.getQuads(dir));
      }
      upperQuads.addAll(part.getQuads(null));
    }

    if (upperQuads.isEmpty()) return;

    TextureAtlasSprite sprite = upperQuads.getFirst().materialInfo().sprite();
    var renderType = RenderTypes.entityCutout(sprite.atlasLocation());

    poseStack.pushPose();
    poseStack.translate(0.0, 1.0, 0.0);
    submitNodeCollector.submitCustomGeometry(
        poseStack,
        renderType,
        (pose, buffer) -> {
          for (BakedQuad quad : upperQuads) {
            renderQuad(pose, buffer, quad, lightCoords, OverlayTexture.NO_OVERLAY);
          }
        });
    poseStack.popPose();
  }

  private static void renderQuad(
      PoseStack.Pose pose,
      VertexConsumer buffer,
      BakedQuad quad,
      int lightCoords,
      int overlayCoords) {
    for (int i = 0; i < 4; i++) {
      Vector3f normal = new Vector3f();
      BakedNormals.unpack(quad.bakedNormals().normal(i), normal);
      buffer
          .addVertex(pose, quad.position(i).x(), quad.position(i).y(), quad.position(i).z())
          .setColor(-1)
          .setUv(UVPair.unpackU(quad.packedUV(i)), UVPair.unpackV(quad.packedUV(i)))
          .setOverlay(overlayCoords)
          .setLight(lightCoords)
          .setNormal(normal.x(), normal.y(), normal.z());
    }
  }
}
