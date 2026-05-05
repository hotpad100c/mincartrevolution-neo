package ml.mypals.minecartrevolution.entity.minecarts.workingcarts;
import ml.mypals.minecartrevolution.entity.minecarts.HasVariantRegularBlockMinecartEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;

public class NonInventoryWorkingBlockMinecartEntity extends HasVariantRegularBlockMinecartEntity {
    public NonInventoryWorkingBlockMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public NonInventoryWorkingBlockMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Block blockInside) {
        super(minecart, world, x, y, z, blockInside);
    }



    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        if (!player.isSecondaryUseActive()) {
            BlockState blockState = this.getDisplayBlockState();
            if (this.level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            MenuProvider provider = getMenuProvider(blockState);
            if (provider != null) {
                player.openMenu(provider);
                return InteractionResult.SUCCESS;
            }
        }
        return super.interact(player, hand, pos);
    }

    @Nullable
    private MenuProvider getMenuProvider(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.CRAFTING_TABLE) {
            return new SimpleMenuProvider((id, inv, p) -> new CraftingMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.crafting"));
        } else if (block == Blocks.STONECUTTER) {
            return new SimpleMenuProvider((id, inv, p) -> new StonecutterMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.stonecutter"));
        } else if (block == Blocks.LOOM) {
            return new SimpleMenuProvider((id, inv, p) -> new LoomMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.loom"));
        } else if (block == Blocks.CARTOGRAPHY_TABLE) {
            return new SimpleMenuProvider((id, inv, p) -> new CartographyTableMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.cartography_table"));
        } else if (block == Blocks.GRINDSTONE) {
            return new SimpleMenuProvider((id, inv, p) -> new GrindstoneMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.grindstone_title"));
        } else if (block == Blocks.SMITHING_TABLE) {
            return new SimpleMenuProvider((id, inv, p) -> new SmithingMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.upgrade"));
        } else if (block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL) {
            return new SimpleMenuProvider((id, inv, p) -> new AnvilMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.repair"));
        } else if (block == Blocks.ENCHANTING_TABLE) {
            return new SimpleMenuProvider((id, inv, p) -> new EnchantmentMenu(id, inv, ContainerLevelAccess.NULL), Component.translatable("container.enchant"));
        } else if (block == Blocks.ENDER_CHEST) {
            return new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, p.getEnderChestInventory()), Component.translatable("container.enderchest"));
        }
        return null;
    }
}
