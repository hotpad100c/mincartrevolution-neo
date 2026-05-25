package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ObsidianMinecartEntity extends SingleBlockMinecartEntity {

    public ObsidianMinecartEntity(EntityType<ObsidianMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.OBSIDIAN.defaultBlockState();
    }

    public ObsidianMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        setCustomDisplayBlockState(Optional.of(Blocks.OBSIDIAN.defaultBlockState()));
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

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.OBSIDIAN_MINECART.item().get().getDefaultInstance();
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.OBSIDIAN_MINECART.item().get();
    }
}