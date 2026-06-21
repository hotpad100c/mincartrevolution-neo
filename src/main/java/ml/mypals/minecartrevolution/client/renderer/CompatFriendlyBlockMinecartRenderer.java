package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import ml.mypals.minecartrevolution.client.renderer.state.CompatFriendlyBlockRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.simulation.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.RenderShape;
import org.jspecify.annotations.NonNull;

public class CompatFriendlyBlockMinecartRenderer
    extends AbstractMinecartRenderer<
        CompatFriendlyBlockMinecartEntity, CompatFriendlyBlockRenderState> {
  public CompatFriendlyBlockMinecartRenderer(EntityRendererProvider.Context context) {
    super(context, ModelLayers.MINECART);
  }

  @Override
  public @NonNull CompatFriendlyBlockRenderState createRenderState() {
    return new CompatFriendlyBlockRenderState();
  }

  @Override
  public void extractRenderState(
      @NonNull CompatFriendlyBlockMinecartEntity entity,
      @NonNull CompatFriendlyBlockRenderState state,
      float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    if (entity.blockEntity != null) {
      LevelRenderState levelRenderState =
          ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer)
              .minecartRevolution$geLevelRenderState();
      entity.blockEntity.setLevel(entity.simulatedLevel);
      state.beRenderState =
          Minecraft.getInstance()
              .getBlockEntityRenderDispatcher()
              .tryExtractRenderState(
                  entity.blockEntity,
                  Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true),
                  null,
                  Minecraft.getInstance().gameRenderer.getMainCamera().getCullFrustum());
      state.levelRenderState = levelRenderState;
      state.be = entity.blockEntity;
      state.minecart = entity;
    }
  }

  @Override
  protected void submitMinecartContents(
      @NonNull CompatFriendlyBlockRenderState state,
      @NonNull BlockModelRenderState blockModel,
      @NonNull PoseStack poseStack,
      @NonNull SubmitNodeCollector submitNodeCollector,
      int lightCoords) {

    if (state.be == null || state.be.getBlockState().getRenderShape() == RenderShape.MODEL) {

      List<BlockStateModelPart> modelParts = new ArrayList<>();
      if (Minecraft.getInstance().level != null && state.be != null) {
        BlockStateModel blockStateModel =
            Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .get(state.be.getBlockState());
        blockStateModel.collectParts(Minecraft.getInstance().level.getRandom(), modelParts);
        if (modelParts.isEmpty()
            && state.minecart.simulatedLevel instanceof ClientLevel clientLevel) {
          blockStateModel.collectParts(
              clientLevel,
              state.be.getBlockPos(),
              state.be.getBlockState(),
              Minecraft.getInstance().level.getRandom(),
              modelParts);
          blockModel.modelParts = modelParts;
        }
      }
      super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);
    }
    if (state.be != null && state.beRenderState != null) {
      state.beRenderState.lightCoords = lightCoords;
      CameraRenderState cameraRenderState = state.levelRenderState.cameraRenderState;
      Minecraft.getInstance()
          .getBlockEntityRenderDispatcher()
          .submit(state.beRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }
  }
}
