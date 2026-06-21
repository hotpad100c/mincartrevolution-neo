package ml.mypals.minecartrevolution.mixin.blocks;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonStructureResolver.class)
public class PistonStructureResolverMixin {

  @Shadow @Final private Level level;
  @Shadow @Final private Direction pushDirection;
  @Shadow @Final private List<BlockPos> toPush;

  @Inject(method = "resolve", at = @At("RETURN"))
  private void onResolveReturn(CallbackInfoReturnable<Boolean> cir) {
    if (!cir.getReturnValue()) return;

    Iterator<BlockPos> it = toPush.iterator();
    while (it.hasNext()) {
      BlockPos pos = it.next();
      BlockPos dest = pos.relative(pushDirection);

      List<AbstractMinecart> minecarts =
          level.getEntitiesOfClass(AbstractMinecart.class, new AABB(dest));
      AbstractMinecart targetMinecart = null;
      for (AbstractMinecart m : minecarts) {
        if (m.getDisplayBlockState().isAir()) {
          targetMinecart = m;
          break;
        }
      }

      if (targetMinecart != null) {
        BlockState state = level.getBlockState(pos);

        CompoundTag blockEntityTag = null;
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
          blockEntityTag = be.saveWithFullMetadata(level.registryAccess());
          blockEntityTag.remove("x");
          blockEntityTag.remove("y");
          blockEntityTag.remove("z");
        }

        AbstractMinecart result =
            MinecartTransformManager.checkForTransform(
                level,
                targetMinecart.position(),
                state.getBlock(),
                targetMinecart,
                net.minecraft.world.item.ItemStack.EMPTY);

        level.destroyBlock(pos, false);
        result.setCustomDisplayBlockState(Optional.of(state));
        if (blockEntityTag != null && result instanceof CompatFriendlyBlockMinecartEntity compat) {
          compat.setBlockEntityTag(blockEntityTag);
        }

        it.remove();
      }
    }
  }
}
