package ml.mypals.minecartrevolution.entity.minecarts;

import java.util.Optional;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class ScaffoldMinecartEntity extends SingleBlockMinecartEntity {

  public static final int SLOT_COUNT = 8;

  private static final EntityDataAccessor<ItemStack>[] SLOTS;

  static {
    SLOTS = new EntityDataAccessor[SLOT_COUNT];
    for (int i = 0; i < SLOT_COUNT; i++) {
      SLOTS[i] =
          SynchedEntityData.defineId(
              ScaffoldMinecartEntity.class, EntityDataSerializers.ITEM_STACK);
    }
  }

  public ScaffoldMinecartEntity(EntityType<ScaffoldMinecartEntity> entityType, Level world) {
    super(entityType, world);
  }

  public ScaffoldMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      MinecartWithBlockItem correspondingItem) {
    super(minecart, world, x, y, z, correspondingItem);
    setCustomDisplayBlockState(
        Optional.of(
            Blocks.SCAFFOLDING
                .defaultBlockState()
                .setValue(ScaffoldingBlock.BOTTOM, true)
                .setValue(ScaffoldingBlock.DISTANCE, 1)));
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.SCAFFOLDING
        .defaultBlockState()
        .setValue(ScaffoldingBlock.BOTTOM, true)
        .setValue(ScaffoldingBlock.DISTANCE, 1);
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
    super.defineSynchedData(builder);
    for (int i = 0; i < SLOT_COUNT; i++) {
      builder.define(SLOTS[i], ItemStack.EMPTY);
    }
  }

  public ItemStack getStoredItem(int slot) {
    if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
    return entityData.get(SLOTS[slot]);
  }

  public void setStoredItem(int slot, ItemStack stack) {
    if (slot < 0 || slot >= SLOT_COUNT) return;
    entityData.set(SLOTS[slot], stack.copy());
  }

  private int firstEmptySlot() {
    for (int i = 0; i < SLOT_COUNT; i++) {
      if (getStoredItem(i).isEmpty()) return i;
    }
    return -1;
  }

  private int lastFilledSlot() {
    for (int i = SLOT_COUNT - 1; i >= 0; i--) {
      if (!getStoredItem(i).isEmpty()) return i;
    }
    return -1;
  }

  @Override
  public @NonNull InteractionResult interact(
      @NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
    if (player.isSecondaryUseActive()) {
      return super.interact(player, hand, pos);
    }

    ItemStack held = player.getItemInHand(hand);

    if (!held.isEmpty()) {
      if (!level().isClientSide()) {
        int emptySlot = firstEmptySlot();
        if (emptySlot == -1) {

          return InteractionResult.FAIL;
        }
        int toTake = Math.min(held.getCount(), held.getMaxStackSize());
        ItemStack stored = held.copyWithCount(toTake);
        setStoredItem(emptySlot, stored);
        held.shrink(toTake);
        player.setItemInHand(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        playSound(SoundEvents.ITEM_PICKUP, 0.8f, 1.0f + (float) (Math.random() * 0.1));
      }
      player.swing(hand);
      return InteractionResult.SUCCESS;
    } else {
      if (!level().isClientSide()) {
        int filledSlot = lastFilledSlot();
        if (filledSlot == -1) {
          return InteractionResult.PASS;
        }
        ItemStack extracted = getStoredItem(filledSlot).copy();
        setStoredItem(filledSlot, ItemStack.EMPTY);
        if (!player.addItem(extracted)) {
          Containers.dropItemStack(level(), getX(), getY() + 0.5, getZ(), extracted);
        }
        playSound(SoundEvents.ITEM_PICKUP, 0.8f, 1.2f + (float) (Math.random() * 0.1));
      }
      player.swing(hand);
      return InteractionResult.SUCCESS;
    }
  }

  @Override
  public void destroy(@NonNull ServerLevel serverLevel, DamageSource source) {
    super.destroy(serverLevel, source);
    for (int i = 0; i < SLOT_COUNT; i++) {
      ItemStack stack = getStoredItem(i);
      if (!stack.isEmpty()) {
        Containers.dropItemStack(serverLevel, getX(), getY() + 0.5, getZ(), stack);
      }
    }
  }

  @Override
  protected void addAdditionalSaveData(@NonNull ValueOutput compound) {
    super.addAdditionalSaveData(compound);
    for (int i = 0; i < SLOT_COUNT; i++) {
      ItemStack stack = getStoredItem(i);
      if (!stack.isEmpty()) {
        compound.store("ScaffoldSlot" + i, ItemStack.CODEC, stack);
      }
    }
  }

  @Override
  protected void readAdditionalSaveData(@NonNull ValueInput compound) {
    super.readAdditionalSaveData(compound);
    for (int i = 0; i < SLOT_COUNT; i++) {
      Optional<ItemStack> stack = compound.read("ScaffoldSlot" + i, ItemStack.CODEC);
      setStoredItem(i, stack.orElse(ItemStack.EMPTY));
    }
  }

  @Override
  public @NonNull ItemStack getPickResult() {
    return MRMinecarts.SCAFFOLD_MINECART.item().get().getDefaultInstance();
  }

  @Override
  public @NonNull Item getDropItem() {
    return MRMinecarts.SCAFFOLD_MINECART.item().get();
  }
}
