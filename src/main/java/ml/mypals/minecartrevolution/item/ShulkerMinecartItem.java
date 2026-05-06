package ml.mypals.minecartrevolution.item;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;

public class ShulkerMinecartItem extends MultiVariantMinecartWithBlockItem {
    private static final Component UNKNOWN_CONTENTS_TEXT = Component.translatable("container.shulkerBox.unknownContents");

    public ShulkerMinecartItem(AdvancedMinecartEntityTypes.Type type, Properties settings, Block blockInside) {
        super(type, settings, blockInside);
    }

    @Override
    public AbstractMinecart getCart(ServerLevel serverWorld, double x, double y, double z, AdvancedMinecartEntityTypes.Type type, MinecartWithBlockItem corrospondingItem, ItemStack stack) {
        AbstractMinecart abstractMinecartEntity = super.getCart(serverWorld, x, y, z, type, corrospondingItem, stack);
        
        ItemContainerContents inventoryNbt = stack.get(DataComponents.CONTAINER);
        if (abstractMinecartEntity instanceof ShulkerMinecartEntity shulkerMinecartEntity) {
            if (inventoryNbt != null) {
                inventoryNbt.copyInto(shulkerMinecartEntity.getItemStacks());
            }
        }

        return abstractMinecartEntity;
    }
}
