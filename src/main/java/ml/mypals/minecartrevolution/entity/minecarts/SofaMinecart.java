package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import ml.mypals.minecartrevolution.util.MusicUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lwjgl.openal.AL10;

public class SofaMinecart extends SingleBlockMinecartEntity {
    private int sourceId = -1;
    private int movingTicks = 0;

    public SofaMinecart(EntityType<? extends SofaMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public SofaMinecart(EntityType<? extends SofaMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
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
                        if (movingTicks >= 100 && this.getDeltaMovement().horizontalDistanceSqr() > 0.3) {
                            MRModCriteria.SOFA_AWAY.get().trigger((ServerPlayer) player);
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
                updateMusicPosition();
            }
        } else {
            stopAndResetMusic();
        }
    }

    private void ensureMusicPlaying() {
        int bufferId = MusicUtils.getBuffer("sofa");
        if (bufferId == -1) return;
        if (sourceId == -1) {
            sourceId = AL10.alGenSources();
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
            AL10.alSourcef(sourceId, AL10.AL_GAIN, 1.0f);
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, AL10.AL_TRUE);
            AL10.alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, 1.0f);
            AL10.alSourcef(sourceId, AL10.AL_REFERENCE_DISTANCE, 3.0f);
            MusicUtils.addSource(sourceId);
        }

        int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
        if (state != AL10.AL_PLAYING) {
            AL10.alSourcePlay(sourceId);
        }
    }

    private void updateMusicPosition() {
        if (sourceId != -1) {
            AL10.alSource3f(sourceId, AL10.AL_POSITION, (float)this.getX(), (float)this.getY(), (float)this.getZ());
            AL10.alSource3f(sourceId, AL10.AL_VELOCITY, (float)this.getDeltaMovement().x, (float)this.getDeltaMovement().y, (float)this.getDeltaMovement().z);
        }
    }

    private void stopAndResetMusic() {
        movingTicks = 0;

        if (sourceId != -1) {
            AL10.alSourceStop(sourceId);
            MusicUtils.removeSource(sourceId);
            AL10.alDeleteSources(sourceId);
            sourceId = -1;
        }
    }

    @Override
    public void remove(@NonNull RemovalReason reason) {
        if (this.level().isClientSide() && sourceId != -1) {
            AL10.alSourceStop(sourceId);
            AL10.alDeleteSources(sourceId);
            sourceId = -1;
        }
        super.remove(reason);
    }
}