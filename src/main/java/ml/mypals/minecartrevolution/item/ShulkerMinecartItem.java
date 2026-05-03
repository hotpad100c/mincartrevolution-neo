package ml.mypals.minecartrevolution.item;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import ml.mypals.minecartrevolution.entity.minecarts.MinecartTransformManager;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ShulkerMinecartItem extends MultiVariantMinecartWithBlockItem{
    private static final Component UNKNOWN_CONTENTS_TEXT = Component.translatable("container.shulkerBox.unknownContents");

    public ShulkerMinecartItem(AdvancedMinecartEntityTypes.Type type, Properties settings, Block blockInside) {
        super(type, settings, blockInside);
    }
    /*
    @Override
    public void appendHoverText(@NonNull ItemStack stack, Item.@NonNull TooltipContext context, @NonNull TooltipDisplay tooltip, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltip, builder, tooltipFlag);
        if (stack.has(DataComponents.CONTAINER_LOOT)) {
            tooltip.shows(DataComponents.TOOL);
        }

        int i = 0;
        int j = 0;

        for (ItemStack itemStack : stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().toList()) {
            j++;
            if (i <= 4) {
                i++;
                tooltip.(Component.translatable("container.shulkerBox.itemCount", itemStack.getHoverName(), itemStack.getCount()));
            }
        }

        if (j - i > 0) {
            tooltip.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
        }
    }*/
    @Override
    public AbstractMinecart getCart(ServerLevel serverWorld, double x, double y, double z, AdvancedMinecartEntityTypes.Type type, MinecartWithBlockItem corrospondingItem, ItemStack stack) {

        CustomData nbtCompound = stack.get(DataComponents.CUSTOM_DATA);
        ItemContainerContents inventoryNbt = stack.get(DataComponents.CONTAINER);
        Block block = corrospondingItem.blockInside;
        if(nbtCompound != null && nbtCompound.contains("block_in_minecart")){
            block = Block.stateById(nbtCompound.copyTag().getInt("block_in_minecart").orElse(1)).getBlock();
        }
        AbstractMinecart abstractMinecartEntity = MinecartTransformManager.getTransform(
                serverWorld,corrospondingItem, block,new Vec3(x,y,z),
                type
        );
        if(abstractMinecartEntity instanceof ShulkerMinecartEntity shulkerMinecartEntity){
            if (inventoryNbt != null) {
                inventoryNbt.copyInto(shulkerMinecartEntity.getItemStacks());
            }
        }

        return abstractMinecartEntity;
    }
}
