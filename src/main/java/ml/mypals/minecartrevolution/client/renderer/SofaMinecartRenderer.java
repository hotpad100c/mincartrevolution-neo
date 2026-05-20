package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.minecartrevolution.client.renderer.state.SofaMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.SofaMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

import static ml.mypals.minecartrevolution.MinecartRevolutionClient.SOFA_MODEL_KEY;

public class SofaMinecartRenderer extends AbstractMinecartRenderer<SofaMinecartEntity, SofaMinecartRenderState> {
    public SofaMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.MINECART);
    }

    @Override
    public @NonNull SofaMinecartRenderState createRenderState() {
        return new SofaMinecartRenderState();
    }

    @Override
    public void extractRenderState(@NonNull SofaMinecartEntity entity, @NonNull SofaMinecartRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.state = entity.getDisplayBlockState();

    }


    @Override
    protected void submitMinecartContents(SofaMinecartRenderState state,
                                          @NonNull BlockModelRenderState blockModel,
                                          @NonNull PoseStack poseStack,
                                          @NonNull SubmitNodeCollector submitNodeCollector,
                                          int lightCoords
    ) {
        BlockState blockState = state.state;
        TextureAtlasSprite blockTexture = getBlockTexture(blockState);
        RenderType renderType = RenderTypes.entityTranslucent(blockTexture.atlasLocation());
        poseStack.pushPose();

        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer)->{
            renderSofaWithBlockTexture(pose, vertexConsumer, blockState, state.lightCoords, OverlayTexture.NO_OVERLAY, blockTexture);
        });

        poseStack.popPose();
    }
    public static void renderSofaWithBlockTexture(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            BlockState blockState,
            int packedLight,
            int packedOverlay,
            TextureAtlasSprite blockTexture) {

        List<BakedQuad> sofaModel = Objects.requireNonNull(Minecraft.getInstance().getModelManager().getStandaloneModel(SOFA_MODEL_KEY)).getAll();
        if (sofaModel.isEmpty() || blockState.isAir()) {
            return;
        }



        renderModelWithTexture(
                pose,
                buffer,
                sofaModel,
                packedLight,
                packedOverlay,
                blockTexture
        );
    }

    private static TextureAtlasSprite getBlockTexture(BlockState blockState) {

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        return model.particleMaterial().sprite();
    }

    private static void renderModelWithTexture(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            List<BakedQuad> model,
            int packedLight,
            int packedOverlay,
            TextureAtlasSprite blockTexture) {


        for (BakedQuad quad : model) {
            renderFace(pose, buffer, quad, packedLight, packedOverlay, blockTexture);
        }

    }
    private static void renderFace(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            BakedQuad face,
            int packedLight,
            int packedOverlay,
            TextureAtlasSprite blockTexture) {

        TextureAtlasSprite originalSprite = face.materialInfo().sprite();
        float originalU0 = originalSprite.getU0();
        float originalU1 = originalSprite.getU1();
        float originalV0 = originalSprite.getV0();
        float originalV1 = originalSprite.getV1();
        float uDiff = originalU1 - originalU0;
        float vDiff = originalV1 - originalV0;

        for (int i = 0; i < 4; i++) {
            long packed = face.packedUV(i);
            float u = UVPair.unpackU(packed);
            float v = UVPair.unpackV(packed);

            float relativeU = uDiff != 0.0f ? (u - originalU0) / uDiff : 0.0f;
            float relativeV = vDiff != 0.0f ? (v - originalV0) / vDiff : 0.0f;

            float newU = blockTexture.getU(relativeU);
            float newV = blockTexture.getV(relativeV);

            Vector3f vector3f = new Vector3f();
            BakedNormals.unpack(face.bakedNormals().normal(i),vector3f);

            buffer.addVertex(
                            pose.pose(),
                            face.position(i).x(),
                            face.position(i).y(),
                            face.position(i).z()
                    )
                    .setColor(255, 255, 255, 255)
                    .setUv(newU, newV)
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(vector3f.x(),vector3f.y(),vector3f.z());

        }
    }

}
