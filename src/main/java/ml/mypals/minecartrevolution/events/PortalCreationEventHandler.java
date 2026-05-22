package ml.mypals.minecartrevolution.events;

import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.EnderPortalMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class PortalCreationEventHandler {
    @SubscribeEvent
    public void endPortalCreationEvent(BlockEvent.PortalSpawnEvent portalSpawnEvent){
        BlockState portal = portalSpawnEvent.getState();

    }
}
