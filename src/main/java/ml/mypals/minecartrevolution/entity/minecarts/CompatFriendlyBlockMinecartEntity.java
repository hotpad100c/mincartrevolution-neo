package ml.mypals.minecartrevolution.entity.minecarts;

import static ml.mypals.minecartrevolution.MinecartRevolution.FORCE_COMAPTERS;
import static ml.mypals.minecartrevolution.registeries.MREntityDataSerializers.COMPOUND_TAG_SERIALIZER;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.config.Config;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.ClientSimLevelFactory;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.LastCartInteractionCache;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulatedServerLevel;
import ml.mypals.minecartrevolution.item.WrenchItem;
import ml.mypals.minecartrevolution.mixin.blocks.BaseContainerBlockEntityAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.BlockEntityAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ScheduledTick;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompatFriendlyBlockMinecartEntity extends VariantBlockMinecartEntity
    implements ICapabilityProvider, ContainerEntity, PowerEmitterMinecartEntity {
  public BlockEntity blockEntity;
  public Level simulatedLevel;
  private CompoundTag blockEntityTag;
  public final List<ScheduledTick<Block>> pendingBlockTicks = Lists.newArrayList();
  public final List<ScheduledTick<Fluid>> pendingFluidTicks = Lists.newArrayList();
  public static final TagKey<Block> SAFE_TO_INTERACT =
      TagKey.create(
          Registries.BLOCK,
          Identifier.fromNamespaceAndPath("minecartrevolution", "safe_to_interact"));

  private static final EntityDataAccessor<CompoundTag> DATA_ID_BLOCK_ENTITY_NBT;

  static {
    DATA_ID_BLOCK_ENTITY_NBT =
        SynchedEntityData.defineId(
            CompatFriendlyBlockMinecartEntity.class, COMPOUND_TAG_SERIALIZER.get());
  }

  public CompatFriendlyBlockMinecartEntity(
      EntityType<CompatFriendlyBlockMinecartEntity> entityType, Level world) {
    super(entityType, world);
    initSimulatedLevel();
    refreshBlockEntity();
  }

  protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_ID_BLOCK_ENTITY_NBT, new CompoundTag());
  }

  public CompatFriendlyBlockMinecartEntity(
      EntityType<CompatFriendlyBlockMinecartEntity> minecart,
      Level world,
      double x,
      double y,
      double z,
      Item item) {
    super(minecart, world, x, y, z, item);
    initSimulatedLevel();
    refreshBlockEntity();
  }

  public CompatFriendlyBlockMinecartEntity(
      EntityType<CompatFriendlyBlockMinecartEntity> minecart,
      Level world,
      double x,
      double y,
      double z,
      Block block) {
    super(minecart, world, x, y, z);
    initSimulatedLevel();
    this.setCustomDisplayBlockState(Optional.of(block.defaultBlockState()));
    refreshBlockEntity();
  }

  public void initSimulatedLevel() {
    if (this.simulatedLevel == null) {
      this.simulatedLevel =
          level().isClientSide()
              ? ClientSimLevelFactory.create((ClientLevel) level(), this)
              : new SimulatedServerLevel((ServerLevel) level(), this);
    }
  }

  public void refreshBlockEntity() {
    BlockState state = getDisplayBlockState();
    if (state.hasBlockEntity()) {
      if (this.blockEntity == null || !this.blockEntity.getType().isValid(state)) {
        if (this.blockEntity != null) {
          this.blockEntity.setRemoved();
        }
        this.blockEntity =
            ((EntityBlock) state.getBlock()).newBlockEntity(this.blockPosition(), state);
        if (this.blockEntity != null) {
          Level simLevel = simulatedLevel;
          this.blockEntity.setLevel(simLevel);
          this.entityData.get(DATA_ID_BLOCK_ENTITY_NBT);
          this.blockEntity.loadWithComponents(
              TagValueInput.create(
                  ProblemReporter.DISCARDING,
                  registryAccess(),
                  this.entityData.get(DATA_ID_BLOCK_ENTITY_NBT)));
        }
      }
    } else {
      this.blockEntity = null;
    }
  }

  public void setBlockEntityTag(CompoundTag tag) {
    this.blockEntityTag = tag;
    this.entityData.set(DATA_ID_BLOCK_ENTITY_NBT, tag);
    if (this.blockEntity != null) {
      this.blockEntity.loadWithComponents(
          TagValueInput.create(ProblemReporter.DISCARDING, registryAccess(), tag));
    }
  }
  public void setContainerInfo(ItemContainerContents containerInfo) {
    if (this.blockEntity != null && blockEntity instanceof BaseContainerBlockEntity container) {
      containerInfo.copyInto(((BaseContainerBlockEntityAccessor)container).minecartRevolution$getItems());
    }
  }

  @Override
  public void onSyncedDataUpdated(@NonNull EntityDataAccessor<?> key) {
    super.onSyncedDataUpdated(key);
    if (DATA_ID_BLOCK_ENTITY_NBT.equals(key)) {
      CompoundTag tag = this.entityData.get(DATA_ID_BLOCK_ENTITY_NBT);
      this.blockEntityTag = tag;
      if(blockEntity == null) refreshBlockEntity();
      if (this.blockEntity != null && !tag.isEmpty()) {
        this.blockEntity.loadWithComponents(
            TagValueInput.create(ProblemReporter.DISCARDING, registryAccess(), tag));
      }
    }
  }

  @Override
  public @NonNull ItemStack getPickResult() {
    ItemStack stack = super.getPickResult();
    if (blockEntityTag != null && blockEntity != null) {
      TypedEntityData<BlockEntityType<?>> customData =
          TypedEntityData.of(this.blockEntity.getType(), blockEntityTag);
      stack.set(DataComponents.BLOCK_ENTITY_DATA, customData);
    }
    return stack;
  }

  @Override
  public void addDataToStack(ItemStack stack) {
    if (blockEntityTag != null && blockEntity != null) {
      TypedEntityData<BlockEntityType<?>> customData =
          TypedEntityData.of(this.blockEntity.getType(), blockEntityTag);
      stack.set(DataComponents.BLOCK_ENTITY_DATA, customData);
    }

  }

  @Override
  public void tick() {
    initSimulatedLevel();

    Level simLevel = simulatedLevel;

    BlockState state = getDisplayBlockState();
    try {
      if (this.blockEntity != null) {
        ((BlockEntityAccessor) this.blockEntity).mr$setWorldPosition(this.blockPosition());

        BlockEntityTicker<BlockEntity> ticker = state.getTicker(simLevel, (BlockEntityType<BlockEntity>) this.blockEntity.getType());
        if (ticker != null) {
          ticker.tick(simLevel, this.blockPosition(), state, this.blockEntity);
        }
      }

      if (!simLevel.isClientSide()) {
        long time = this.level().getGameTime();
        pendingBlockTicks.removeIf(
                tick -> {
                  if (tick.triggerTick() <= time) {
                    if (state.is(tick.type())) {
                      state.tick((ServerLevel) simLevel, this.blockPosition(), this.level().getRandom());
                    }
                    return true;
                  }
                  return false;
                });
        pendingFluidTicks.removeIf(
                tick -> {
                  if (tick.triggerTick() <= time) {
                    FluidState fluidState = this.simulatedLevel.getFluidState(this.blockPosition());

                    if (fluidState.is(tick.type())) {
                      fluidState.tick((ServerLevel) simLevel, this.blockPosition(), state);
                    }
                    return true;
                  }
                  return false;
                });
      } else {
        state.getBlock().animateTick(state, simLevel, blockPosition(), getRandom());
      }

    }catch (Exception ignored){}

    // Updates handled by AbstractMinecartMixin

    super.tick();
  }

  @Override
  protected void addAdditionalSaveData(@NonNull ValueOutput compound) {
    super.addAdditionalSaveData(compound);
    if (this.blockEntity != null) {
      ValueOutput be = compound.child("BlockEntityTag");
      this.blockEntity.saveWithFullMetadata(be);
      blockEntityTag = this.blockEntity.saveWithFullMetadata(this.registryAccess());
    }
  }

  @Override
  protected void readAdditionalSaveData(@NonNull ValueInput compound) {
    super.readAdditionalSaveData(compound);
    var beData = compound.child("BlockEntityTag");
    if (beData.isPresent()) {
      if (blockEntity == null) {
        refreshBlockEntity();
      }

      this.blockEntity.loadWithComponents(beData.get());
      blockEntityTag = this.blockEntity.saveWithFullMetadata(this.registryAccess());
      this.entityData.set(DATA_ID_BLOCK_ENTITY_NBT, blockEntityTag);
    }
  }

  @Override
  public ItemStack getCloneItemStack(BlockState blockState) {
    ItemStack stack = blockState.getCloneItemStack(simulatedLevel, blockPosition(), true);

    if (blockEntity != null) {
      try (ProblemReporter.ScopedCollector reporter =
          new ProblemReporter.ScopedCollector(
              blockEntity.problemPath(), MinecartRevolution.LOGGER)) {
        TagValueOutput output =
            TagValueOutput.createWithContext(reporter, simulatedLevel.registryAccess());
        blockEntity.saveCustomOnly(output);
        blockEntity.removeComponentsFromTag(output);
        BlockItem.setBlockEntityData(stack, blockEntity.getType(), output);
        stack.applyComponents(blockEntity.collectComponents());
      }
    }
    return stack;
  }

  @Override
  public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
    boolean bypassWhiteList = level().isClientSide() && Config.FORCE_COMPATIBILITY.get() || FORCE_COMAPTERS.contains(player.getUUID());
    boolean safeToInteract = bypassWhiteList || getDisplayBlockState().is(SAFE_TO_INTERACT);
    if(player.isSprinting()
        || player.isShiftKeyDown()
        || (!safeToInteract && blockEntity != null)) {
      return super.interact(player, hand, pos);
    }
    ItemStack stack = player.getItemInHand(hand);
    if (stack.getItem() instanceof WrenchItem) {
      return super.interact(player, hand, pos);
    }
    BlockState block = getDisplayBlockState();
    Level simLevel = simulatedLevel;

    BlockHitResult hit = new BlockHitResult(pos, player.getDirection(), this.blockPosition(), false);
    try {

      if (level().isClientSide() && Config.FORCE_COMPATIBILITY.get()){
        LastCartInteractionCache.LAST_INTERACTED = this;
      }

      InteractionResult itemUse = block.useItemOn(stack, simLevel, player, hand, hit);
      if (itemUse.consumesAction()) {
        return itemUse;
      }

      if (itemUse instanceof InteractionResult.TryEmptyHandInteraction && hand == InteractionHand.MAIN_HAND) {
        InteractionResult use = block.useWithoutItem(simLevel, player, hit);
        if (use.consumesAction()) {
          return use;
        }
      }
      return itemUse;

    } catch (Exception e) {
      return super.interact(player, hand, pos);
    }
  }

  @Override
  public void moveEntitiesAbove(Consumer<Entity> consumer) {
    super.moveEntitiesAbove((entity)->{
      getDisplayBlockState().getBlock().stepOn(simulatedLevel, blockPosition(), getDisplayBlockState(), entity);
      getDisplayBlockState().entityInside(simulatedLevel, blockPosition(), entity, InsideBlockEffectApplier.NOOP,true);
    });
  }
  @Override
  public void remove(@NonNull RemovalReason reason) {
    if (this.blockEntity != null) {
      blockEntity.setRemoved();
      blockEntity = null;
    }
    super.remove(reason);
  }

  @Override
  public void handleActive(ServerLevel level, int x, int y, int z, boolean powered) {
    if (activated != powered) {
      this.activated = powered;
      try {
        getDisplayBlockState()
                .handleNeighborChanged(simulatedLevel, blockPosition(), Blocks.AIR, null, false);
      }catch(Exception ignored){

      }
    }
  }

  @Override
  public @Nullable Object getCapability(@NonNull Object object, Object context) {
    return simulatedLevel.getCapability(
        (BlockCapability<? extends Object, ? super Object>) object, blockPosition(), context);
  }

  private Container getContainer(){
    Container container = null;
    if(blockEntity instanceof WorldlyContainerHolder){
      container = ((WorldlyContainerHolder) blockEntity).getContainer(getDisplayBlockState(), simulatedLevel, blockEntity.getBlockPos());
    }
    if(blockEntity instanceof Container container1){
      container = container1;
    }
    return container;
  }
  @Override
  public int getContainerSize() {
    Container container = getContainer();
    return container == null?0:container.getContainerSize();
  }

  @Override
  public @NonNull ItemStack getItem(int i) {
    Container container = getContainer();
    return container == null?ItemStack.EMPTY :container.getItem(i);
  }
  @Override
  public @NonNull ItemStack removeItem(int i, int i1) {
    Container container = getContainer();
    return container == null ? ItemStack.EMPTY : container.removeItem(i, i1);
  }

  @Override
  public @NonNull ItemStack removeItemNoUpdate(int i) {
    Container container = getContainer();
    return container == null ? ItemStack.EMPTY : container.removeItemNoUpdate(i);
  }

  @Override
  public void setItem(int i, @NonNull ItemStack itemStack) {
    Container container = getContainer();
    if (container != null) {
      container.setItem(i, itemStack);
    }
  }

  @Override
  public void setChanged() {
    Container container = getContainer();
    if (container != null) {
      container.setChanged();
    }
  }

  @Override
  public boolean stillValid(@NonNull Player player) {
    Container container = getContainer();
    return container != null && container.stillValid(player);
  }

  @Override
  public void clearContent() {
    Container container = getContainer();
    if (container != null) {
      container.clearContent();
    }
  }

  @Override
  public @Nullable ResourceKey<LootTable> getContainerLootTable() {
    return null;
  }

  @Override
  public void setContainerLootTable(@Nullable ResourceKey<LootTable> resourceKey) {

  }

  @Override
  public long getContainerLootTableSeed() {
    return  0L;
  }

  @Override
  public void setContainerLootTableSeed(long l) {

  }

  @Override
  public @NonNull NonNullList<ItemStack> getItemStacks() {
    Container container = getContainer();
    BaseContainerBlockEntity baseContainerBlockEntity = null;
    if(container instanceof BaseContainerBlockEntity blockEntity){
      baseContainerBlockEntity = blockEntity;
    }
    return baseContainerBlockEntity == null ? NonNullList.create() :
            ((BaseContainerBlockEntityAccessor)baseContainerBlockEntity).minecartRevolution$getItems();
  }

  @Override
  public void clearItemStacks() {
    Container container = getContainer();
    if (container != null) {
      container.clearContent();
    }
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory, @NonNull Player player) {
    Container container = getContainer();
    BaseContainerBlockEntity baseContainerBlockEntity = null;
    if(container instanceof BaseContainerBlockEntity blockEntity){
      baseContainerBlockEntity = blockEntity;
    }
    return baseContainerBlockEntity == null ? null : baseContainerBlockEntity.createMenu(i, inventory, player);
  }
  @Override
  public int getPowerStrength(Direction direction, BlockPos pos) {
    BlockState state = getDisplayBlockState();
    int signal = state.getSignal(simulatedLevel, pos, direction);
    return state.shouldCheckWeakPower(simulatedLevel, pos, direction) ?
            Math.max(signal, 0) : signal;
  }
}
