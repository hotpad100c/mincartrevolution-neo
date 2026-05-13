package ml.mypals.minecartrevolution.entity.minecarts;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class WoolMinecartEntity extends VariantBlockMinecartEntity {
    public WoolMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }
    public WoolMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world, Item item) {
        super(entityType, world, item);
        this.setNoGravity(true);
    }
    public WoolMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
        this.setNoGravity(true);
    }
    @Override
    public void tick() {
        super.tick();
    }
}
