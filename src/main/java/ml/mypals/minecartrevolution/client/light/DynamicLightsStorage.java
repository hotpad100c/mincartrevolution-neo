package ml.mypals.minecartrevolution.client.light;


import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicLightsStorage {
    public static final ThreadLocal<BlockPos.MutableBlockPos> blockPointer = new ThreadLocal<>() {{
        set(new BlockPos.MutableBlockPos());
    }};

    public static final Map<Entity, Integer> LIGHT_SOURCES = new ConcurrentHashMap<>();

    public static double getLightLevel(BlockPos pos) {
        double maxLight = 0;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (Map.Entry<Entity, Integer> entry : LIGHT_SOURCES.entrySet()) {
            Entity entity = entry.getKey();
            if (entity.isRemoved()) {
                LIGHT_SOURCES.remove(entity);
                continue;
            }
            int maxSourceLight = entry.getValue();
            if (maxSourceLight <= 0) continue;

            double dx = entity.getX() - (x + 0.5);
            double dy = entity.getY() - (y + 0.5);
            double dz = entity.getZ() - (z + 0.5);

            double distSqr = dx * dx + dy * dy + dz * dz;
            double maxDist = 1.0 / DynamicLightsSpread.FACTOR;
            
            if (distSqr <= maxDist * maxDist) {
                double dist = Math.sqrt(distSqr);
                double light = maxSourceLight * (1 - dist * DynamicLightsSpread.FACTOR);
                if (light > maxLight) {
                    maxLight = light;
                }
            }
        }
        return Mth.clamp(maxLight, 0, 15);
    }

    public static void clear() {
        LIGHT_SOURCES.clear();
    }
}