package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.*;
import ml.mypals.minecartrevolution.entity.chain.ChainEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class ChainRenderer extends EntityRenderer<ChainEntity, ChainRenderState> {

    private static final float LINE_WIDTH = 0.5f;

    public ChainRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull ChainRenderState createRenderState() {
        return new ChainRenderState();
    }

    @Override
    public void extractRenderState(@NonNull ChainEntity entity, @NonNull ChainRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.segments = entity.segments;
        Entity e1 = entity.level().getEntity(entity.getCartAId());
        Entity e2 = entity.level().getEntity(entity.getCartBId());
        if (e1 instanceof AbstractMinecart cartA && e2 instanceof AbstractMinecart cartB) {
            state.cartA = cartA;
            state.cartB = cartB;
        } else {
            state.cartA = null;
            state.cartB = null;
        }
        state.partialTicks = partialTick;
    }

    @Override
    public void submit(@NonNull ChainRenderState state, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        if (state.segments == null || state.segments.size() < 2 || state.cartA == null || state.cartB == null) return;

        BlockState blockState = Blocks.IRON_CHAIN.defaultBlockState();
        TextureAtlasSprite blockTexture = getBlockTexture(blockState);
        RenderType renderType = RenderTypes.entityCutout(blockTexture.atlasLocation());

        Vec3 entityPos = new Vec3(state.x, state.y, state.z);

        poseStack.pushPose();
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (renderPoseStack, vc) -> {
            Matrix4f mat = renderPoseStack.pose();
            
            int r = 255, g = 255, b = 255, a = 255;
            float u0 = blockTexture.getU0();
            float u1 = blockTexture.getU1();
            float v0 = blockTexture.getV0();
            float v1 = blockTexture.getV1();
            int overlay = OverlayTexture.NO_OVERLAY;
            int light = 15728880;

            Vec3 attachA = state.cartA.getPosition(state.partialTicks).add(0, state.cartA.getBbHeight() * 0.75, 0);
            Vec3 attachB = state.cartB.getPosition(state.partialTicks).add(0, state.cartB.getBbHeight() * 0.75, 0);

            for (int i = 1; i < state.segments.size(); i++) {
                ChainEntity.ChainSegment prevSeg = state.segments.get(i - 1);
                ChainEntity.ChainSegment currSeg = state.segments.get(i);

                Vec3 prev = i == 1 ? attachA : prevSeg.oldPosition.lerp(prevSeg.position, state.partialTicks);
                Vec3 curr = i == state.segments.size() - 1 ? attachB : currSeg.oldPosition.lerp(currSeg.position, state.partialTicks);

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

                // Convert to entity-relative coordinates (poseStack is already relative to entity)
                Vec3 relPrev = prev.subtract(entityPos);
                Vec3 relCurr = curr.subtract(entityPos);

                // Vanilla chain texture offset correction
                float TEXTURE_OFFSET = hw * 0.5f + 0.03f;

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
        });
        poseStack.popPose();
    }
    private static TextureAtlasSprite getBlockTexture(BlockState blockState) {

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        return model.particleMaterial().sprite();
    }
}
