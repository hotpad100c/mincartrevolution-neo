package ml.mypals.minecartrevolution.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class MinecartMusicSoundInstance extends AbstractTickableSoundInstance {
  private final AbstractMinecart minecart;

  public MinecartMusicSoundInstance(AbstractMinecart minecart, Identifier location) {
    super(
        SoundEvent.createVariableRangeEvent(location),
        SoundSource.RECORDS,
        net.minecraft.util.RandomSource.create());
    this.minecart = minecart;
    this.looping = true;
    this.delay = 0;
    this.volume = 1.0F;
    this.pitch = 1.0F;
    this.relative = true;
    this.attenuation = Attenuation.NONE;
  }

  @Override
  public void tick() {
    if (this.minecart.isRemoved()
        || !this.minecart.hasPassenger(
            e -> e == net.minecraft.client.Minecraft.getInstance().player)) {
      this.stop();
      return;
    }
    this.x = (float) this.minecart.getX();
    this.y = (float) this.minecart.getY();
    this.z = (float) this.minecart.getZ();
  }

  public void stopMusic() {
    super.stop();
  }

  @Override
  public boolean canStartSilent() {
    return true;
  }

  @Override
  public boolean canPlaySound() {
    return true;
  }
}
