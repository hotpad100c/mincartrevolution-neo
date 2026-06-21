package ml.mypals.minecartrevolution.interfaces;

import ml.mypals.minecartrevolution.registeries.MRDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IMinecartWithBlockItem {
  BlockState getBlockInside(ItemStack stack);

  default BlockState getSyncedBlockState(ItemStack stack, Block defaultBlock) {

    BlockState state = stack.get(MRDataComponents.BLOCK_STATE.get());
    if (state != null) return state;

    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    if (customData != null && customData.contains("block_in_minecart")) {
      int id = customData.copyTag().getIntOr("block_in_minecart", 1);
      return Block.stateById(id);
    }

    return defaultBlock.defaultBlockState();
  }
}
