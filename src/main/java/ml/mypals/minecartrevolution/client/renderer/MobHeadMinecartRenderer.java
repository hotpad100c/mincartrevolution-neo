package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.MobHeadMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.armorstand.ArmorStandModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class MobHeadMinecartRenderer extends AbstractMinecartRenderer<MobHeadMinecartEntity, MobHeadMinecartRenderState> {

    private static final float SKULL_Y = 0.45f;

    private final Map<SkullBlock.Type, SkullModelBase> skullModels;

    public MobHeadMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.MINECART);
        EntityModelSet modelSet = context.getModelSet();
        this.skullModels = new HashMap<>(){};
        skullModels.put(SkullBlock.Types.CREEPER, SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.CREEPER));
        skullModels.put(SkullBlock.Types.PIGLIN, SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.PIGLIN));
        skullModels.put(SkullBlock.Types.SKELETON, SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.SKELETON));
        skullModels.put(SkullBlock.Types.DRAGON, SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.DRAGON));
        skullModels.put(SkullBlock.Types.PLAYER, SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.PLAYER));
        skullModels.put(SkullBlock.Types.WITHER_SKELETON, SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.WITHER_SKELETON));
        skullModels.put(SkullBlock.Types.ZOMBIE, SkullBlockRenderer.createModel(modelSet, SkullBlock.Types.ZOMBIE));
    }

    @Override
    public @NonNull MobHeadMinecartRenderState createRenderState() {
        return new MobHeadMinecartRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull MobHeadMinecartEntity entity,
            @NonNull MobHeadMinecartRenderState state,
            float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        BlockState blockState = entity.getDisplayBlockState();
        state.headBlockState = blockState.isAir() ? null : blockState;
    }

    /*@Override
    protected void submitMinecartContents(
            @NonNull MobHeadMinecartRenderState state,
            @NonNull BlockModelRenderState blockModel,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            int lightCoords) {

        BlockState headState = state.headBlockState;
        if (headState == null || !(headState.getBlock() instanceof AbstractSkullBlock skullBlock)) {
            return;
        }

        SkullBlock.Type skullType = skullWBlock.getType();
        net.minecraft.client.model.object.skull.SkullModelBase skullModel = skullModels.get(skullType);
        if (skullModel == null) return;

        RenderType renderType = SkullBlockRenderer.getSkullRenderType(skullType, null);

        poseStack.pushPose();

        poseStack.translate(state.x,state.y,state.z);

        poseStack.translate(0.0f, SKULL_Y, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        float scale = 0.75f;
        poseStack.scale(scale, scale, scale);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.0f, 0.5f);
            poseStack.scale(-1.0f, -1.0f, 1.0f);
            skullModel.renderToBuffer(poseStack, vertexConsumer, lightCoords, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        });

        poseStack.popPose();
    }*/
}
