package ml.mypals.minecartrevolution.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

public class MultiVariantMinecartWithBlockItem extends MinecartWithBlockItem {
  public MultiVariantMinecartWithBlockItem(Properties settings, Block blockInside) {
    super(settings, blockInside);
  }

}
