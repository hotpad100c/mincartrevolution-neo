package ml.mypals.minecartrevolution.client.sound;

import java.util.List;
import ml.mypals.minecartrevolution.entity.minecarts.JukeboxMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ChainedJukeboxSoundInstance extends AbstractTickableSoundInstance {
  private final JukeboxMinecartEntity jukebox;
  private List<Integer> connectedAmethysts;

  public ChainedJukeboxSoundInstance(
      SoundEvent soundEvent,
      SoundSource soundSource,
      float volume,
      float pitch,
      JukeboxMinecartEntity jukebox,
      long seed,
      List<Integer> connectedAmethysts) {
    super(soundEvent, soundSource, SoundInstance.createUnseededRandom());
    this.jukebox = jukebox;
    this.volume = volume;
    this.pitch = pitch;
    this.connectedAmethysts = connectedAmethysts;
    this.x = (float) jukebox.getX();
    this.y = (float) jukebox.getY();
    this.z = (float) jukebox.getZ();
    this.looping = false;
    this.delay = 0;
  }

  public void updateConnections(List<Integer> connectedAmethysts) {
    this.connectedAmethysts = connectedAmethysts;
  }

  @Override
  public void tick() {
    if (this.jukebox.isRemoved() || this.jukebox.getDisc().isEmpty()) {
      this.stop();
    } else {
      Minecraft client = Minecraft.getInstance();
      if (client.player == null || client.level == null) return;

      Vec3 closestPos = this.jukebox.position();
      double minDistance = client.player.distanceToSqr(closestPos);

      for (Integer entityId : this.connectedAmethysts) {
        Entity entity = client.level.getEntity(entityId);
        if (entity != null && entity.isAlive()) {
          double dist = client.player.distanceToSqr(entity.position());
          if (dist < minDistance) {
            minDistance = dist;
            closestPos = entity.position();
          }
        }
      }

      this.x = closestPos.x;
      this.y = closestPos.y;
      this.z = closestPos.z;
    }
  }
}
