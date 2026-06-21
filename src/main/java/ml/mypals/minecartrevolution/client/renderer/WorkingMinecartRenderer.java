package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.WorkingMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.NonInventoryWorkingBlockMinecartEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

public class WorkingMinecartRenderer
    extends AbstractMinecartRenderer<
        NonInventoryWorkingBlockMinecartEntity, WorkingMinecartRenderState> {

  private final BookModel bookModel;
  private final SpriteGetter sprites;

  public WorkingMinecartRenderer(EntityRendererProvider.Context context) {
    super(context, ModelLayers.MINECART);
    this.sprites = context.getSprites();
    this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
  }

  @Override
  public @NonNull WorkingMinecartRenderState createRenderState() {
    return new WorkingMinecartRenderState();
  }

  @Override
  public void extractRenderState(
      NonInventoryWorkingBlockMinecartEntity entity,
      WorkingMinecartRenderState state,
      float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.displayBlock = entity.getDisplayBlockState();

    if (state.displayBlock.is(Blocks.ENCHANTING_TABLE)) {
      state.open = Mth.lerp(partialTicks, entity.oBookOpen, entity.bookOpen);
      state.flip = Mth.lerp(partialTicks, entity.oFlip, entity.flip);
      state.time = entity.time + partialTicks;
      float or = entity.bookRotation - entity.oBookRotation;
      while (or >= (float) Math.PI) or -= (float) (Math.PI * 2);
      while (or < (float) -Math.PI) or += (float) (Math.PI * 2);
      state.yRot = entity.oBookRotation + or * partialTicks;
    }
  }

  @Override
  protected void submitMinecartContents(
      WorkingMinecartRenderState state,
      @NonNull BlockModelRenderState blockModel,
      @NonNull PoseStack poseStack,
      @NonNull SubmitNodeCollector submitNodeCollector,
      int lightCoords) {
    super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);
    if (state.displayBlock == null) return;
    if (state.displayBlock.is(Blocks.ENCHANTING_TABLE)) {
      poseStack.pushPose();
      float floatOffset = 0.1F + Mth.sin(state.time * 0.1F) * 0.01F;
      poseStack.translate(0.5F, 0.8F + floatOffset, 0.5F);

      poseStack.mulPose(Axis.YP.rotation(-state.yRot + ((float) Math.PI / 2f)));
      poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));

      float ff1 = Mth.frac(state.flip + 0.25F) * 1.6F - 0.3F;
      float ff2 = Mth.frac(state.flip + 0.75F) * 1.6F - 0.3F;

      BookModel.State bookAnimState =
          BookModel.State.forAnimation(
              state.time, Mth.clamp(ff1, 0.0F, 1.0F), Mth.clamp(ff2, 0.0F, 1.0F), state.open);

      submitNodeCollector.submitModel(
          this.bookModel,
          bookAnimState,
          poseStack,
          lightCoords,
          OverlayTexture.NO_OVERLAY,
          -1,
          EnchantTableRenderer.BOOK_TEXTURE,
          this.sprites,
          state.outlineColor,
          null);

      poseStack.popPose();
    }
  }
}
