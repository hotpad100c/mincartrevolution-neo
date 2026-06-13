package ml.mypals.minecartrevolution.registeries;

import ml.mypals.minecartrevolution.client.renderer.*;
import net.minecraft.client.model.geom.ModelLayers;
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
                                if (entityTypeDeferredHolder == MRMinecarts.CHAIN_ENTITY) return;
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
                MRMinecarts.PORTAL_MINECART.entity().get(),
                PortalMinecartRenderer::new
        );

        registerRenderers.registerEntityRenderer(
                MRMinecarts.ENDER_PORTAL_MINECART.entity().get(),
                EnderPortalMinecartRenderer::new
        );
        registerRenderers.registerEntityRenderer(
                MRMinecarts.SOFA_MINECART.entity().get(),
                SofaMinecartRenderer::new
        );

        registerRenderers.registerEntityRenderer(
                MRMinecarts.BLOCK_MINECART.get(),
                CompatFriendlyBlockMinecartRenderer::new
        );

        registerRenderers.registerEntityRenderer(
                MRMinecarts.SCAFFOLD_MINECART.entity().get(),
                ScaffoldMinecartRenderer::new
        );

        registerRenderers.registerEntityRenderer(
                MRMinecarts.CHAIN_ENTITY.get(),
                ChainRenderer::new
        );

        registerRenderers.registerEntityRenderer(
                MRMinecarts.MOB_HEAD_MINECART.entity().get(),
                MobHeadMinecartRenderer::new
        );


        registerRenderers.registerEntityRenderer(
                MRMinecarts.PICKER_MINECART.entity().get(),
                context -> new PickerMinecartRenderer(context)
        );
    }
}
