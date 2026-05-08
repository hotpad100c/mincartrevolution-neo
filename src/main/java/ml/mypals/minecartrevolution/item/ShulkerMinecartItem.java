package ml.mypals.minecartrevolution.item;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class ShulkerMinecartItem extends MultiVariantMinecartWithBlockItem {

    public ShulkerMinecartItem(AdvancedMinecartEntityTypes.Type type, Properties settings, Block blockInside) {
        super(type, settings, blockInside);
    }

    @Override
    public AbstractMinecart getCart(ServerLevel serverWorld, double x, double y, double z, AdvancedMinecartEntityTypes.Type type, MinecartWithBlockItem corrospondingItem, ItemStack stack) {

        CustomData nbtCompound = stack.get(DataComponents.CUSTOM_DATA);
        ItemContainerContents inventoryNbt = stack.get(DataComponents.CONTAINER);
        Block block = corrospondingItem.blockInside;
        if (nbtCompound != null && nbtCompound.contains("block_in_minecart")) {
            block = Block.stateById(nbtCompound.copyTag().getInt("block_in_minecart").orElse(1)).getBlock();
        }
        AbstractMinecart abstractMinecartEntity = MinecartTransformManager.getTransform(
                serverWorld, corrospondingItem, Item.byBlock(block), new Vec3(x, y, z),
                type
        );
        if (abstractMinecartEntity instanceof ShulkerMinecartEntity shulkerMinecartEntity) {
            if (inventoryNbt != null) {
                inventoryNbt.copyInto(shulkerMinecartEntity.getItemStacks());
            }
        }

        return abstractMinecartEntity;
    }
}
