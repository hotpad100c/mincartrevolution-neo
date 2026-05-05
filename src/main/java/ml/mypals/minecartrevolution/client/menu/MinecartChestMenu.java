package ml.mypals.minecartrevolution.client.menu;

import ml.mypals.minecartrevolution.entity.minecarts.workingcarts.NonInventoryWorkingBlockMinecartEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.NonNull;

public class MinecartChestMenu extends ChestMenu {
    private final NonInventoryWorkingBlockMinecartEntity entity;
    public MinecartChestMenu(MenuType<?> menuType, int containerId, Inventory inventory, Container container, int rows, NonInventoryWorkingBlockMinecartEntity entity) {
        super(menuType, containerId, inventory, container, rows);
        this.entity = entity;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            this.entity.onContainerClosed();
        }
    }
}
