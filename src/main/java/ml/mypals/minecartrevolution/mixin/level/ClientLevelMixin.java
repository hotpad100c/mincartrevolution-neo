package ml.mypals.minecartrevolution.mixin.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulatedClientLevel;
import ml.mypals.minecartrevolution.interfaces.IServerLevelExt;
import ml.mypals.minecartrevolution.manager.PortalMinecartStorage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements BlockAndTintGetter, IServerLevelExt {
  @Unique
  private PortalMinecartStorage mincartrevolution_neo$portalMinecartStorage =
      new PortalMinecartStorage();

  @Override
  public PortalMinecartStorage mincartrevolution_neo$getPortalMinecartStorage() {
    return mincartrevolution_neo$portalMinecartStorage;
  }
  @WrapOperation(method = "<init>",at = @At(target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;", value = "INVOKE"))
  public <T extends Event> T blockSimulatedLevelLoadingEvent(IEventBus instance, T t, Operation<T> original){
    if((Object)this instanceof SimulatedClientLevel){
      return null;
    }else {
      return original.call(instance, t);
    }
  }

}
