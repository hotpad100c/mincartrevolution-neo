package ml.mypals.minecartrevolution.entity.minecarts;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class WoolMinecartEntity extends VariantBlockMinecartEntity {
    public WoolMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }
    public WoolMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world, Item item) {
        super(entityType, world, item);
    }
    public WoolMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
        setCustomDisplayBlockState(Optional.of(Blocks.WHITE_WOOL.defaultBlockState()));
    }
    @Override
    public void tick() {
        setOnGround(false);
        super.tick();
    }
    @Override
    protected @NonNull Vec3 applyNaturalSlowdown(@NonNull Vec3 movement) {
        if (this.isInWater()) {
            movement = movement.scale(0.95F);
        }

        return movement;
    }
    protected double getDefaultGravity() {
        return 0.001;
    }
}
