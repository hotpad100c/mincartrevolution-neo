package ml.mypals.minecartrevolution.mixin.level;

import ml.mypals.minecartrevolution.interfaces.IServerLevelExt;
import ml.mypals.minecartrevolution.manager.PortalMinecartStorage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements BlockAndTintGetter, IServerLevelExt {
  @Unique
  private PortalMinecartStorage mincartrevolution_neo$portalMinecartStorage =
      new PortalMinecartStorage();

  @Override
  public PortalMinecartStorage mincartrevolution_neo$getPortalMinecartStorage() {
    return mincartrevolution_neo$portalMinecartStorage;
  }
}
