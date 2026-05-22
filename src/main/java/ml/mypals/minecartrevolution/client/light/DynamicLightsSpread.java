package ml.mypals.minecartrevolution.client.light;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class DynamicLightsSpread {

    public static final int RADIUS = 6;
    public static final double FACTOR = 0.18;

    private DynamicLightsSpread() {
    }

    public static void markAreaDirty(BlockPos pos, int radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.levelRenderer == null) return;
        
        int minX = SectionPos.blockToSectionCoord(pos.getX() - radius);
        int minY = SectionPos.blockToSectionCoord(pos.getY() - radius);
        int minZ = SectionPos.blockToSectionCoord(pos.getZ() - radius);

        int maxX = SectionPos.blockToSectionCoord(pos.getX() + radius);
        int maxY = SectionPos.blockToSectionCoord(pos.getY() + radius);
        int maxZ = SectionPos.blockToSectionCoord(pos.getZ() + radius);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mc.levelRenderer.setSectionDirty(x, y, z);
                }
            }
        }
    }
}