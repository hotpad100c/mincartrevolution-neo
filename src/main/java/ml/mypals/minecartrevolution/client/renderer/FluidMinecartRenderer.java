package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.FluidMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.FluidMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.WaterFluid;
import org.jline.utils.Colors;
import org.jspecify.annotations.NonNull;

import java.util.Calendar;
import java.util.Objects;

public class FluidMinecartRenderer extends AbstractMinecartRenderer<FluidMinecartEntity, FluidMinecartRenderState> {
    public FluidMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.MINECART);
    }

    @Override
    public @NonNull FluidMinecartRenderState createRenderState() {
        return new FluidMinecartRenderState();
    }

    @Override
    public void extractRenderState(FluidMinecartEntity entity, FluidMinecartRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.fluidBlock = entity.getDisplayBlockState();

        // Calculate wobble based on movement
        double speed = entity.getDeltaMovement().horizontalDistance();
        float time = (entity.level().getGameTime() + partialTicks) * 0.2F;
        state.wobble = (float) (Math.sin(time) * speed * 0.2F);

        // Shake effect if on activator rail or hurt
        if (entity.activated || entity.getHurtTime() > 0) {
            float shakeTime = (entity.level().getGameTime() + partialTicks) * 0.8F;
            state.shake = (float) (Math.sin(shakeTime) * 0.05F);
        } else {
            state.shake = 0;
        }
    }


    private void renderFluidPlane(BlockState fluidState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet().
                get(fluidState.getFluidState()).stillMaterial().sprite();
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(sprite.atlasLocation()), (pose, buffer) -> {

            int color = -1;
            if(fluidState.is(Blocks.WATER)){
                color = 0xEE4444FF;
            }
            float height = 0.6f;
            buffer.addVertex(pose, -2.1f, height, 0.5f).setColor(color).setUv(sprite.getU0(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
            buffer.addVertex(pose, -2.1f, height, 1.9f).setColor(color).setUv(sprite.getU0(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
            buffer.addVertex(pose, -0.3f, height, 1.9f).setColor(color).setUv(sprite.getU1(), sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
            buffer.addVertex(pose, -0.3f, height, 0.5f).setColor(color).setUv(sprite.getU1(), sprite.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        });
    }

    @Override
    protected void submitMinecartContents(FluidMinecartRenderState state,
                                          @NonNull BlockModelRenderState blockModel,
                                          @NonNull PoseStack poseStack,
                                          @NonNull SubmitNodeCollector submitNodeCollector,
                                          int lightCoords
    ) {
        BlockState fluidState = state.fluidBlock;
        if (fluidState != null && !fluidState.isAir()) {
            poseStack.pushPose();
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(-0.5F, (float)(state.displayOffset - 8) / 16.0F, -0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

            // Custom Fluid Animation: Wobble and Shake
            poseStack.translate(0.5F, 0.5F, 0.5F);
            if (state.wobble != 0) {
                poseStack.mulPose(Axis.XP.rotation(state.wobble));
            }
            if (state.shake != 0) {
                poseStack.mulPose(Axis.ZP.rotation(state.shake));
            }
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            // Render the fluid plane
            renderFluidPlane(fluidState, poseStack, submitNodeCollector, state.lightCoords);

            poseStack.popPose();
        }
    }
}
