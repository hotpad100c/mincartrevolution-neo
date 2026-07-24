package ml.mypals.minecartrevolution.interfaces;

import java.util.List;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import net.minecraft.core.BlockPos;

public interface ILevelChunkRedstoneExt {
  void mincartrevolution_neo$addRedstoneMinecart(PowerEmitterMinecartEntity cart, BlockPos pos);

  void mincartrevolution_neo$removeRedstoneMinecart(PowerEmitterMinecartEntity cart, BlockPos pos);

  List<PowerEmitterMinecartEntity> mincartrevolution_neo$queryRedstoneMinecarts(BlockPos pos);
}
