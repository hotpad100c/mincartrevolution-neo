package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class PressurePlateMinecartEntity extends VariantBlockMinecartEntity
    implements PowerEmitterMinecartEntity {

  public PressurePlateMinecartEntity(
      EntityType<? extends PressurePlateMinecartEntity> entityType,
      Level world,
      double x,
      double y,
      double z,
      Item item) {
    super(entityType, world, x, y, z, item);
  }

  public PressurePlateMinecartEntity(
      EntityType<? extends PressurePlateMinecartEntity> presherPlateMinecartEntityEntityType,
      Level world) {
    super(presherPlateMinecartEntityEntityType, world);
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.OAK_PRESSURE_PLATE.defaultBlockState();
  }

  @Override
  public @NonNull Item getDropItem() {
    return MRMinecarts.PRESHER_PLATE_MINECART_ITEM.item().get();
  }

  @Override
  public @NonNull InteractionResult interact(
      @NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
    super.interact(player, hand, pos);
    if (this.isVehicle()) {
      return InteractionResult.PASS;
    } else if (!this.level().isClientSide()) {
      BlockState blockState =
          entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

      updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
      updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
      return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    } else {
      return InteractionResult.SUCCESS;
    }
  }

  @Override
  public void tick() {
    super.tick();
    BlockState blockState =
        entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
    if (!updatedBlocks || !this.isAlive()) {
      if (getPreviousBlockPos() != null)
        updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
      updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
    }
    if (this.getPreviousBlockPos() == null
        || !this.getPreviousBlockPos().equals(this.blockPosition())) {
      if (this.getPreviousBlockPos() == null) this.setPreviousBlockPos(this.blockPosition());
      updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
      this.setPreviousBlockPos(this.blockPosition());
      updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
    }
  }

  @Override
  public void activateMinecart(
      @NonNull ServerLevel level, int xt, int yt, int zt, boolean powered) {

    if (powered) {
      if (this.isVehicle()) {
        this.ejectPassengers();
      }

      if (this.getHurtTime() == 0) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(50.0F);
        this.markHurt();
      }
    }
  }

  @Override
  public int getPowerStrength(Direction direction, BlockPos pos) {
    if (!this.isAlive()) {
      return 0;
    }
    return this.isVehicle() ? 15 : 0;
  }
}
