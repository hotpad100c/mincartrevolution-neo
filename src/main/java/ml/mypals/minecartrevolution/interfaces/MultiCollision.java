package ml.mypals.minecartrevolution.interfaces;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public interface MultiCollision {
    List<AABB> getColliders();
}
