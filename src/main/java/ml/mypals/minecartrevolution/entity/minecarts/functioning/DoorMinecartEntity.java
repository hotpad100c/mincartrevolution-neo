package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import java.util.Optional;
import ml.mypals.minecartrevolution.entity.minecarts.SingleBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class DoorMinecartEntity extends SingleBlockMinecartEntity {

  public DoorMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
    super(entityType, world);
    this.setCustomDisplayBlockState(Optional.of(getDefaultDisplayBlockState()));
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.IRON_DOOR.defaultBlockState();
  }

  @Override
  public int getDefaultDisplayOffset() {
    return 10;
  }

  public DoorMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      MinecartWithBlockItem correspondingItem) {
    super(minecart, world, x, y, z, correspondingItem);
    this.setCustomDisplayBlockState(
        Optional.of(correspondingItem.getBlockInside().defaultBlockState()));
  }

  private boolean isIronDoor() {
    return getDisplayBlockState().getBlock() == Blocks.IRON_DOOR;
  }

  @Override
  public void handleActive(ServerLevel level, int x, int y, int z, boolean powered) {
    BlockState blockState = getDisplayBlockState();
    boolean currentPowered = blockState.getValue(BlockStateProperties.POWERED);

    if (powered != currentPowered) {
      BlockState newState =
          blockState
              .setValue(BlockStateProperties.OPEN, powered)
              .setValue(BlockStateProperties.POWERED, powered);
      this.setCustomDisplayBlockState(Optional.of(newState));
      this.activated = powered;
      SoundEvent sound =
          powered
              ? (isIronDoor() ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN)
              : (isIronDoor() ? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_CLOSE);
      level.playSound(
          null,
          this.blockPosition(),
          sound,
          SoundSource.BLOCKS,
          1.0F,
          level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
  }

  @Override
  public @NonNull InteractionResult interact(
      @NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
    if (player.isShiftKeyDown() || isIronDoor()) {
      return super.interact(player, hand, pos);
    }

    BlockState blockState = getDisplayBlockState();
    boolean currentlyOpen = blockState.getValue(BlockStateProperties.OPEN);
    boolean newOpen = !currentlyOpen;

    BlockState newState = blockState.setValue(BlockStateProperties.OPEN, newOpen);
    this.setCustomDisplayBlockState(Optional.of(newState));

    SoundEvent sound =
        newOpen
            ? (isIronDoor() ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN)
            : (isIronDoor() ? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_CLOSE);
    this.level()
        .playSound(
            null,
            this.blockPosition(),
            sound,
            SoundSource.BLOCKS,
            1.0F,
            this.level().getRandom().nextFloat() * 0.1F + 0.9F);

    return InteractionResult.SUCCESS;
  }
}
