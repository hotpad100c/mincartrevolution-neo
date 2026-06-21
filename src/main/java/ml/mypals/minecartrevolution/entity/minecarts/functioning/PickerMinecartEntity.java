package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import java.util.Optional;
import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class PickerMinecartEntity extends Minecart {

  public PickerMinecartEntity(EntityType<?> entityType, Level level) {
    super(entityType, level);
  }

  public PickerMinecartEntity(Level level, double x, double y, double z) {
    super(MRMinecarts.PICKER_MINECART.entity().get(), level);
    this.setPos(x, y, z);
    this.xo = x;
    this.yo = y;
    this.zo = z;
  }

  @Override
  protected @NonNull Item getDropItem() {
    return MRMinecarts.PICKER_MINECART.item().get();
  }

  @Override
  public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {
    if (powered) {
      BlockPos above = this.blockPosition().above();
      BlockState state = level.getBlockState(above);
      if (!state.isAir() && state.getDestroySpeed(level, above) >= 0) {
        CompoundTag blockEntityTag = null;
        BlockEntity be = level.getBlockEntity(above);
        if (be != null) {
          blockEntityTag = be.saveWithFullMetadata(level.registryAccess());
          blockEntityTag.remove("x");
          blockEntityTag.remove("y");
          blockEntityTag.remove("z");
        }

        Block block = state.getBlock();

        AbstractMinecart result =
            MinecartTransformManager.checkForTransform(
                level, position(), block, this, ItemStack.EMPTY);
        level.destroyBlock(above, false);
        result.setCustomDisplayBlockState(Optional.of(state));

        if (blockEntityTag != null && result instanceof CompatFriendlyBlockMinecartEntity compat) {
          compat.setBlockEntityTag(blockEntityTag);
        }
      }
    }
  }
}
