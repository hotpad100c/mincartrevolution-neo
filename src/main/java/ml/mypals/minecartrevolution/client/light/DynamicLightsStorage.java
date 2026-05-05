package ml.mypals.minecartrevolution.client.light;


import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicLightsStorage {
    public static final ThreadLocal<BlockPos.MutableBlockPos> blockPointer = new ThreadLocal<>() {{
        set(new BlockPos.MutableBlockPos());
    }};

    public static final Map<Long, Double> BP_TO_LIGHT_LEVEL = new ConcurrentHashMap<>();

    public static double getLightLevel(BlockPos pos) {
        return BP_TO_LIGHT_LEVEL.getOrDefault(pos.asLong(), 0.0);
    }

    public static void removeLight(long posLong) {
        BP_TO_LIGHT_LEVEL.remove(posLong);
    }

    public static void clear() {
        BP_TO_LIGHT_LEVEL.clear();
    }
}