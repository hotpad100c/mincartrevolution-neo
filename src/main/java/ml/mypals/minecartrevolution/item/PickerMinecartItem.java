package ml.mypals.minecartrevolution.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

public class PickerMinecartItem extends MinecartWithBlockItem {
  public PickerMinecartItem(Properties settings) {
    super(settings, Blocks.AIR);
  }

  @Override
  public @NonNull Component getName(@NonNull ItemStack stack) {
    return Component.translatable("item.minecartrevolution.minecart_picker");
  }
}
