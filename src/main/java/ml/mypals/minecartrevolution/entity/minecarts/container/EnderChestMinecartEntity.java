package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.client.menu.MinecartChestMenu;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class EnderChestMinecartEntity extends BaseMinecartContainer implements IMinecartContainer, ILinkedEnderChest {
    private final ChestLidController chestLidController = new ChestLidController();
    private int openCount = 0;

    public EnderChestMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public EnderChestMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z) {
        super(minecart, world);
        setInitialPos(x, y, z);
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.ENDER_CHEST_MINECART.item().get();
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory) {
        if (this.hasCustomName()) {
            LinkedContainer linkedContainer = LinkedContainerManager.get(this.getCustomName().getString());
            return new MinecartChestMenu(MenuType.GENERIC_9x3, i, inventory, linkedContainer, 3, this);
        }
        return new MinecartChestMenu(MenuType.GENERIC_9x3, i, inventory, inventory.player.getEnderChestInventory(), 3, this);
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.ENDER_CHEST_MINECART.item().get().getDefaultInstance();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasCustomName()) {
            LinkedContainer linkedContainer = LinkedContainerManager.get(this.getCustomName().getString());
            linkedContainer.setActiveMinecart(this);
        }
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
            player.openMenu(provider);
            this.level().broadcastEntityEvent(this, (byte) 10);
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand, pos);
    }

    private @NonNull MenuProvider getMenuProvider() {
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS);
        String containerKey = this.hasCustomName() ? this.getCustomName().getString() : "global_ender_minecart";
        if (containerKey.equalsIgnoreCase("global_ender_minecart")) {
            return new SimpleMenuProvider((id, inv, p) ->
                    new MinecartChestMenu(MenuType.GENERIC_9x3, id, inv, p.getEnderChestInventory(), 3, this),
                    Component.translatable("container.enderchest")
            );
        } else {
            LinkedContainer linkedContainer = LinkedContainerManager.get(containerKey);
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
        Container active = getActiveContainerForHopper();
        return active != null ? active : this;
    }

    private Container getActiveContainerForHopper() {
        if (this.isLinked()) {
            return LinkedContainerManager.get(this.getCustomName().getString());
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        Container active = getActiveContainerForHopper();
        return active == null || active.isEmpty();
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        Container active = getActiveContainerForHopper();
        return active == null ? ItemStack.EMPTY : active.getItem(slot);
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int amount) {
        Container active = getActiveContainerForHopper();
        return active == null ? ItemStack.EMPTY : active.removeItem(slot, amount);
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        Container active = getActiveContainerForHopper();
        return active == null ? ItemStack.EMPTY : active.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        Container active = getActiveContainerForHopper();
        if (active != null) {
            active.setItem(slot, stack);
            this.setChanged();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        Container active = getActiveContainerForHopper();
        if (active != null) {
            active.setChanged();
        }
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {}

    @Override
    public boolean isLinked() {
        return this.hasCustomName();
    }
}
