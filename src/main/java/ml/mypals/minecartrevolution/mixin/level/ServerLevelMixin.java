package ml.mypals.minecartrevolution.mixin.level;

import ml.mypals.minecartrevolution.entity.minecarts.redstone.RedstoneMinecartManager;
import ml.mypals.minecartrevolution.interfaces.IServerLevelExt;
import ml.mypals.minecartrevolution.manager.PortalMinecartStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements LevelAccessor, IServerLevelExt {

  @Unique
  private final PortalMinecartStorage mincartrevolution_neo$portalMinecartStorage =
      new PortalMinecartStorage();

  @Unique
  private final RedstoneMinecartManager mincartrevolution_neo$redstoneMinecartManager =
      new RedstoneMinecartManager((ServerLevel) (Object) this);

  @Override
  public PortalMinecartStorage mincartrevolution_neo$getPortalMinecartStorage() {
    return mincartrevolution_neo$portalMinecartStorage;
  }

  @Override
  public RedstoneMinecartManager mincartrevolution_neo$getRedstoneMinecartManager() {
    return mincartrevolution_neo$redstoneMinecartManager;
  }
}
