package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ObsidianMinecart extends SingleBlockMinecartEntity {

    public ObsidianMinecart(EntityType<ObsidianMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public ObsidianMinecart(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.isInLava()) {
            Vec3 motion = this.getDeltaMovement();
            this.setNoGravity(true);
            this.setDeltaMovement(motion.x, 0, motion.z);
        }
        else
        {
            this.setNoGravity(false);
        }
    }

    @Override
    public boolean fireImmune() { return true; }
}