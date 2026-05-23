package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.client.menu.MinecartChestMenu;
import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.ILinkedEnderChest;
import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import ml.mypals.minecartrevolution.inventory.LinkedContainer;
import ml.mypals.minecartrevolution.manager.LinkedContainerManager;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;

public class EnderChestMinecartEntity extends VariantBlockMinecartEntity implements IMinecartContainer, ILinkedEnderChest {
    private final ChestLidController chestLidController = new ChestLidController();
    private int openCount = 0;

    public EnderChestMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public EnderChestMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.ENDER_CHEST_MINECART.item().get();
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.ENDER_CHEST_MINECART.item().get().getDefaultInstance();
    }

    @Override
    public void tick() {
        super.tick();
        this.chestLidController.shouldBeOpen(this.openCount > 0);
        this.chestLidController.tickLid();
    }

    public float getOpenness(float partialTick) {
        return this.chestLidController.getOpenness(partialTick);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.openCount++;
        } else if (id == 11) {
            this.openCount = Math.max(0, this.openCount - 1);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        if (!player.isSecondaryUseActive() && !player.isSprinting()) {
            if (this.level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            MenuProvider provider = getMenuProvider();
            if (provider != null) {
                player.openMenu(provider);
                this.level().broadcastEntityEvent(this, (byte) 10);
                return InteractionResult.SUCCESS;
            }
        }
        return super.interact(player, hand, pos);
    }

    @Nullable
    private MenuProvider getMenuProvider() {
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS);
        String containerKey = this.hasCustomName() ? this.getCustomName().getString() : "global_ender_minecart";
        if (containerKey.equalsIgnoreCase("global_ender_minecart")) {
            return new SimpleMenuProvider((id, inv, p) ->
                    new MinecartChestMenu(MenuType.GENERIC_9x3, id, inv, p.getEnderChestInventory(), 3, this),
                    Component.translatable("container.enderchest")
            );
        } else {
            LinkedContainer linkedContainer = LinkedContainerManager.get(containerKey);
            linkedContainer.setActiveMinecart(this);
            return new SimpleMenuProvider((id, inv, _) ->
                    new MinecartChestMenu(MenuType.GENERIC_9x3, id, inv, linkedContainer, 3, this),
                    this.getDisplayName()
            );
        }
    }

    @Override
    public void minecartrevolution$OnContainerClosed(Level level, Player player) {
        this.level().broadcastEntityEvent(this, (byte) 11);
        if (this.openCount == 0) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS);
        }
    }

    @Override
    public Container getContainer() {
        return null;
    }

    @Override
    public boolean isLinked() {
        return this.hasCustomName();
    }
}
