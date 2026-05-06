package ml.mypals.minecartrevolution.client.menu;

import ml.mypals.minecartrevolution.entity.minecarts.workingcarts.NonInventoryWorkingBlockMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.NonNull;

public class MinecartChestMenu extends ChestMenu {
    private final IMinecartContainer entity;

    public MinecartChestMenu(MenuType<?> menuType, int containerId, Inventory inventory, Container container, int rows, IMinecartContainer entity) {
        super(menuType, containerId, inventory, container, rows);
        this.entity = entity;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        this.entity.minecartrevolution$OnContainerClosed(player.level(), player);
    }

}
