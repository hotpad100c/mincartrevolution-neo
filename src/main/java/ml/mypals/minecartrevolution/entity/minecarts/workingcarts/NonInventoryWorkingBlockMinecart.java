package ml.mypals.minecartrevolution.entity.minecarts.workingcarts;

import ml.mypals.minecartrevolution.entity.minecarts.HasVariantRegularBlockMinecartEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class NonInventoryWorkingBlockMinecart extends HasVariantRegularBlockMinecartEntity {
    public NonInventoryWorkingBlockMinecart(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public NonInventoryWorkingBlockMinecart(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Block blockInside) {
        super(minecart, world, x, y, z, blockInside);
    }
    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        if(!player.isSecondaryUseActive()){

            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand, pos);
    }
}
