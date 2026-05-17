package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import ml.mypals.minecartrevolution.mixin.entity.FallingBlockEntityAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PortalFallingBlockEntity extends FallingBlockEntity {

    private static final double DRIFT_SPEED = 0.01;

    private Vec3 targetPos;

    public PortalFallingBlockEntity(Level level, double x, double y, double z,
                                    BlockState blockState, Vec3 targetPos) {
        super(EntityType.FALLING_BLOCK, level);
        this.setPos(x, y, z);
        ((FallingBlockEntityAccessor) this).mincartrevolution_neo$setBlockState(blockState);
        this.targetPos = targetPos;

        this.noPhysics = true;
        this.setNoGravity(true);
        this.time = 1;
    }

    @Override
    public void tick() {

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (targetPos == null) return;

        Vec3 current = this.position();
        Vec3 delta   = targetPos.subtract(current);
        double dist  = delta.length();


        Vec3 step = delta.normalize().scale(Math.min(DRIFT_SPEED, dist));
        this.setPos(current.x + step.x, current.y + step.y, current.z + step.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void setTargetPos(Vec3 targetPos) {
        this.targetPos = targetPos;
    }
}
