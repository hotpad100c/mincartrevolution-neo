package ml.mypals.minecartrevolution;

import static ml.mypals.minecartrevolution.registeries.MREntityDataSerializers.ENTITY_DATA_SERIALIZERS;
import static ml.mypals.minecartrevolution.registeries.MRMinecarts.ENTITIES;
import static ml.mypals.minecartrevolution.registeries.MRMinecarts.ITEMS;
import static ml.mypals.minecartrevolution.registeries.MRModCriteria.TRIGGERS;
import static ml.mypals.minecartrevolution.registeries.MRModItems.*;

import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javazoom.jl.player.Player;
import ml.mypals.minecartrevolution.config.Config;
import ml.mypals.minecartrevolution.datagen.MRAdvancementProvider;
import ml.mypals.minecartrevolution.datagen.MRRecipeProvider;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.PortalMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import ml.mypals.minecartrevolution.entity.others.AttackMonsterMinecartGoal;
import ml.mypals.minecartrevolution.entity.others.FearDragonHeadMinecartGoal;
import ml.mypals.minecartrevolution.entity.others.FearMonsterMinecartGoal;
import ml.mypals.minecartrevolution.interfaces.IServerLevelExt;
import ml.mypals.minecartrevolution.manager.LinkedContainerManager;
import ml.mypals.minecartrevolution.manager.MinecartChainManager;
import ml.mypals.minecartrevolution.manager.PortalMinecartStorage;
import ml.mypals.minecartrevolution.packets.*;
import ml.mypals.minecartrevolution.registeries.MRDataComponents;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MinecartRevolution.MODID)
public class MinecartRevolution {
  // Define mod id in a common place for everything to reference
  public static final String MODID = "minecartrevolution";
  // Directly reference a slf4j logger
  public static final Logger LOGGER = LogUtils.getLogger();
  public static List<UUID> FORCE_COMAPTERS = new ArrayList<>();
  public static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath(MODID, path);
  }

  public static String idString(String path) {
    return MODID + ":" + path;
  }

  // The constructor for the mod class is the first code that is run when your mod is loaded.
  // FML will recognize some parameter types like IEventBus or ModContainer and pass them in
  // automatically.
  public MinecartRevolution(IEventBus modEventBus, ModContainer modContainer) {
    modEventBus.addListener(this::commonSetup);
    modEventBus.addListener(this::gatherData);
    modEventBus.addListener(this::gatherServerData);
    modEventBus.addListener(this::registerPayloads);

    ITEMS.register(modEventBus);
    ENTITIES.register(modEventBus);
    TRIGGERS.register(modEventBus);
    CREATIVE_MODE_TABS.register(modEventBus);
    ENTITY_DATA_SERIALIZERS.register(modEventBus);
    MRDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
    NeoForge.EVENT_BUS.register(this);
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  private void registerPayloads(final RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar(MODID);
    registrar.playToClient(JukeboxUpdateS2CPacket.TYPE, JukeboxUpdateS2CPacket.STREAM_CODEC);
    registrar.playToClient(BabelScramblePacket.TYPE, BabelScramblePacket.STREAM_CODEC);
    registrar.playToClient(EnderPortalShakePacket.TYPE, EnderPortalShakePacket.STREAM_CODEC);
    registrar.playToClient(MinecartCollisionPacket.TYPE, MinecartCollisionPacket.STREAM_CODEC);
    registrar.playToServer(ForceCompatRegisterPacket.TYPE, ForceCompatRegisterPacket.STREAM_CODEC,((payload, context) -> {
      if(payload.active()){
        FORCE_COMAPTERS.add(context.player().getUUID());
      }else {
        FORCE_COMAPTERS.remove(context.player().getUUID());
      }
    }));
  }

  private void commonSetup(FMLCommonSetupEvent event) {
    registerDispenserBehaviors();
    MinecartChainManager.chainEntityTypeSupplier = MRMinecarts.CHAIN_ENTITY;
    ml.mypals.minecartrevolution.behaviours.MinecartTransformManager.init();
  }

  public void gatherData(GatherDataEvent.Client event) {
    event.createProvider(
        (output, lookupProvider) ->
            new AdvancementProvider(
                output, lookupProvider, List.of(MRAdvancementProvider::generate)));
    event.createProvider(MRRecipeProvider.Runner::new);
  }

  public void gatherServerData(GatherDataEvent.Server event) {
    event.createProvider(MRRecipeProvider.Runner::new);
  }

  @SubscribeEvent
  public void onServerStarted(ServerStartedEvent event) {
    LinkedContainerManager.INSTANCE.load(event.getServer());
  }

  @SubscribeEvent
  public void onServerStopping(ServerStoppingEvent event) {
    LinkedContainerManager.INSTANCE.save(event.getServer());
  }

  @SubscribeEvent
  public void onEntityJoin(EntityJoinLevelEvent event) {
    if (event.getEntity() instanceof Villager villager) {
      villager.goalSelector.addGoal(1, new FearMonsterMinecartGoal(villager, 0.9));
    } else if (event.getEntity() instanceof Monster monster) {
      monster.goalSelector.addGoal(1, new FearDragonHeadMinecartGoal(monster, 1.5));
    }
    if (event.getEntity() instanceof IronGolem ironGolem) {
      ironGolem.goalSelector.addGoal(
          1, new AttackMonsterMinecartGoal(ironGolem, MobHeadMinecartEntity.class, 5));
    }
    if (event.getEntity() instanceof PortalMinecartEntity portalMinecartEntity) {
      PortalMinecartStorage storage =
          ((IServerLevelExt) event.getLevel()).mincartrevolution_neo$getPortalMinecartStorage();
      storage.add(portalMinecartEntity);
    }
  }

  @SubscribeEvent
  public void onEntityLeave(EntityLeaveLevelEvent event) {

    if (event.getEntity() instanceof PortalMinecartEntity portalMinecartEntity) {
      PortalMinecartStorage storage =
          ((IServerLevelExt) event.getLevel()).mincartrevolution_neo$getPortalMinecartStorage();
      storage.remove(portalMinecartEntity);
    }
  }
}
