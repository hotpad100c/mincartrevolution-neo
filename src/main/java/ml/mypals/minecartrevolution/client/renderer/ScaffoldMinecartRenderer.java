package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.ScaffoldMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.ScaffoldMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;

public class ScaffoldMinecartRenderer extends AbstractMinecartRenderer<ScaffoldMinecartEntity, ScaffoldMinecartRenderState> {

    private static final float BOTTOM_Y  = -0.2f;
    private static final float TOP_Y = BOTTOM_Y + 0.95f;
    private static final float LAYER_HEIGHT = 0.03f;
    private static final float XZ_WOBBLE  = 0.54f;
    private static final float ITEM_SCALE = 1.15f;
    private static final float COPY_SPREAD = 0.4f;
    private static final float COPY_RISE   = 0.00f;

    private final RandomSource random = RandomSource.create();

    public ScaffoldMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.MINECART);
    }

    @Override
    public @NonNull ScaffoldMinecartRenderState createRenderState() {
        return new ScaffoldMinecartRenderState();
    }

    @Override
    public void extractRenderState(@NonNull ScaffoldMinecartEntity entity, @NonNull ScaffoldMinecartRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        for (int i = 0; i < ScaffoldMinecartEntity.SLOT_COUNT; i++) {
            ItemClusterRenderState cluster = new ItemClusterRenderState();
            ItemStack stack = entity.getStoredItem(i);
            cluster.extractItemGroupRenderState(entity, stack,
                    Minecraft.getInstance().getItemModelResolver());
            cluster.count = Math.min(32,stack.count());
            state.items[i] = cluster;
        }
    }

    @Override
    protected void submitMinecartContents(@NonNull ScaffoldMinecartRenderState state, @NonNull BlockModelRenderState blockModel, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int lightCoords) {
        super.submitMinecartContents(state, blockModel, poseStack, submitNodeCollector, lightCoords);

        if (state.items[0] == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        renderPile(poseStack, submitNodeCollector, lightCoords, state.items, 0, 4, BOTTOM_Y);

        renderPile(poseStack, submitNodeCollector, lightCoords, state.items, 4, 4, TOP_Y);

        poseStack.popPose();
    }

    private void renderPile(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ItemClusterRenderState[] clusters, int offset, int count, float baseY) {
        long pileSeed = 0L;
        for (int i = offset; i < offset + count; i++) {
            if (clusters[i] != null) pileSeed = pileSeed * 31L + clusters[i].seed;
        }
        random.setSeed(pileSeed);

        int layer = 0;
        for (int i = offset; i < offset + count; i++) {
            ItemClusterRenderState cluster = clusters[i];
            if (cluster == null) continue;
            ItemStackRenderState stackState = cluster.item;

            float y = baseY + layer * LAYER_HEIGHT;
            float driftScale = 0.4f + 0.6f * ((float) layer / count);
            float dx = (random.nextFloat() * 2f - 0.5f) * XZ_WOBBLE * driftScale;
            float dz = (random.nextFloat() * 2f - 0.5f) * XZ_WOBBLE * driftScale;
            poseStack.pushPose();
            poseStack.translate(dx, y, dz);
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

            submitMultipleFromCount(poseStack, submitNodeCollector, lightCoords, cluster, random, stackState.getModelBoundingBox());

            poseStack.popPose();
            layer++;
        }
    }

    public static void submitMultipleFromCount(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ItemClusterRenderState state, RandomSource random, AABB modelBoundingBox) {
        int amount = state.count;
        if (amount == 0) return;

        random.setSeed((long) state.seed);
        ItemStackRenderState item = state.item;
        float modelDepth = (float) modelBoundingBox.getZsize();

        if (modelDepth > 0.0625F) {
            item.submit(poseStack, submitNodeCollector, lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);

            for (int i = 1; i < amount; i++) {
                poseStack.pushPose();
                float radius = COPY_SPREAD * ((float) i / (amount - 1 + 1e-4f));
                float angle  = random.nextFloat() * (float)(2.0 * Math.PI);
                float xo = (float) Math.cos(angle) * radius;
                float zo = (float) Math.sin(angle) * radius;
                float yo = COPY_RISE * i + random.nextFloat() * 0.2f;
                xo += (random.nextFloat() * 2f - 1f) * 0.04f;
                zo += (random.nextFloat() * 2f - 1f) * 0.04f;

                poseStack.translate(xo, yo, zo);
                poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360f));
                poseStack.mulPose(Axis.XP.rotationDegrees((random.nextFloat() * 2f - 1f) * 12f));
                poseStack.mulPose(Axis.ZP.rotationDegrees((random.nextFloat() * 2f - 1f) * 12f));
                item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        } else {
            float arcStep   = (float)(2.0 * Math.PI) / Math.max(amount, 1);
            float fanRadius = 0.10f + 0.04f * (amount - 1); // widens with count

            for (int i = 0; i < amount; i++) {
                poseStack.pushPose();
                float angle = arcStep * i + (random.nextFloat() * 2f - 1f) * arcStep * 0.25f;
                float xo = (float) Math.cos(angle) * fanRadius;
                float zo = (float) Math.sin(angle) * fanRadius;
                float yo = COPY_RISE * i;
                poseStack.translate(xo, yo, zo);
                poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(angle) + 90f));
                poseStack.mulPose(Axis.ZP.rotationDegrees((random.nextFloat() * 2f - 1f) * 8f));
                item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }
    }
}
