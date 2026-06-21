package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import ml.mypals.minecartrevolution.client.renderer.state.ShulkerMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jspecify.annotations.NonNull;

public class ShulkerMinecartRenderer
    extends AbstractMinecartRenderer<ShulkerMinecartEntity, ShulkerMinecartRenderState> {
  private final SpriteGetter sprites;
  private final ShulkerBoxModel internalModel;

  public ShulkerMinecartRenderer(EntityRendererProvider.Context context) {
    super(context, ModelLayers.MINECART);
    this.sprites = context.getSprites();
    this.internalModel = new ShulkerBoxModel(context.bakeLayer(ModelLayers.SHULKER_BOX));
  }

  @Override
  public @NonNull ShulkerMinecartRenderState createRenderState() {
    return new ShulkerMinecartRenderState();
  }

  @Override
  public void extractRenderState(
      @NonNull ShulkerMinecartEntity entity,
      @NonNull ShulkerMinecartRenderState state,
      float partialTick) {
    super.extractRenderState(entity, state, partialTick);
    state.progress = entity.getProgress(partialTick);

    if (entity.getDisplayBlockState().getBlock() instanceof ShulkerBoxBlock shulker) {
      state.color = shulker.getColor();
    } else {
      state.color = null;
    }
  }

  @Override
  protected void submitMinecartContents(
      ShulkerMinecartRenderState state,
      net.minecraft.client.renderer.block.@NonNull BlockModelRenderState blockModel,
      @NonNull PoseStack poseStack,
      @NonNull SubmitNodeCollector submitNodeCollector,
      int lightCoords) {
    super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);
    SpriteId sprite =
        (state.color == null)
            ? Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION
            : Sheets.getShulkerBoxSprite(state.color);

    poseStack.pushPose();
    Transformation transformation = ShulkerBoxRenderer.modelTransform(Direction.UP);
    poseStack.mulPose(transformation.getMatrix());
    this.internalModel.setupAnim(state.progress);
    submitNodeCollector.submitModel(
        this.internalModel,
        state.progress,
        poseStack,
        lightCoords,
        OverlayTexture.NO_OVERLAY,
        -1,
        sprite,
        this.sprites,
        0,
        null);

    poseStack.popPose();
  }

  private static class ShulkerBoxModel extends Model<Float> {
    private final ModelPart lid;

    public ShulkerBoxModel(ModelPart root) {
      super(root, RenderTypes::entityCutout);
      this.lid = root.getChild("lid");
    }

    @Override
    public void setupAnim(Float progress) {
      super.setupAnim(progress);
      this.lid.setPos(0.0F, 24.0F - progress * 0.5F * 16.0F, 0.0F);
      this.lid.yRot = 270.0F * progress * (float) (Math.PI / 180.0);
    }
  }
}
