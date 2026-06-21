package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;

public class ClientSimLevelFactory {

  public static Level create(ClientLevel level, CompatFriendlyBlockMinecartEntity entity) {
    return new SimulatedClientLevel(level, entity);
  }
}
