package ml.mypals.minecartrevolution.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class MinecartRotationUtils {
    /**
     * Calculates the world-space direction of a block property inside a minecart,
     * accounting for the vanilla AbstractMinecartRenderer's rotations.
     * 
     * @param localDirection The direction property of the block state (relative to minecart)
     * @param minecartYaw The current yaw of the minecart
     * @return The world-space Direction
     */
    public static Direction getAbsoluteDirection(Direction localDirection, float minecartYaw) {
        if (localDirection == Direction.UP || localDirection == Direction.DOWN) {
            return localDirection;
        }
        
        // Vanilla AbstractMinecartRenderer applies:
        // 1. RotationY(180 - yaw)
        // 2. RotationY(90)
        // Total rotation = (180 - yaw + 90) = (270 - yaw)
        
        float localYaw = localDirection.toYRot();
        float worldYaw = Mth.wrapDegrees(localYaw + 270.0F - minecartYaw);
        return Direction.fromYRot(worldYaw);
    }

    /**
     * Calculates the required block-space direction to achieve a desired world-space direction.
     * 
     * @param worldDirection The desired world-space direction
     * @param minecartYaw The current yaw of the minecart
     * @return The local block Direction property
     */
    public static Direction getRelativeDirection(Direction worldDirection, float minecartYaw) {
        if (worldDirection == Direction.UP || worldDirection == Direction.DOWN) {
            return worldDirection;
        }
        
        float worldYaw = worldDirection.toYRot();
        // worldYaw = localYaw + 270 - yaw
        // localYaw = worldYaw - 270 + yaw
        float localYaw = Mth.wrapDegrees(worldYaw - 270.0F + minecartYaw);
        return Direction.fromYRot(localYaw);
    }
}
