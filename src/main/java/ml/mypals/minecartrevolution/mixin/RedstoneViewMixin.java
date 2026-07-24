package ml.mypals.minecartrevolution.mixin;

import java.util.List;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.RedstoneMinecartManager;
import ml.mypals.minecartrevolution.interfaces.IServerLevelExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class RedstoneViewMixin implements LevelAccessor {
  @Override
  public int getSignal(@NotNull BlockPos pos, @NotNull Direction direction) {
    int redstonePowerFromBlock = minecartrevolution_neo$getRedstonePower(pos, direction);
    int redstonePowerFromEntity =
        this.minecartrevolution_neo$getRedstonePowerFromEntity(pos, direction);
    return Math.max(redstonePowerFromBlock, redstonePowerFromEntity);
  }

  @Unique
  private int minecartrevolution_neo$getRedstonePower(BlockPos pos, Direction direction) {
    BlockState blockState = this.getBlockState(pos);
    int i = blockState.getSignal(this, pos, direction);
    return blockState.isRedstoneConductor(this, pos) ? Math.max(i, this.getDirectSignalTo(pos)) : i;
  }

  @Unique
  public int minecartrevolution_neo$getRedstonePowerFromEntity(BlockPos pos, Direction direction) {

    RedstoneMinecartManager manager =
        ((IServerLevelExt) this).mincartrevolution_neo$getRedstoneMinecartManager();
    List<PowerEmitterMinecartEntity> powers = manager.queryAt(pos);
    int power = 0;
    for (int i = 0; i < powers.size(); i++) {
      final int j = powers.get(i).getPowerStrength(direction, pos);
      if (power < j) power = j;
    }
    return power;
  }
}
