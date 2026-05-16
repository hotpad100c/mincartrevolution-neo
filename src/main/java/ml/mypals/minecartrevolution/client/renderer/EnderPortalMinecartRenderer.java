package ml.mypals.minecartrevolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.client.renderer.state.PortalMinecartRenderState;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.PortalMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class EnderPortalMinecartRenderer extends PortalMinecartRenderer {

    public EnderPortalMinecartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderPlane(BlockState portalBlock, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.endGateway(), (pose, buffer) -> {
            float height = 0.6f;
            buffer.addVertex(pose, -2.1f, height, 0.5f);
            buffer.addVertex(pose, -2.1f, height, 1.9f);
            buffer.addVertex(pose, -0.3f, height, 1.9f);
            buffer.addVertex(pose, -0.3f, height, 0.5f);
        });
    }

}
