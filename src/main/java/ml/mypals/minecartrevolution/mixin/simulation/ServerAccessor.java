package ml.mypals.minecartrevolution.mixin.simulation;

import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface ServerAccessor {
  @Accessor("executor")
  Executor mcr$getExecutor();

  @Accessor("storageSource")
  LevelStorageSource.LevelStorageAccess mcr$getStorageSource();
}
