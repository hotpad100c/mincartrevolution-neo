package ml.mypals.minecartrevolution.registeries;

import ml.mypals.minecartrevolution.client.renderer.*;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulationBlockMinecartEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static ml.mypals.minecartrevolution.registeries.MRMinecarts.ENTITIES;

public class MRModEntityRenderers {
    public static void init(EntityRenderersEvent.RegisterRenderers registerRenderers) {
        ENTITIES.getEntries()
                .forEach(
                        entityTypeDeferredHolder -> {
                            try {
                                EntityType<? extends AbstractMinecart> entity = (EntityType<? extends AbstractMinecart>) (entityTypeDeferredHolder.get());
                                registerRenderers.registerEntityRenderer(
                                        entity,
                                        (context) -> new MinecartRenderer(context, ModelLayers.MINECART));

                            } catch (Exception ignored) {

                            }
                        }
                );

        registerRenderers.registerEntityRenderer(
                MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                WorkingMinecartRenderer::new
        );

        registerRenderers.registerEntityRenderer(
                MRMinecarts.SHULKER_MINECART.entity().get(),
                ShulkerMinecartRenderer::new
        );
        registerRenderers.registerEntityRenderer(
                MRMinecarts.BEACON_MINECART.entity().get(),
                BeaconMinecartRenderer::new
        );
        registerRenderers.registerEntityRenderer(
                MRMinecarts.WATER_MINECART.entity().get(),
                FluidMinecartRenderer::new
        );
        registerRenderers.registerEntityRenderer(
                MRMinecarts.LAVA_MINECART.entity().get(),
                FluidMinecartRenderer::new
        );
        registerRenderers.registerEntityRenderer(
                MRMinecarts.BLOCK_MINECART.entity().get(),
                SimulateBlockMinecartRenderer::new
        );
    }
}
