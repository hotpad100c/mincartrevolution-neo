package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.minecartrevolution.client.renderer.state.SimulationMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulationBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.simulation.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.jspecify.annotations.NonNull;

public class SimulateBlockMinecartRenderer extends AbstractMinecartRenderer<SimulationBlockMinecartEntity, SimulationMinecartRenderState> {
    public SimulateBlockMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.MINECART);
    }
    @Override
    public @NonNull SimulationMinecartRenderState createRenderState() {
        return new SimulationMinecartRenderState();
    }

    @Override
    public void extractRenderState(@NonNull SimulationBlockMinecartEntity entity, @NonNull SimulationMinecartRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        if(entity.blockEntity != null){
            LevelRenderState levelRenderState = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).minecartRevolution$geLevelRenderState();
            entity.blockEntity.setLevel(entity.simulatedLevel);
            state.beRenderState = Minecraft.getInstance().getBlockEntityRenderDispatcher().tryExtractRenderState(
                    entity.blockEntity,
                    Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks(),
                    null,
                    Minecraft.getInstance().gameRenderer.getMainCamera().getCullFrustum()
            );
            state.levelRenderState = levelRenderState;
            state.be = entity.blockEntity;
        }
    }


    @Override
    protected void submitMinecartContents(SimulationMinecartRenderState state,
                                          @NonNull BlockModelRenderState blockModel,
                                          @NonNull PoseStack poseStack,
                                          @NonNull SubmitNodeCollector submitNodeCollector,
                                          int lightCoords
    ) {
        super.submitMinecartContents(state,blockModel,poseStack,submitNodeCollector,lightCoords);
        if(state.be != null && state.beRenderState != null){
            state.beRenderState.lightCoords = lightCoords;
            CameraRenderState cameraRenderState = state.levelRenderState.cameraRenderState;
            Minecraft.getInstance().getBlockEntityRenderDispatcher().submit(
                    state.beRenderState,
                    poseStack,
                    submitNodeCollector,
                    cameraRenderState
            );
        }
    }
}
