package ml.mypals.minecartrevolution.inventory;

import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LinkedContainer extends PlayerEnderChestContainer {
    private final String key;

    private final Set<AbstractMinecart> activeMinecarts = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public LinkedContainer(String key) {
        this.key = key;
    }

    public void setActiveMinecart(AbstractMinecart minecart) {
        this.activeMinecarts.add(minecart);
    }

    @Override
    public void startOpen(@NonNull ContainerUser user) {
    }

    @Override
    public void stopOpen(@NonNull ContainerUser user) {
        activeMinecarts.removeIf(cart -> !cart.isAlive());
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }

    @Override
    public void setChanged() {
        super.setChanged();
        activeMinecarts.removeIf(cart -> {
            if (!cart.isAlive()) return true;

            Level level = cart.level();
            if (!level.isClientSide()) {
                level.updateNeighbourForOutputSignal(cart.blockPosition(), level.getBlockState(cart.blockPosition()).getBlock());
            }
            return false;
        });
    }
}