package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.client.menu.MinecartChestMenu;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Unique;

public class TrappedChestMinecartEntity extends BaseMinecartContainer
    implements PowerEmitterMinecartEntity, IMinecartContainer {
  private int openCount = 0;
  @Unique public final ChestLidController chestLidController = new ChestLidController();

  public TrappedChestMinecartEntity(
      EntityType<? extends AbstractMinecartContainer> entityType, Level world) {
    super(entityType, world);
  }

  public TrappedChestMinecartEntity(Level world, double x, double y, double z) {
    super(MRMinecarts.TRAPPED_CHEST_MINECART.entity().get(), world);
    this.setInitialPos(x, y, z);
  }

  @Override
  public @NonNull ItemStack getPickResult() {
    return MRMinecarts.TRAPPED_CHEST_MINECART.item().get().getDefaultInstance();
  }

  @Override
  public @NonNull Item getDropItem() {
    return MRMinecarts.TRAPPED_CHEST_MINECART.item().get();
  }

  @Override
  public int getContainerSize() {
    return 27;
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.TRAPPED_CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
  }

  @Override
  public int getDefaultDisplayOffset() {
    return 8;
  }

  @Override
  public @NonNull AbstractContainerMenu createMenu(int syncId, @NonNull Inventory playerInventory) {
    return new MinecartChestMenu(MenuType.GENERIC_9x3, syncId, playerInventory, this, 3, this);
  }

  @Override
  public void tick() {
    this.entityData
        .get(DATA_ID_CUSTOM_DISPLAY_BLOCK)
        .ifPresent(
            blockState -> {
              if (!updatedBlocks || !this.isAlive()) {
                if (getPreviousBlockPos() != null)
                  updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
                updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
              }
              if (this.getPreviousBlockPos() == null
                  || !this.getPreviousBlockPos().equals(this.blockPosition())) {
                if (this.getPreviousBlockPos() == null)
                  this.setPreviousBlockPos(this.blockPosition());
                updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
                this.setPreviousBlockPos(this.blockPosition());
                updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
              }
            });
    this.chestLidController.shouldBeOpen(this.openCount > 0);
    this.chestLidController.tickLid();
    super.tick();
  }

  public float getOpenness(float partialTick) {
    return this.chestLidController.getOpenness(partialTick);
  }

  @Override
  public @NonNull InteractionResult interactWithContainerVehicle(@NonNull Player player) {
    if (!this.level().isClientSide()) {
      this.openCount++;
      this.level().broadcastEntityEvent(this, (byte) 10);
    }
    return super.interactWithContainerVehicle(player);
  }

  @Override
  public int getPowerStrength(Direction direction, BlockPos pos) {
    return direction == Direction.UP ? 0 : Mth.clamp(openCount, 0, 15);
  }

  @Override
  public @NonNull InteractionResult interact(
      @NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
    if (!player.isSecondaryUseActive()) {
      InteractionResult actionResult = this.interactWithContainerVehicle(player);
      if (actionResult.consumesAction()) {
        this.openCount++;
        if (this.openCount >= 1) {
          this.level()
              .playSound(this, this.blockPosition(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS);
        }
        this.level().broadcastEntityEvent(this, (byte) 11);
        this.gameEvent(GameEvent.CONTAINER_OPEN, player);
        if (player.level() instanceof ServerLevel serverLevel)
          PiglinAi.angerNearbyPiglins(serverLevel, player, true);
        this.entityData
            .get(DATA_ID_CUSTOM_DISPLAY_BLOCK)
            .ifPresent(
                blockState -> {
                  updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
                });
      }
      return actionResult;
    }
    return super.interact(player, hand, pos);
  }

  @Override
  public void handleEntityEvent(byte id) {
    if (id == 10) {
      this.openCount++;
    } else if (id == 11) {
      this.openCount = Math.max(0, this.openCount - 1);
    } else {
      super.handleEntityEvent(id);
    }
  }

  @Override
  public void minecartrevolution$OnContainerClosed(Level level, Player player) {
    this.openCount = Math.max(0, this.openCount - 1);
    if (this.openCount == 0) {
      this.level()
          .playSound(this, this.blockPosition(), SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS);
    }
    this.level().broadcastEntityEvent(this, (byte) 11);
    updateNeighbors(this.level(), this.blockPosition(), Blocks.TRAPPED_CHEST);
  }
}
