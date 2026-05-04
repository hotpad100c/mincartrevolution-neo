package ml.mypals.minecartrevolution.registeries;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static ml.mypals.minecartrevolution.registeries.MRMinecarts.ENTITIES;

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
        );
    }
}
