package ml.mypals.minecartrevolution.client.entity;

import ml.mypals.minecartrevolution.entity.minecarts.MRModEntities;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class MRModEntityRenderers {
    public static void init(EntityRenderersEvent.RegisterRenderers registerRenderers){
        registerRenderers.registerEntityRenderer(MRModEntities.DRAGON_EGG_MINECART.get(), (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
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
    }
}
