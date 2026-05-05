package ml.mypals.minecartrevolution;

import ml.mypals.minecartrevolution.registeries.MRModEntityRenderers;
import ml.mypals.minecartrevolution.entity.minecarts.JukeboxMinecartEntity;
import ml.mypals.minecartrevolution.packets.JukeboxUpdateS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MinecartRevolution.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = MinecartRevolution.MODID, value = Dist.CLIENT)
public class MinecartRevolutionClient {
    public static HashMap<JukeboxMinecartEntity, SoundInstance> songs = new HashMap<>();
    public MinecartRevolutionClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        MinecartRevolution.LOGGER.info("HELLO FROM CLIENT SETUP");
        MinecartRevolution.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

    }
    @SubscribeEvent // on the mod event bus
    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(
                JukeboxUpdateS2CPacket.TYPE,
                MinecartRevolutionClient::jukeboxEntityPlayUpdate
        );
    }

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        MRModEntityRenderers.init(event);
    }

    private static void jukeboxEntityPlayUpdate(
            final JukeboxUpdateS2CPacket payload, final IPayloadContext context) {
        int entityId = payload.uuid();
        int discId = payload.dickID();
        boolean play = payload.play();
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            JukeboxMinecartEntity jukeboxMinecartEntity = null;
            if (!(client.level.getEntity(entityId) instanceof JukeboxMinecartEntity)) {
                return;
            }
            jukeboxMinecartEntity = (JukeboxMinecartEntity) client.level.getEntity(entityId);
            //ItemStack recordStack = jukeboxMinecartEntity.getDisc().getItem() == null ? ItemStack.EMPTY : jukeboxMinecartEntity.getDisc();

            SoundManager soundSystem = Minecraft.getInstance().getSoundManager();

            AtomicReference<SoundInstance> instance = new AtomicReference<>(songs.get(jukeboxMinecartEntity));

            if (instance.get() != null && soundSystem.isActive(instance.get()) || !play) {
                soundSystem.stop(instance.get());
                songs.remove(instance);
                instance.set(null);
                notifyNearbyEntities(client.level, jukeboxMinecartEntity.blockPosition(), true);
            }else{
                Level world = client.level;
                JukeboxMinecartEntity finalJukeboxMinecartEntity = jukeboxMinecartEntity;

                world.registryAccess().lookupOrThrow(Registries.JUKEBOX_SONG).get(discId).ifPresent(song -> {
                    JukeboxSong jukeboxSong = song.value();
                    SoundEvent soundEvent = jukeboxSong.soundEvent().value();
                    instance.set(new EntityBoundSoundInstance(soundEvent, SoundSource.RECORDS, 4.0f, 1.0f,
                            finalJukeboxMinecartEntity, client.level.getRandom().nextLong()));
                    client.gui.setNowPlaying(jukeboxSong.description());

                    notifyNearbyEntities(client.level, finalJukeboxMinecartEntity.blockPosition(), true);

                    songs.put(finalJukeboxMinecartEntity, instance.get());
                    if (instance.get() != null)
                        soundSystem.play(instance.get());
                });
            }

        });
    }

    public static void notifyNearbyEntities(Level level, BlockPos pos, boolean isPlaying) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3.0))) {
            entity.setRecordPlayingNearby(pos, isPlaying);
        }
    }
}
