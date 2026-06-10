package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.*;
import ml.mypals.minecartrevolution.entity.chain.ChainEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class ChainRenderer extends EntityRenderer<ChainEntity, EntityRenderState> {

    private static final float LINE_WIDTH = 0.5f;

    public ChainRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @EventBusSubscriber(modid = "minecartrevolution", value = Dist.CLIENT)
    public static class RenderHandler {
        @SubscribeEvent
        public static void onSubmitCustomGeometryEvent(SubmitCustomGeometryEvent event) {

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            Entity camEntity = mc.getCameraEntity();
            if (camEntity == null) return;
            Vec3 camPos = event.getLevelRenderState().cameraRenderState.pos;

            PoseStack poseStack = event.getPoseStack();

            BlockState blockState = Blocks.IRON_CHAIN.defaultBlockState();
            TextureAtlasSprite blockTexture = getBlockTexture(blockState);
            RenderType renderType = RenderTypes.entityCutout(blockTexture.atlasLocation());

            float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

            event.getSubmitNodeCollector().submitCustomGeometry(poseStack, renderType, (renderPoseStack, vc) -> {
                Matrix4f mat = renderPoseStack.pose();
                for (ChainEntity chain : mc.level.getEntitiesOfClass(ChainEntity.class,
                        mc.player.getBoundingBox().inflate(64))) {
                    var segments = chain.segments;
                    if (segments == null || segments.size() < 2) continue;

                    Entity e1 = mc.level.getEntity(chain.getCartAId());
                    Entity e2 = mc.level.getEntity(chain.getCartBId());
                    if (!(e1 instanceof AbstractMinecart cartA) || !(e2 instanceof AbstractMinecart cartB)) continue;

                    int r = 255, g = 255, b = 255, a = 255;
                    float u0 = blockTexture.getU0();
                    float u1 = blockTexture.getU1();
                    float v0 = blockTexture.getV0();
                    float v1 = blockTexture.getV1();
                    int overlay = OverlayTexture.NO_OVERLAY;
                    int light = 15728880;

                    Vec3 attachA = cartA.getPosition(partialTicks).add(0, cartA.getBbHeight() * 0.75, 0);
                    Vec3 attachB = cartB.getPosition(partialTicks).add(0, cartB.getBbHeight() * 0.75, 0);

                    for (int i = 1; i < segments.size(); i++) {
                        ChainEntity.ChainSegment prevSeg = segments.get(i - 1);
                        ChainEntity.ChainSegment currSeg = segments.get(i);
                        
                        Vec3 prev = i == 1 ? attachA : prevSeg.oldPosition.lerp(prevSeg.position, partialTicks);
                        Vec3 curr = i == segments.size() - 1 ? attachB : currSeg.oldPosition.lerp(currSeg.position, partialTicks);

                        Vec3 dir = curr.subtract(prev);
                        if (dir.lengthSqr() < 0.0001) continue;
                        dir = dir.normalize();

                        Vec3 up = new Vec3(0, 1, 0);
                        if (Math.abs(dir.y) > 0.99) {
                            up = new Vec3(1, 0, 0);
                        }
                        Vec3 right = dir.cross(up).normalize();
                        up = right.cross(dir).normalize();

                        float hw = LINE_WIDTH / 2.0f;

                        // Convert to camera-relative coordinates
                        Vec3 relPrev = prev.subtract(camPos);
                        Vec3 relCurr = curr.subtract(camPos);

                        // Vanilla chain texture offset correction
                        // Tweak this value to perfectly center the chain pixels on the axis
                        float TEXTURE_OFFSET = hw * 0.5f+0.03f;

                        Vec3 p1_0 = relPrev.subtract(right.scale(hw - TEXTURE_OFFSET));
                        Vec3 p1_1 = relPrev.add(right.scale(hw + TEXTURE_OFFSET));
                        Vec3 p1_2 = relCurr.add(right.scale(hw + TEXTURE_OFFSET));
                        Vec3 p1_3 = relCurr.subtract(right.scale(hw - TEXTURE_OFFSET));

                        Vec3 p2_0 = relPrev.subtract(up.scale(hw - TEXTURE_OFFSET));
                        Vec3 p2_1 = relPrev.add(up.scale(hw + TEXTURE_OFFSET));
                        Vec3 p2_2 = relCurr.add(up.scale(hw + TEXTURE_OFFSET));
                        Vec3 p2_3 = relCurr.subtract(up.scale(hw - TEXTURE_OFFSET));

                        Matrix3f normalMat = renderPoseStack.normal();

                        Vector3f n_up = new Vector3f((float) up.x, (float) up.y, (float) up.z);
                        n_up.mul(normalMat).normalize();

                        Vector3f n_down = new Vector3f((float) -up.x, (float) -up.y, (float) -up.z);
                        n_down.mul(normalMat).normalize();

                        Vector3f n_right = new Vector3f((float) right.x, (float) right.y, (float) right.z);
                        n_right.mul(normalMat).normalize();

                        Vector3f n_left = new Vector3f((float) -right.x, (float) -right.y, (float) -right.z);
                        n_left.mul(normalMat).normalize();

                        // Plane 1 Front
                        vc.addVertex(mat, (float) p1_0.x, (float) p1_0.y, (float) p1_0.z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(n_up.x, n_up.y, n_up.z);
                        vc.addVertex(mat, (float) p1_1.x, (float) p1_1.y, (float) p1_1.z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(n_up.x, n_up.y, n_up.z);
                        vc.addVertex(mat, (float) p1_2.x, (float) p1_2.y, (float) p1_2.z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(n_up.x, n_up.y, n_up.z);
                        vc.addVertex(mat, (float) p1_3.x, (float) p1_3.y, (float) p1_3.z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(n_up.x, n_up.y, n_up.z);

                        // Plane 1 Back
                        vc.addVertex(mat, (float) p1_3.x, (float) p1_3.y, (float) p1_3.z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(n_down.x, n_down.y, n_down.z);
                        vc.addVertex(mat, (float) p1_2.x, (float) p1_2.y, (float) p1_2.z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(n_down.x, n_down.y, n_down.z);
                        vc.addVertex(mat, (float) p1_1.x, (float) p1_1.y, (float) p1_1.z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(n_down.x, n_down.y, n_down.z);
                        vc.addVertex(mat, (float) p1_0.x, (float) p1_0.y, (float) p1_0.z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(n_down.x, n_down.y, n_down.z);

                        // Plane 2 Front
                        vc.addVertex(mat, (float) p2_0.x, (float) p2_0.y, (float) p2_0.z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(n_right.x, n_right.y, n_right.z);
                        vc.addVertex(mat, (float) p2_1.x, (float) p2_1.y, (float) p2_1.z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(n_right.x, n_right.y, n_right.z);
                        vc.addVertex(mat, (float) p2_2.x, (float) p2_2.y, (float) p2_2.z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(n_right.x, n_right.y, n_right.z);
                        vc.addVertex(mat, (float) p2_3.x, (float) p2_3.y, (float) p2_3.z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(n_right.x, n_right.y, n_right.z);

                        // Plane 2 Back
                        vc.addVertex(mat, (float) p2_3.x, (float) p2_3.y, (float) p2_3.z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(n_left.x, n_left.y, n_left.z);
                        vc.addVertex(mat, (float) p2_2.x, (float) p2_2.y, (float) p2_2.z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(n_left.x, n_left.y, n_left.z);
                        vc.addVertex(mat, (float) p2_1.x, (float) p2_1.y, (float) p2_1.z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(n_left.x, n_left.y, n_left.z);
                        vc.addVertex(mat, (float) p2_0.x, (float) p2_0.y, (float) p2_0.z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(n_left.x, n_left.y, n_left.z);
                    }
                }
            });
        }
    }
    private static TextureAtlasSprite getBlockTexture(BlockState blockState) {

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        return model.particleMaterial().sprite();
    }
}
