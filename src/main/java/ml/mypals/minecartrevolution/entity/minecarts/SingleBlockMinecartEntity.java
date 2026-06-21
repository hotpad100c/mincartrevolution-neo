package ml.mypals.minecartrevolution.entity.minecarts;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

public class SingleBlockMinecartEntity extends VariantBlockMinecartEntity {

  private static final Logger LOGGER = LogUtils.getLogger();
  protected static final EntityDataAccessor<Integer> CORRESPONDING_ITEM =
      SynchedEntityData.defineId(SingleBlockMinecartEntity.class, EntityDataSerializers.INT);
  private Item correspondingItem;

  public SingleBlockMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
    super(entityType, world);
    this.correspondingItem = MRMinecarts.BLOCK_MINECART_ITEM.item().get();
  }

  public SingleBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      MinecartWithBlockItem correspondingItem) {
    super(minecart, world, x, y, z, Item.byBlock(correspondingItem.getBlockInside()));
    this.correspondingItem = correspondingItem;
    this.getEntityData().set(CORRESPONDING_ITEM, Item.getId(correspondingItem));
    this.setCustomDisplayBlockState(
        Optional.of(correspondingItem.getBlockInside().defaultBlockState()));
  }

  public SingleBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> entityType,
      Level world,
      MinecartWithBlockItem correspondingItem) {
    super(entityType, world);
    this.correspondingItem = correspondingItem;
    this.getEntityData().set(CORRESPONDING_ITEM, Item.getId(correspondingItem));
    this.setCustomDisplayBlockState(
        Optional.of(correspondingItem.getBlockInside().defaultBlockState()));
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
    super.defineSynchedData(builder);
    builder.define(CORRESPONDING_ITEM, Item.getId(correspondingItem));
  }

  @Override
  public void destroy(@NonNull ServerLevel serverLevel, DamageSource source) {
    boolean sourceIsPlayer = false;
    Player playerEntity = null;
    if (source.getEntity() instanceof Player player) {
      sourceIsPlayer = true;
      playerEntity = player;
    }
    boolean shouldDrop =
        serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)
            || (sourceIsPlayer && !((Player) source.getEntity()).isCreative());
    if (shouldDrop) {
      if (playerEntity != null) {
        if (playerEntity.isSecondaryUseActive()) {
          ItemStack stack = getDropItem().getDefaultInstance();
          spawnAtLocation(serverLevel, stack);
        } else {
          ItemStack stack = Items.MINECART.getDefaultInstance();
          spawnAtLocation(serverLevel, stack);
          BlockState blockState = getDisplayBlockState();
          ItemStack stack2 = blockState.getBlock().asItem().getDefaultInstance();
          Containers.dropItemStack(this.level(), this.getX(), this.getY(), this.getZ(), stack2);
        }
        this.remove(Entity.RemovalReason.KILLED);
      }
    }
    this.kill(serverLevel);
  }

  @Override
  public @NonNull Item getDropItem() {
    if (this.correspondingItem != null
        && !(this.correspondingItem.getDefaultInstance().isEmpty())) {
      return this.correspondingItem;
    } else {
      return this.getCorrespondingItem();
    }
  }

  /** Recovers the corresponding item from saved NBT when the in-memory field is null/air. */
  public Item getCorrespondingItem() {
    ProblemReporter.ScopedCollector reporter =
        new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER);
    TagValueOutput valueOutput = TagValueOutput.createWithContext(reporter, this.registryAccess());

    this.saveWithoutId(valueOutput);
    CompoundTag nbt = valueOutput.buildResult();
    this.correspondingItem = Item.byId(nbt.getIntOr("correspondingItem", 0));
    return this.correspondingItem;
  }

  @Override
  public @NonNull ItemStack getPickResult() {
    return this.correspondingItem != null
        ? correspondingItem.getDefaultInstance()
        : Items.MINECART.getDefaultInstance();
  }

  @Override
  protected void readAdditionalSaveData(@NonNull ValueInput nbt) {
    super.readAdditionalSaveData(nbt);
    int item = nbt.getIntOr("correspondingItem", -1);
    if (item != -1) {
      this.correspondingItem = Item.byId(item);
    }
    this.getEntityData().set(CORRESPONDING_ITEM, Item.getId(correspondingItem));
  }

  @Override
  protected void addAdditionalSaveData(@NonNull ValueOutput nbt) {
    nbt.putInt("correspondingItem", Item.getId(this.correspondingItem));
    super.addAdditionalSaveData(nbt);
  }

  public void setCorrespondingItem(Item correspondingItem) {
    this.correspondingItem = correspondingItem;
    this.entityData.set(CORRESPONDING_ITEM, Item.getId(correspondingItem));
  }
}
