package ml.mypals.minecartrevolution.interfaces;

import ml.mypals.minecartrevolution.entity.minecarts.redstone.RedstoneMinecartManager;
import ml.mypals.minecartrevolution.manager.PortalMinecartStorage;

public interface IServerLevelExt {
  PortalMinecartStorage mincartrevolution_neo$getPortalMinecartStorage();

  RedstoneMinecartManager mincartrevolution_neo$getRedstoneMinecartManager();
}
