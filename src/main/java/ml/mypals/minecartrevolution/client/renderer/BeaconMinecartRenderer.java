package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.BeaconMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.BeaconMinecartEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

public class BeaconMinecartRenderer extends AbstractMinecartRenderer<BeaconMinecartEntity, BeaconMinecartRenderState> {
    public static final Identifier BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/beacon/beacon_beam.png");

    public BeaconMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.MINECART);
    }

    @Override
    public @NonNull BeaconMinecartRenderState createRenderState() {
        return new BeaconMinecartRenderState();
    }

    @Override
    public void extractRenderState(BeaconMinecartEntity entity, BeaconMinecartRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.animationTime = entity.level() != null ? Math.floorMod(entity.level().getGameTime(), 40) + partialTicks : 0.0F;

        long charge = entity.getChargeTicks();
        state.alpha = charge >= 100 ? 1.0f : (float) charge / 100.0f;

        state.sections = entity.getBeamSections().stream()
                .map(section -> new BeaconMinecartRenderState.Section(section.getColor(), section.getHeight()))
                .toList();
    }

    @Override
    protected void submitMinecartContents(BeaconMinecartRenderState state,
                                          @NonNull BlockModelRenderState blockModel,
                                          @NonNull PoseStack poseStack,
                                          @NonNull SubmitNodeCollector submitNodeCollector,
                                          int lightCoords
    ) {
        super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);
        
        if (state.alpha <= 0.0f) return;

        int beamStart = 0;
        for (BeaconMinecartRenderState.Section section : state.sections) {
            int finalBeamStart = beamStart;

            renderBeaconBeam(
                    poseStack,
                    submitNodeCollector,
                    0.8f,
                    state.animationTime,
                    finalBeamStart,
                    section.height(),
                    section.color(),
                    state.alpha
            );

            beamStart += section.height();
        }
    }

    private static void renderBeaconBeam(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        float beamRadiusScale,
        float animationTime,
        int beamStart,
        int height,
        int color,
        float alpha
    ) {
        float solidBeamRadius = 0.2f * beamRadiusScale;
        float beamGlowRadius = 0.25f * beamRadiusScale;
        int beamEnd = (int) (beamStart + (height * alpha*alpha));
        
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float scroll = -animationTime;
        float texVOff = Mth.frac(scroll * 0.2F - Mth.floor(scroll * 0.1F));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animationTime * 2.25F - 45.0F));

        int solidColor = ARGB.color((int) (255 * alpha), color);

        submitNodeCollector.submitCustomGeometry(
            poseStack,
            RenderTypes.entityTranslucentEmissive(BEAM_LOCATION),
            (pose, buffer) -> renderPart(
                pose, buffer, solidColor, beamStart, beamEnd, 0.0F, solidBeamRadius, solidBeamRadius, 0.0F, -solidBeamRadius, 0.0F, 0.0F, -solidBeamRadius, 0.0F, 1.0F, height * (0.5F / solidBeamRadius) + texVOff - 1.0F, texVOff - 1.0F
            )
        );
        poseStack.popPose();

        int glowColor = ARGB.color((int) (32 * alpha), color);
        submitNodeCollector.submitCustomGeometry(
            poseStack,
            RenderTypes.entityTranslucentEmissive(BEAM_LOCATION, true),
            (pose, buffer) -> renderPart(
                pose, buffer, glowColor, beamStart, beamEnd, -beamGlowRadius, -beamGlowRadius, beamGlowRadius, -beamGlowRadius, -beamGlowRadius, beamGlowRadius, beamGlowRadius, beamGlowRadius, 0.0F, 1.0F, height + texVOff - 1.0F, texVOff - 1.0F
            )
        );

        poseStack.popPose();
    }

    private static void renderPart(
        PoseStack.Pose pose, VertexConsumer builder, int color, int beamStart, int beamEnd,
        float wnx, float wnz, float enx, float enz, float wsx, float wsz, float esx, float esz,
        float uu1, float uu2, float vv1, float vv2
    ) {
        renderQuad(pose, builder, color, beamStart, beamEnd, wnx, wnz, enx, enz, uu1, uu2, vv1, vv2);
        renderQuad(pose, builder, color, beamStart, beamEnd, esx, esz, wsx, wsz, uu1, uu2, vv1, vv2);
        renderQuad(pose, builder, color, beamStart, beamEnd, enx, enz, esx, esz, uu1, uu2, vv1, vv2);
        renderQuad(pose, builder, color, beamStart, beamEnd, wsx, wsz, wnx, wnz, uu1, uu2, vv1, vv2);
    }

    private static void renderQuad(
        PoseStack.Pose pose, VertexConsumer builder, int color, int beamStart, int beamEnd,
        float wnx, float wnz, float enx, float enz, float uu1, float uu2, float vv1, float vv2
    ) {
        addVertex(pose, builder, color, beamEnd, wnx, wnz, uu2, vv1);
        addVertex(pose, builder, color, beamStart, wnx, wnz, uu2, vv2);
        addVertex(pose, builder, color, beamStart, enx, enz, uu1, vv2);
        addVertex(pose, builder, color, beamEnd, enx, enz, uu1, vv1);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer builder, int color, int y, float x, float z, float u, float v) {
        builder.addVertex(pose, x, (float)y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
