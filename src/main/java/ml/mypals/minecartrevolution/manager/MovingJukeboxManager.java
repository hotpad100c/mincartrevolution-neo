package ml.mypals.minecartrevolution.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MovingJukeboxManager extends JukeboxSongPlayer {
    public Vec3 movingPos;
    public MovingJukeboxManager(OnSongChanged changeNotifier, BlockPos pos) {
        super(changeNotifier, pos);
        this.movingPos = Vec3.atCenterOf(pos);
    }
    @Override
    public void tick(LevelAccessor world, @Nullable BlockState state) {
        if (this.getSong() != null) {
            if (((JukeboxSong) this.getSong()).hasFinished(this.getTicksSinceSongStarted())) {
                this.stop(world, state);
            } else {
                if (this.shouldEmitJukeboxPlayingEvent()) {
                    world.gameEvent(GameEvent.JUKEBOX_PLAY, this.movingPos, GameEvent.Context.of(state));
                    spawnNoteParticles(world, this.movingPos);
                }
                ++this.ticksSinceSongStarted;
            }
        }
    }
    private static void spawnNoteParticles(LevelAccessor world, Vec3 pos) {
        if (world instanceof ServerLevel serverWorld) {
            Vec3 vec3d = pos.add((double)0.0F, (double)1.2F, (double)0.0F);
            float f = (float)world.getRandom().nextInt(4) / 24.0F;
            serverWorld.sendParticles(ParticleTypes.NOTE, vec3d.x(), vec3d.y(), vec3d.z(), 0, (double)f, (double)0.0F, (double)0.0F, (double)1.0F);
        }

    }
}
