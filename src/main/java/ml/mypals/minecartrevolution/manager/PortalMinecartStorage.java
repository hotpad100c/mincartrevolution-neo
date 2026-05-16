package ml.mypals.minecartrevolution.manager;

import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.PortalMinecartEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public final class PortalMinecartStorage {

    private final Set<UUID> carts = new HashSet<>();
    public void add(PortalMinecartEntity entity) {
        carts.add(entity.getUUID());
    }

    public void remove(PortalMinecartEntity entity) {
        carts.remove(entity.getUUID());
    }

    public List<PortalMinecartEntity> getAll(Level level) {
        List<PortalMinecartEntity> result = new ArrayList<>();

        Iterator<UUID> it = carts.iterator();

        while (it.hasNext()) {
            UUID uuid = it.next();

            Entity entity = level.getEntity(uuid);

            if (entity instanceof PortalMinecartEntity cart) {
                result.add(cart);
            } else {
                it.remove();
            }
        }

        return result;
    }
    @Nullable
    public <T extends PortalMinecartEntity> T getClosest(
            Entity except,
            Level level,
            Vec3 pos,
            Class<T> clazz,
            @Nullable String name
    ) {
        T closest = null;
        double closestDistance = Double.MAX_VALUE;

        Iterator<UUID> it = carts.iterator();

        while (it.hasNext()) {
            UUID uuid = it.next();

            Entity entity = level.getEntity(uuid);

            if (!(entity instanceof PortalMinecartEntity portal)) {
                it.remove();
                continue;
            }
            if(uuid.equals(except.getUUID())){
                continue;
            }
            if (!clazz.equals(portal.getClass())) {
                continue;
            }

            if (name != null) {
                if (!portal.hasCustomName()) {
                    continue;
                }

                if (!name.equals(portal.getName().getString())) {
                    continue;
                }
            }

            double distance = portal.distanceToSqr(pos);

            if (distance < closestDistance) {
                closestDistance = distance;
                closest = clazz.cast(portal);
            }
        }

        return closest;
    }
}