package ml.mypals.minecartrevolution.client.light;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class DynamicLightsSpread {

    public static final int RADIUS = 6;
    public static final double FACTOR = 0.18;

    private DynamicLightsSpread() {}

    public static void computeDynamicLights(long origin, double originX, double originY, double originZ,
                                            double maxLight, Predicate<Long> hasLight, Consumer<Long> markChanged) {

        final int x = BlockPos.getX(origin);
        final int y = BlockPos.getY(origin);
        final int z = BlockPos.getZ(origin);

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS / 2; dy <= RADIUS / 2; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    int blockX = x + dx;
                    int blockY = y + dy;
                    int blockZ = z + dz;

                    double dist = Math.hypot(
                            originX - (blockX + 0.5),
                            Math.hypot(originY - (blockY + 0.5), originZ - (blockZ + 0.5))
                    );

                    double lightLevel = Mth.clamp(maxLight * (1 - dist * FACTOR), 0, 15);

                    long bpLong = BlockPos.asLong(blockX, blockY, blockZ);

                    markChanged.accept(bpLong);

                    if (hasLight.test(bpLong)) {
                        DynamicLightsStorage.BP_TO_LIGHT_LEVEL.merge(bpLong, lightLevel, Math::max);
                    } else {
                        DynamicLightsStorage.BP_TO_LIGHT_LEVEL.put(bpLong, lightLevel);
                    }
                }
            }
        }
    }


    public static void computeLightsOff(long origin, Predicate<Long> hasLight, Consumer<Long> markChanged) {

        final int x = BlockPos.getX(origin);
        final int y = BlockPos.getY(origin);
        final int z = BlockPos.getZ(origin);

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS / 2; dy <= RADIUS / 2; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    long bpLong = BlockPos.asLong(x + dx, y + dy, z + dz);

                    if (!hasLight.test(bpLong)) continue;

                    DynamicLightsStorage.BP_TO_LIGHT_LEVEL.remove(bpLong);
                    markChanged.accept(bpLong);
                }
            }
        }
    }

    public static void clearFromCenter(long origin) {
        final int x = BlockPos.getX(origin);
        final int y = BlockPos.getY(origin);
        final int z = BlockPos.getZ(origin);

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS / 2; dy <= RADIUS / 2; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    DynamicLightsStorage.BP_TO_LIGHT_LEVEL.remove(
                            BlockPos.asLong(x + dx, y + dy, z + dz)
                    );
                }
            }
        }
    }
}