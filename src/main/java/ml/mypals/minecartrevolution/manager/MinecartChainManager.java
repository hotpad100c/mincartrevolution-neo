package ml.mypals.minecartrevolution.manager;

import ml.mypals.minecartrevolution.entity.chain.ChainEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Supplier;

public class MinecartChainManager {

    private static final Map<UUID, UUID> playerSelections = new HashMap<>();
    private static final Map<UUID, Long> selectionTimestamps = new HashMap<>();
    private static final long SELECTION_TIMEOUT_MS = 10000;

    public static Supplier<EntityType<ChainEntity>> chainEntityTypeSupplier;

    public static void startSelection(Player player, AbstractMinecart minecart) {
        UUID playerId = player.getUUID();
        UUID cartId = minecart.getUUID();

        UUID existing = playerSelections.get(playerId);
        if (existing != null && existing.equals(cartId)) {
            playerSelections.remove(playerId);
            selectionTimestamps.remove(playerId);
            return;
        }

        playerSelections.put(playerId, cartId);
        selectionTimestamps.put(playerId, System.currentTimeMillis());
    }

    public static UUID getSelectedMinecart(Player player) {
        UUID playerId = player.getUUID();
        Long timestamp = selectionTimestamps.get(playerId);
        if (timestamp == null) return null;
        if (System.currentTimeMillis() - timestamp > SELECTION_TIMEOUT_MS) {
            playerSelections.remove(playerId);
            selectionTimestamps.remove(playerId);
            return null;
        }
        return playerSelections.get(playerId);
    }

    public static void clearSelection(Player player) {
        playerSelections.remove(player.getUUID());
        selectionTimestamps.remove(player.getUUID());
    }

    public static boolean createChain(ServerLevel level, AbstractMinecart cartA, AbstractMinecart cartB) {
        if (cartA == null || cartB == null || cartA == cartB) return false;
        if (!cartA.isAlive() || !cartB.isAlive()) return false;
        if (areMinecartsConnected(cartA, cartB)) return false;
        if (chainEntityTypeSupplier == null) return false;

        ChainEntity chain = new ChainEntity(chainEntityTypeSupplier.get(), level, cartA.getUUID(), cartB.getUUID());
        Vec3 midpoint = cartA.position().add(cartB.position()).scale(0.5);
        chain.setPos(midpoint.x, midpoint.y, midpoint.z);
        level.addFreshEntity(chain);

        return true;
    }

    public static void breakChain(ServerLevel level, AbstractMinecart minecart) {
        ChainEntity chain = findChainForMinecart(level, minecart);
        if (chain != null) {
            chain.discard();
        }
    }

    public static boolean areMinecartsConnected(AbstractMinecart cartA, AbstractMinecart cartB) {
        if (!(cartA.level() instanceof ServerLevel level)) return false;
        ChainEntity chain = findChainForMinecart(level, cartA);
        return chain != null && chain.linksMinecart(cartB.getUUID());
    }

    public static boolean isMinecartChained(ServerLevel level, AbstractMinecart minecart) {
        return findChainForMinecart(level, minecart) != null;
    }

    public static ChainEntity findChainForMinecart(ServerLevel level, AbstractMinecart minecart) {
        UUID uuid = minecart.getUUID();
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof ChainEntity chain && chain.linksMinecart(uuid)) {
                return chain;
            }
        }
        return null;
    }

    public static void onChainBroken(ChainEntity chain) {
    }

    public static void onChainRemoved(ChainEntity chain) {
    }
}