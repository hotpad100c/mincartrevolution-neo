package ml.mypals.minecartrevolution.client.renderer;

import static net.minecraft.client.renderer.BiomeColors.WATER_COLOR_RESOLVER;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.FluidMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.FluidMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.NonNull;

public class FluidMinecartRenderer
    extends AbstractMinecartRenderer<FluidMinecartEntity, FluidMinecartRenderState> {
  public FluidMinecartRenderer(EntityRendererProvider.Context context) {
    super(context, ModelLayers.MINECART);
  }

  @Override
  public @NonNull FluidMinecartRenderState createRenderState() {
    return new FluidMinecartRenderState();
  }

  @Override
  public void extractRenderState(
      @NonNull FluidMinecartEntity entity,
      @NonNull FluidMinecartRenderState state,
      float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.fluidBlock = entity.getDisplayBlockState();

    double speed = entity.getDeltaMovement().horizontalDistance();
    float time = (entity.level().getGameTime() + partialTicks) * 0.2F;
    state.wobble = (float) (Math.sin(time) * speed * 0.05F);

    if (entity.activated || entity.getHurtTime() > 0) {
      float shakeTime = (entity.level().getGameTime() + partialTicks) * 0.8F;
      state.shake = (float) (Math.sin(shakeTime) * 0.05F);
    } else {
      state.shake = 0;
    }
  }

  private int getFluidTintColor(BlockPos pos, BlockState fluidState) {
    FluidState fs = fluidState.getFluidState();
    if (fs.isEmpty()) return -1;
    if (fs.is(FluidTags.WATER)) {
      assert Minecraft.getInstance().level != null;
      return Minecraft.getInstance().level.getBlockTint(pos, WATER_COLOR_RESOLVER);
    }
    FluidStack fluidStack = new FluidStack(fs.getType(), fs.getAmount());
    int tint = getFluidColor(getFluidModel(fluidStack), fluidStack);
    int alpha = 0xEE;
    return (alpha << 24) | (tint & 0x00FFFFFF);
  }

  public static int getFluidColor(FluidModel model, FluidStack stack) {
    int color = 0xffffffff;
    if (model.fluidTintSource() != null) color = model.fluidTintSource().colorAsStack(stack);
    return color;
  }

  public static FluidModel getFluidModel(FluidStack stack) {
    return Minecraft.getInstance()
        .getModelManager()
        .getFluidStateModelSet()
        .get(stack.getFluid().defaultFluidState());
  }

  private void renderFluidPlane(
      BlockPos pos,
      BlockState fluidState,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int light) {
    TextureAtlasSprite sprite =
        Minecraft.getInstance()
            .getModelManager()
            .getFluidStateModelSet()
            .get(fluidState.getFluidState())
            .stillMaterial()
            .sprite();
    int color = getFluidTintColor(pos, fluidState);
    submitNodeCollector.submitCustomGeometry(
        poseStack,
        RenderTypes.entityTranslucent(sprite.atlasLocation()),
        (pose, buffer) -> {
          float height = 0.6f;
          buffer
              .addVertex(pose, -2.1f, height, 0.5f)
              .setColor(color)
              .setUv(sprite.getU0(), sprite.getV0())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
          buffer
              .addVertex(pose, -2.1f, height, 1.9f)
              .setColor(color)
              .setUv(sprite.getU0(), sprite.getV1())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
          buffer
              .addVertex(pose, -0.3f, height, 1.9f)
              .setColor(color)
              .setUv(sprite.getU1(), sprite.getV1())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
          buffer
              .addVertex(pose, -0.3f, height, 0.5f)
              .setColor(color)
              .setUv(sprite.getU1(), sprite.getV0())
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(light)
              .setNormal(pose, 0.0f, 1.0f, 0.0f);
        });
  }

  @Override
  protected void submitMinecartContents(
      FluidMinecartRenderState state,
      @NonNull BlockModelRenderState blockModel,
      @NonNull PoseStack poseStack,
      @NonNull SubmitNodeCollector submitNodeCollector,
      int lightCoords) {
    BlockState fluidState = state.fluidBlock;
    if (fluidState != null && !fluidState.isAir()) {
      poseStack.pushPose();
      poseStack.scale(0.75F, 0.75F, 0.75F);
      poseStack.translate(-0.5F, (float) (state.displayOffset - 8) / 16.0F, -0.5F);
      poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

      poseStack.translate(0.5F, 0.5F, 0.5F);
      if (state.wobble != 0) {
        poseStack.mulPose(Axis.XP.rotation(state.wobble));
      }
      if (state.shake != 0) {
        poseStack.mulPose(Axis.ZP.rotation(state.shake));
      }
      poseStack.translate(-0.5F, -0.5F, -0.5F);

      renderFluidPlane(
          BlockPos.containing(new Vec3(state.x, state.y, state.z)),
          fluidState,
          poseStack,
          submitNodeCollector,
          state.lightCoords);

      poseStack.popPose();
    }
  }
}
