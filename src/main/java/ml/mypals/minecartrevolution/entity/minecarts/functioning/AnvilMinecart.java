package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import ml.mypals.minecartrevolution.MinecartRevolution;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class AnvilMinecart extends NonInventoryWorkingBlockMinecartEntity{
    public AnvilMinecart(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
        this.mass = 7D;
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.ANVIL.defaultBlockState();
    }

    public AnvilMinecart(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
    }

//    @Override
//    public void tick() {
//        super.tick();
//
//        Vec3 movement = this.getDeltaMovement();
//        if (this.level().isClientSide() || movement.length() < 0.02D) return;
//        Vec3 pos = this.position();
//        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().expandTowards(movement.scale(0.8D)))) {
//            if (entity == this.getControllingPassenger() || !(entity instanceof LivingEntity)) continue;
//            Vec3 ePos = entity.position();
//            Vec3 tmp = ePos.subtract(pos);
//            if (tmp.dot(movement) > 0) {
//                entity.hurtServer((ServerLevel)this.level(), entity.damageSources().anvil(this), 2F + (float)movement.length() * 10F);
//                entity.setDeltaMovement(movement.multiply(1D, 1.2D, 1D));
//            }
//        }
//    }
}
