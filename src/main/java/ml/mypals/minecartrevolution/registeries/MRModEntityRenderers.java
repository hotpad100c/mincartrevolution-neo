package ml.mypals.minecartrevolution.registeries;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static ml.mypals.minecartrevolution.registeries.MRModEntities.ENTITIES;

public class MRModEntityRenderers {
    public static void init(EntityRenderersEvent.RegisterRenderers registerRenderers){

        ENTITIES.getEntries()
                .forEach(
                entityTypeDeferredHolder -> {
                    try {
                        EntityType<? extends Minecart> entity = (EntityType<? extends Minecart>) (entityTypeDeferredHolder.get());
                        registerRenderers.registerEntityRenderer(
                                entity,
                                (context) -> new MinecartRenderer(context, ModelLayers.MINECART));

                    }catch (Exception ignored){

                    }
                }
        );/*
        registerRenderers.registerEntityRenderer(MRModEntities.TRAPPED_CHEST_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.SPONGE_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.DAMAGE_CAUSING_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.POWER_PROVIDER_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.BLOCK_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.DIRECTIONAL_POWER_PROVIDER_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.BARREL_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.PRESHER_PLATE_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.WEIGHT_PRESHER_PLATE_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.JUKEBOX_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        registerRenderers.registerEntityRenderer(MRModEntities.SHULKER_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
 */
    }
}
