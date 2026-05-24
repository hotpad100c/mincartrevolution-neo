package ml.mypals.minecartrevolution.helper;

import ml.mypals.minecartrevolution.client.sound.MinecartMusicSoundInstance;
import ml.mypals.minecartrevolution.entity.minecarts.SofaMinecartEntity;
import ml.mypals.minecartrevolution.util.MusicUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class SofaMinecartClientHelper {
    private static MinecartMusicSoundInstance musicInstance = null;

    public static void handleClientMusicLogic(SofaMinecartEntity sofa, int movingTicks) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        boolean isRiding = localPlayer.getVehicle() == sofa;
        boolean isFastEnough = sofa.getDeltaMovement().horizontalDistanceSqr() > 0.3;

        if (isRiding && isFastEnough) {
            movingTicks++;
            if (movingTicks >= 100) {
                ensureMusicPlaying(sofa);
                movingTicks = 100;
            }
            sofa.setMovingTicks(movingTicks);
        } else {
            stopAndResetMusic();
            sofa.setMovingTicks(0);
        }
    }

    private static void ensureMusicPlaying(SofaMinecartEntity sofa) {
        if (MusicUtils.getRawData("sofa") == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (musicInstance == null || !mc.getSoundManager().isActive(musicInstance)) {
            Identifier virtualLoc = Identifier.fromNamespaceAndPath("minecart_music_mp3", "sofa");
            musicInstance = new MinecartMusicSoundInstance(sofa, virtualLoc);
            mc.getSoundManager().play(musicInstance);
        }
    }

    public static void stopAndResetMusic() {
        if (musicInstance != null) {
            musicInstance.stopMusic();
            musicInstance = null;
        }
    }
}