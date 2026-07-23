package ml.mypals.minecartrevolution.interfaces;

import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import net.minecraft.core.BlockPos;
import java.util.List;

public interface ILevelChunkRedstoneExt {
    void mincartrevolution_neo$addRedstoneMinecart(PowerEmitterMinecartEntity cart, BlockPos pos);
    void mincartrevolution_neo$removeRedstoneMinecart(PowerEmitterMinecartEntity cart, BlockPos pos);
    List<PowerEmitterMinecartEntity> mincartrevolution_neo$queryRedstoneMinecarts(BlockPos pos);
}
