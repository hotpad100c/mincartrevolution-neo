package ml.mypals.minecartrevolution;

import ml.mypals.minecartrevolution.datagen.MRAdvancementProvider;
import ml.mypals.minecartrevolution.packets.JukeboxUpdateS2CPacket;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.List;

import static ml.mypals.minecartrevolution.registeries.MRMinecarts.ENTITIES;
import static ml.mypals.minecartrevolution.registeries.MRMinecarts.ITEMS;
import static ml.mypals.minecartrevolution.registeries.MRModCriteria.TRIGGERS;
import static ml.mypals.minecartrevolution.registeries.MRModItems.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MinecartRevolution.MODID)
public class MinecartRevolution {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "minecartrevolution";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Creates a creative tab with the id "minecartrevolution:example_tab" for the example item, that is placed after the combat tab


    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
    public static String idString(String path) {
        return MODID + ":" + path;
    }
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MinecartRevolution(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::registerPayloads);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TRIGGERS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);


        /*
        MRModCriteria.init();
        MRModItems.init();
        MRModEntities.init();*/
    }


    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
                JukeboxUpdateS2CPacket.TYPE,
                JukeboxUpdateS2CPacket.STREAM_CODEC
        );
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        registerDispenserBehaviors();
    }


    public void gatherData(GatherDataEvent.Client event) {
        event.createProvider((output, lookupProvider) ->
                new AdvancementProvider(
                output, lookupProvider,
                List.of(
                        MRAdvancementProvider::generate
                )
    ));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
