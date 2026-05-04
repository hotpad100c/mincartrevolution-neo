package ml.mypals.minecartrevolution;

import ml.mypals.minecartrevolution.advancements.criterion.MRModCriteria;
import ml.mypals.minecartrevolution.datagen.MRAdvancementProvider;
import ml.mypals.minecartrevolution.entity.minecarts.MRModEntities;
import ml.mypals.minecartrevolution.item.MRModItems;
import ml.mypals.minecartrevolution.packets.JukeboxUpdateS2CPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static ml.mypals.minecartrevolution.advancements.criterion.MRModCriteria.TRIGGERS;
import static ml.mypals.minecartrevolution.entity.minecarts.MRModEntities.ENTITIES;
import static ml.mypals.minecartrevolution.item.MRModItems.*;
import static net.neoforged.neoforge.common.NeoForgeMod.MOD_ID;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MinecartRevolution.MODID)
public class MinecartRevolution {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "minecartrevolution";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Creates a creative tab with the id "mincartrevolution:example_tab" for the example item, that is placed after the combat tab


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
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TRIGGERS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);


        /*
        MRModCriteria.init();
        MRModItems.init();
        MRModEntities.init();*/
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
