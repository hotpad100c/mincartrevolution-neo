package ml.mypals.minecartrevolution.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.EnderPortalMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.NetherPortalMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(EnderEyeItem.class)
public class EndPortalCreationMixin {

    @WrapOperation(method = "useOn", at = @At(ordinal = 1, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", value = "INVOKE"))
    public boolean createPortalBlocks(Level level, BlockPos pos, BlockState blockState, int updateFlags, Operation<Boolean> original, @Local(argsOnly = true) net.minecraft.world.item.context.UseOnContext context) {
        List<Minecart> minecarts = level.getEntitiesOfClass(Minecart.class, AABB.of(BoundingBox.fromCorners(pos, pos)));
        if (!minecarts.isEmpty()) {
            Minecart minecart = minecarts.getFirst();
            minecart.remove(Entity.RemovalReason.DISCARDED);
            EnderPortalMinecartEntity enderPortalMinecart = new EnderPortalMinecartEntity(MRMinecarts.ENDER_PORTAL_MINECART.entity().get(), minecart.level(), minecart.getX(),minecart.getY(),minecart.getZ(),MRMinecarts.ENDER_PORTAL_MINECART.item().asItem());
            level.addFreshEntity(enderPortalMinecart);
            if (context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                ml.mypals.minecartrevolution.registeries.MRModCriteria.BLOCK_CART_CRAFTED.get().trigger(serverPlayer, enderPortalMinecart);
            }
            return level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return original.call(level, pos, blockState,updateFlags);

    }
}
