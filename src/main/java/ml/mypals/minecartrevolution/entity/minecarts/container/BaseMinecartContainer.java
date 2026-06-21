package ml.mypals.minecartrevolution.entity.minecarts.container;

import java.util.Optional;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public abstract class BaseMinecartContainer extends AbstractMinecartContainer {

  protected BaseMinecartContainer(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Override
  public BlockState getDisplayBlockState() {
    return this.getEntityData()
        .get(DATA_ID_CUSTOM_DISPLAY_BLOCK)
        .orElse(getDefaultDisplayBlockState());
  }

  // ── Subclasses must implement these ─────────────────────────────────────

  @Override
  protected abstract @NonNull AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory);

  @Override
  public abstract @NonNull ItemStack getPickResult();

  @Override
  protected abstract @NonNull Item getDropItem();

  @Override
  public abstract int getContainerSize();

  // ── Shared utilities ─────────────────────────────────────────────────────

  /** Returns {@code true} when a non-air block is set as the display block. */
  public boolean hasCustomDisplay() {
    BlockState blockState =
        entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
    return !(blockState.getBlock() instanceof AirBlock);
  }

  /**
   * Converts this minecart back into a plain {@link Minecart}, dropping the display block in the
   * player's hand and discarding the original entity.
   */
  protected void clearToMinecart() {
    setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
    Minecart minecartEntity = new Minecart(EntityType.MINECART, level());
    minecartEntity.restoreFrom(this);
    minecartEntity.copyPosition(this);
    minecartEntity.setDeltaMovement(this.getDeltaMovement());
    this.remove(RemovalReason.DISCARDED);
    this.level().addFreshEntity(minecartEntity);
    minecartEntity.setHurtDir(-minecartEntity.getHurtDir());
    minecartEntity.setHurtTime(10);
    minecartEntity.setDamage(50.0F);
  }

  @Override
  public @NonNull InteractionResult interact(
      @NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
    if (player.isSecondaryUseActive()) {
      if (this.hasCustomDisplay()) {
        BlockState blockState = getDisplayBlockState();
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
          Block block = blockState.getBlock();
          playSound(block.defaultBlockState().getSoundType().getBreakSound(), 1, 1);
          player.swing(hand);
          if (!this.level().isClientSide()) {
            clearToMinecart();
            player.setItemInHand(hand, block.asItem().getDefaultInstance());
          }
        }
        return InteractionResult.SUCCESS;
      }
    }
    return super.interact(player, hand, location);
  }
}
