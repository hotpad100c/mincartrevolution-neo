package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.client.sound.MinecartMusicSoundInstance;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import ml.mypals.minecartrevolution.util.MusicUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class SofaMinecartEntity extends SingleBlockMinecartEntity {
    private MinecartMusicSoundInstance musicInstance = null;
    private int movingTicks = 0;

    public SofaMinecartEntity(EntityType<? extends SofaMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    public SofaMinecartEntity(EntityType<? extends SofaMinecartEntity> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        setCustomDisplayBlockState(Optional.of(Blocks.WHITE_CARPET.defaultBlockState()));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            handleClientMusicLogic();
        } else {
            if (hasPassenger(e -> e instanceof Player)) {
                this.getPassengers().forEach(entity -> {
                    if (entity instanceof Player player) {
                        boolean isFastEnough = this.getDeltaMovement().horizontalDistanceSqr() > 0.3;
                        if (movingTicks >= 100 && isFastEnough) {
                            MRModCriteria.SOFA_AWAY.get().trigger((ServerPlayer) player);
                        }
                        if (isFastEnough) {
                            movingTicks++;
                            if (movingTicks >= 100) movingTicks = 100;
                        } else {
                            movingTicks = 0;
                        }
                    }
                });
            }
        }
    }

    private void handleClientMusicLogic() {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        boolean isRiding = localPlayer.getVehicle() == this;
        boolean isFastEnough = this.getDeltaMovement().horizontalDistanceSqr() > 0.3;

        if (isRiding && isFastEnough) {
            movingTicks++;
            if (movingTicks >= 100) {
                ensureMusicPlaying();
                movingTicks = 100;
            }
        } else {
            stopAndResetMusic();
        }
    }

    private void ensureMusicPlaying() {
        if (MusicUtils.getRawData("sofa") == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (musicInstance == null || !mc.getSoundManager().isActive(musicInstance)) {
            Identifier virtualLoc = Identifier.fromNamespaceAndPath("minecart_music_mp3", "sofa");
            musicInstance = new MinecartMusicSoundInstance(this, virtualLoc);
            mc.getSoundManager().play(musicInstance);
        }
    }

    private void stopAndResetMusic() {
        movingTicks = 0;
        if (musicInstance != null) {
            musicInstance.stopMusic();
            musicInstance = null;
        }
    }

    @Override
    public void remove(@NonNull RemovalReason reason) {
        if (this.level().isClientSide()) {
            stopAndResetMusic();
        }
        super.remove(reason);
    }

    @Override
    public @NonNull Vec3 getPassengerRidingPosition(@NonNull Entity passenger) {
        return super.getPassengerRidingPosition(passenger).add(0, 0.3f, 0);
    }
}