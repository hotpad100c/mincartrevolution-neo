package ml.mypals.minecartrevolution.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.server.command.ModIdArgument;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

public class SofaModelLoader {
    private static BlockModel sofaModel;

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        
    }

    public static BlockModel getSofaModel() {
        return sofaModel;
    }
}