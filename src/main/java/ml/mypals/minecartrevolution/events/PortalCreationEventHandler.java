package ml.mypals.minecartrevolution.events;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class PortalCreationEventHandler {
  @SubscribeEvent
  public void endPortalCreationEvent(BlockEvent.PortalSpawnEvent portalSpawnEvent) {
    BlockState portal = portalSpawnEvent.getState();
  }
}
