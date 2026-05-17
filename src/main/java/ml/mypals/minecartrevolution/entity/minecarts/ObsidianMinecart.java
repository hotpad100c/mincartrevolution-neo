package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

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
        if (this.isInLava()) {
            setOnGround(false);
            this.addDeltaMovement(new Vec3(0,0.05,0));
        }
        super.tick();
    }

    @Override
    protected @NonNull Vec3 applyNaturalSlowdown(@NonNull Vec3 movement) {
        if(!this.isInLava()) return super.applyNaturalSlowdown(movement);

        return movement;
    }
    @Override
    public boolean fireImmune() { return true; }
}