package ml.mypals.minecartrevolution.item;

import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class MultiVariantMinecartWithBlockItem extends MinecartWithBlockItem {
    public MultiVariantMinecartWithBlockItem(Properties settings, Block blockInside) {
        super(settings, blockInside);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack itemStack) {

        String blockName = this.blockInside.getName().getString();
        if(itemStack.getComponents().has(DataComponents.CUSTOM_DATA))
        {
            var customData = itemStack.getComponents().get(DataComponents.CUSTOM_DATA);
            if(customData != null){
                CompoundTag compoundTag = customData.copyTag();

                if(compoundTag.contains("block_in_minecart")){
                    String key = Block.stateById(compoundTag.getIntOr("block_in_minecart", 1)).getBlock().getDescriptionId();
                    blockName = I18n.get(key);
                }
            }
        }
        String cartName = Items.MINECART.getName(Items.MINECART.getDefaultInstance()).getString();

        String prompt = Component.translatable("item.minecartrevolution.minecart_with_block").getString();
        return Component.nullToEmpty(String.format(prompt, blockName, cartName));
    }

}
