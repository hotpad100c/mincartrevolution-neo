package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface PowerEmitterMinecartEntity {
  BlockPos.MutableBlockPos previousBlockPos = new BlockPos.MutableBlockPos();
  boolean updatedBlocks = false;

  default BlockPos getPreviousBlockPos() {
    return previousBlockPos;
  }

  default void setPreviousBlockPos(BlockPos pos) {
    previousBlockPos.set(pos);
  }

  default void updateNeighbors(Level world, BlockPos pos, Block block) {
    if(!isEmittingSignal(pos)){return;}
    world.updateNeighborsAt(pos, block);
    for (Direction direction : Direction.values()) {
      world.updateNeighborsAt(pos.relative(direction), block);
    }
  }
  default boolean isEmittingSignal(BlockPos pos){
    for (Direction direction : Direction.values()){
      if(getPowerStrength(direction, pos) > 0) return true;
    }
    return false;
  }

  default int getPowerStrength(Direction direction, BlockPos pos) {
    return 0;
  }
}
