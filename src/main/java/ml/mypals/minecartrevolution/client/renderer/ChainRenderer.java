package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.*;
import ml.mypals.minecartrevolution.entity.chain.ChainEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class ChainRenderer extends EntityRenderer<ChainEntity, EntityRenderState> {

    private static final float LINE_WIDTH = 0.08f;

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
        public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            Entity camEntity = mc.getCameraEntity();
            if (camEntity == null) return;
            Vec3 camPos = camEntity.getEyePosition();

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f mat = poseStack.last().pose();

            MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
            RenderType type = RenderTypes.LINES;

            for (ChainEntity chain : mc.level.getEntitiesOfClass(ChainEntity.class,
                    mc.player.getBoundingBox().inflate(64))) {
                var segments = chain.clientSegments;
                if (segments == null || segments.size() < 2) continue;

                VertexConsumer vc = buf.getBuffer(type);
                int r = 64, g = 64, b = 64, a = 255;

                for (int i = 1; i < segments.size(); i++) {
                    Vec3 prev = segments.get(i - 1);
                    Vec3 curr = segments.get(i);
                    if (prev.distanceToSqr(curr) < 0.0001) continue;

                    vc.addVertex(mat, (float) prev.x, (float) prev.y, (float) prev.z)
                            .setColor(r, g, b, a)
                            .setNormal(0.0f, 1.0f, 0.0f)
                            .setLineWidth(2.0f);
                    vc.addVertex(mat, (float) curr.x, (float) curr.y, (float) curr.z)
                            .setColor(r, g, b, a)
                            .setNormal(0.0f, 1.0f, 0.0f)
                            .setLineWidth(2.0f);
                }
            }

            poseStack.popPose();
        }
    }
}
