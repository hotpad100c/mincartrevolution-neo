package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;

public class CloudMinecartEntity extends FluidMinecartEntity{
    public CloudMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public CloudMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
    }
    public void tick(){
        super.tick();
        if(!(level().getBlockState(blockPosition()).getBlock() instanceof RailBlock)){
            this.setDeltaMovement(this.getDeltaMovement().add(0,0.1,0));
        }
    }
}
