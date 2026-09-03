package ml.mypals.minecartrevolution;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import ml.mypals.minecartrevolution.client.CameraShakeManager;
import ml.mypals.minecartrevolution.client.light.DynamicLightsStorage;
import ml.mypals.minecartrevolution.client.sound.ChainedJukeboxSoundInstance;
import ml.mypals.minecartrevolution.config.Config;
import ml.mypals.minecartrevolution.entity.minecarts.JukeboxMinecartEntity;
import ml.mypals.minecartrevolution.manager.LinkedContainerManager;
import ml.mypals.minecartrevolution.packets.*;
import ml.mypals.minecartrevolution.registeries.MRModEntityRenderers;
import ml.mypals.minecartrevolution.util.MusicUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class
// annotated with @SubscribeEvent
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class MinecartRevolutionClient {
  public static final Object MODEL_MARKER = new Object();
  public static HashMap<JukeboxMinecartEntity, SoundInstance> songs = new HashMap<>();

  public static final StandaloneModelKey<QuadCollection> SOFA_MODEL_KEY =
      new StandaloneModelKey<>(
          new ModelDebugName() {
            @Override
            public @NonNull String debugName() {
              return MODID + ": Sofa Model";
            }
          });

  @SubscribeEvent // on the mod event bus only on the physical client
  public static void registerAdditional(ModelEvent.RegisterStandalone event) {
    event.register(
        // The model to get
        SOFA_MODEL_KEY,
        // An UnbakedStandaloneModel<T> we care about, in this case one that returns a
        // QuadCollection
        // Can use the static methods from SimpleUnbakedStandaloneModel<T> for simplicity
        SimpleUnbakedStandaloneModel.quadCollection(
            // The model id, relative to `assets/<namespace>/models/<path>.json`
            Identifier.fromNamespaceAndPath(MODID, "sofa/sofa")));
  }

  public MinecartRevolutionClient(ModContainer container) {
    // Allows NeoForge to create a config screen for this mod's configs.
    // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking
    // on config.
    // Do not forget to add translations for your config options to the en_us.json file.
    container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
  }

  @SubscribeEvent // on the mod event bus
  public static void register(RegisterClientPayloadHandlersEvent event) {
    try {
      MusicUtils.prepareMusic(
          "sofa",
          new URL(
              "https://lw-sycdn.kuwo.cn/3213e69d7e5e10e5ecaa132c5f9ac4f0/6a009506/resource/30106/trackmedia/M500003PcGlP2m854L.mp3"));
    } catch (Exception _) {
    }
    event.register(JukeboxUpdateS2CPacket.TYPE, MinecartRevolutionClient::jukeboxEntityPlayUpdate);
    event.register(BabelScramblePacket.TYPE, MinecartRevolutionClient::babelScrambleUpdate);
    event.register(MinecartCollisionPacket.TYPE, MinecartRevolutionClient::minecartCollisionUpdate);
    event.register(EnderPortalShakePacket.TYPE, MinecartRevolutionClient::enderPortalShakeUpdate);
  }

  @SubscribeEvent // on the mod event bus only on the physical client
  public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    MRModEntityRenderers.init(event);
  }

  private static void jukeboxEntityPlayUpdate(
      final JukeboxUpdateS2CPacket payload, final IPayloadContext context) {
    int entityId = payload.uuid();
    int discId = payload.discId();
    boolean play = payload.play();
    Minecraft client = Minecraft.getInstance();
    client.execute(
        () -> {
          JukeboxMinecartEntity jukeboxMinecartEntity = null;
          if (!(client.level.getEntity(entityId) instanceof JukeboxMinecartEntity)) {
            return;
          }
          jukeboxMinecartEntity = (JukeboxMinecartEntity) client.level.getEntity(entityId);

          SoundManager soundSystem = Minecraft.getInstance().getSoundManager();

          AtomicReference<SoundInstance> instance =
              new AtomicReference<>(songs.get(jukeboxMinecartEntity));

          if (instance.get() != null) {
            if (play) {
              if (instance.get() instanceof ChainedJukeboxSoundInstance chained) {
                chained.updateConnections(payload.connectedAmethystIds());
              }
              if (!soundSystem.isActive(instance.get())) {
                soundSystem.play(instance.get());
              }
              return;
            } else {
              soundSystem.stop(instance.get());
              songs.remove(jukeboxMinecartEntity);
              instance.set(null);
              notifyNearbyEntities(client.level, jukeboxMinecartEntity.blockPosition(), false);
              return;
            }
          } else if (play) {
            Level world = client.level;
            JukeboxMinecartEntity finalJukeboxMinecartEntity = jukeboxMinecartEntity;

            world
                .registryAccess()
                .lookupOrThrow(Registries.JUKEBOX_SONG)
                .get(discId)
                .ifPresent(
                    song -> {
                      JukeboxSong jukeboxSong = song.value();
                      SoundEvent soundEvent = jukeboxSong.soundEvent().value();
                      ChainedJukeboxSoundInstance chainedInstance =
                          new ChainedJukeboxSoundInstance(
                              soundEvent,
                              SoundSource.RECORDS,
                              4.0f,
                              1.0f,
                              finalJukeboxMinecartEntity,
                              client.level.getRandom().nextLong(),
                              payload.connectedAmethystIds());
                      instance.set(chainedInstance);
                      client.gui.setNowPlaying(jukeboxSong.description());

                      notifyNearbyEntities(
                          client.level, finalJukeboxMinecartEntity.blockPosition(), true);

                      songs.put(finalJukeboxMinecartEntity, instance.get());
                      if (instance.get() != null) soundSystem.play(instance.get());
                    });
          }
        });
  }

  @SubscribeEvent
  public static void onConfigReload(ModConfigEvent.Reloading event) {
    if (event.getConfig().getSpec() == Config.SPEC
        && Minecraft.getInstance().getConnection() != null) {
      Minecraft.getInstance()
          .getConnection()
          .send(new ForceCompatRegisterPacket(Config.FORCE_COMPATIBILITY.get()));
    }
  }

  private static void babelScrambleUpdate(
      final ml.mypals.minecartrevolution.packets.BabelScramblePacket payload,
      final IPayloadContext context) {
    Minecraft client = Minecraft.getInstance();
    client.execute(
        () -> {
          List<String> languages =
              new ArrayList<>(client.getLanguageManager().getLanguages().keySet());

          if (client.level != null) {
            String randomLang = languages.get(client.level.getRandom().nextInt(languages.size()));

            client.getLanguageManager().setSelected(randomLang);
            client.options.languageCode = randomLang;

            client.reloadResourcePacks();
            client.options.save();
          }
        });
  }

  private static void minecartCollisionUpdate(
      final ml.mypals.minecartrevolution.packets.MinecartCollisionPacket payload,
      final IPayloadContext context) {
    Minecraft client = Minecraft.getInstance();
    client.execute(
        () -> {
          if (client.level != null
              && client.level.getEntity(payload.entityId())
                  instanceof
                  ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity
                      minecart) {
            minecart.onCollision(
                payload.pos(), payload.target(), payload.actual(), payload.delta());
          }
        });
  }

  private static void enderPortalShakeUpdate(
      final EnderPortalShakePacket payload, final IPayloadContext context) {
    Minecraft client = Minecraft.getInstance();
    client.execute(() -> CameraShakeManager.start(payload.durationTicks(), payload.intensity()));
  }

  @SubscribeEvent
  public static void onClientTick(final ClientTickEvent.Post event) {
    CameraShakeManager.tick();
  }

  public static void notifyNearbyEntities(Level level, BlockPos pos, boolean isPlaying) {
    for (LivingEntity entity :
        level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3.0))) {
      entity.setRecordPlayingNearby(pos, isPlaying);
    }
  }

  @SubscribeEvent
  public static void setupBuiltInResourcePack(final AddPackFindersEvent event) {
    event.addPackFinders(
        Identifier.fromNamespaceAndPath(MODID, "resourcepacks/minecartrevolution_3d_minecart"),
        PackType.CLIENT_RESOURCES,
        Component.literal("MinecartRevolution 3D Minecart"),
        PackSource.BUILT_IN,
        true,
        Pack.Position.TOP);
  }

  @SubscribeEvent
  public static void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
    DynamicLightsStorage.clear();
    LinkedContainerManager.clearAll();
  }
}
