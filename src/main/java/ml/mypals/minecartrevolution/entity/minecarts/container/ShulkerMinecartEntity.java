package ml.mypals.minecartrevolution.entity.minecarts.container;

import com.mojang.logging.LogUtils;
import ml.mypals.minecartrevolution.client.menu.MinecartChestMenu;
import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.Optional;

public class ShulkerMinecartEntity extends BaseMinecartContainer implements IMinecartContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private float progress;
    private float progressOld;
    private int openCount;
    private AnimationStatus animationStatus = AnimationStatus.CLOSED;

    public enum AnimationStatus {CLOSED, OPENING, OPENED, CLOSING}

    public ShulkerMinecartEntity(EntityType<? extends AbstractMinecartContainer> entityType, Level world) {
        super(entityType, world);
    }

    public ShulkerMinecartEntity(EntityType<? extends AbstractMinecartContainer> entityType, Level world, double x, double y, double z) {
        super(entityType, world);
        setInitialPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        this.updateAnimation();
    }

    private void updateAnimation() {
        this.progressOld = this.progress;
        switch (this.animationStatus) {
            case CLOSED -> this.progress = 0.0F;
            case OPENING -> {
                this.progress += 0.1F;
                if (this.progress >= 1.0F) {
                    this.animationStatus = AnimationStatus.OPENED;
                    this.progress = 1.0F;
                }
            }
            case OPENED -> this.progress = 1.0F;
            case CLOSING -> {
                this.progress -= 0.1F;
                if (this.progress <= 0.0F) {
                    this.animationStatus = AnimationStatus.CLOSED;
                    this.progress = 0.0F;
                }
            }
        }
    }

    public float getProgress(float partialTick) {
        return Mth.lerp(partialTick, this.progressOld, this.progress);
    }

    public ShulkerMinecartEntity(EntityType<ShulkerMinecartEntity> shulkerMinecart, Level world, double x, double y, double z, ShulkerBoxBlock blockInside) {
        super(shulkerMinecart, world);
        setInitialPos(x, y, z);
        this.setCustomDisplayBlockState(Optional.of(blockInside.defaultBlockState().setValue(ShulkerBoxBlock.FACING, Direction.UP)));
    }

    @Override
    public void destroy(@NonNull ServerLevel level, @NonNull DamageSource source) {
        killAndDropSelf(level, source, false);
        this.kill(level);
    }

    @Override
    public void remove(Entity.@NonNull RemovalReason reason) {
        this.setRemoved(reason);
    }

    public void killAndDropSelf(ServerLevel level, DamageSource source, boolean separate) {
        if (this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).isEmpty()) this.spawnAtLocation(level, Items.MINECART);

        Block block = this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).get().getBlock();
        if (separate) {
            this.spawnAtLocation(level, getMinecartStack(level, block instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock) block).getColor() : null));
        } else {
            this.spawnAtLocation(level, getMinecartStackWithInventory(block instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock) block).getColor() : null));
        }

    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        if (this.level().isClientSide() || this.isRemoved()) {
            return true;
        } else if (this.isInvulnerableToBase(source)) {
            return false;
        } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + amount * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
            boolean bl = source.getEntity() instanceof Player && ((Player) source.getEntity()).getAbilities().instabuild;
            if ((bl || !(this.getDamage() > 40.0F)) && !this.shouldSourceDestroy(source)) {
                if (bl) {
                    if (!this.isEmpty())
                        this.killAndDropSelf(level, source, false);
                    this.remove(Entity.RemovalReason.DISCARDED);
                }
            } else {
                if (source.getEntity() instanceof Player playerEntity && !playerEntity.isShiftKeyDown()) {
                    this.killAndDropSelf(level, source, true);
                } else {
                    this.destroy(level, source);
                }
                this.kill(level);
            }

            return true;
        }
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        ItemStack stack = MRMinecarts.SHULKER_MINECART.item().get().getDefaultInstance();
        CompoundTag nbt = new CompoundTag();
        this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).ifPresent(blockState -> {
            nbt.putInt("block_in_minecart", Block.getId(blockState));
        });
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        return stack;
    }

    public ItemStack getMinecartStackWithInventory(@Nullable DyeColor dyeColor) {
        ItemStack itemStack = new ItemStack(MRMinecarts.SHULKER_MINECART.item().get());

        CompoundTag nbt = new CompoundTag();
        Block block = get(dyeColor);
        nbt.putInt("block_in_minecart", Block.getId(block.defaultBlockState().setValue(ShulkerBoxBlock.FACING, Direction.UP)));
        setInventoryToItemStack(block, nbt);
        itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItemStacks()));
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        return itemStack;
    }


    public ItemStack getMinecartStack(ServerLevel level, @Nullable DyeColor dyeColor) {
        ItemStack itemStack = new ItemStack(Items.MINECART);
        Block block = get(dyeColor);
        ItemStack box = block.asItem().getDefaultInstance();

        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItemStacks()));
        this.spawnAtLocation(level, box);
        return itemStack;
    }

    public void setInventoryToItemStack(Block block, CompoundTag nbtCompound) {
        if (block instanceof ShulkerBoxBlock) {
            ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER);
            TagValueOutput valueOutput = TagValueOutput.createWithContext(reporter, this.registryAccess());
            ContainerHelper.saveAllItems(valueOutput, this.getItemStacks(), false);
            nbtCompound.put("inventory", valueOutput.buildResult());
        }
    }

    public static Block get(@Nullable DyeColor dyeColor) {
        if (dyeColor == null) {
            return Blocks.SHULKER_BOX;
        } else {
            return switch (dyeColor) {
                case WHITE -> Blocks.WHITE_SHULKER_BOX;
                case ORANGE -> Blocks.ORANGE_SHULKER_BOX;
                case MAGENTA -> Blocks.MAGENTA_SHULKER_BOX;
                case LIGHT_BLUE -> Blocks.LIGHT_BLUE_SHULKER_BOX;
                case YELLOW -> Blocks.YELLOW_SHULKER_BOX;
                case LIME -> Blocks.LIME_SHULKER_BOX;
                case PINK -> Blocks.PINK_SHULKER_BOX;
                case GRAY -> Blocks.GRAY_SHULKER_BOX;
                case LIGHT_GRAY -> Blocks.LIGHT_GRAY_SHULKER_BOX;
                case CYAN -> Blocks.CYAN_SHULKER_BOX;
                case BLUE -> Blocks.BLUE_SHULKER_BOX;
                case BROWN -> Blocks.BROWN_SHULKER_BOX;
                case GREEN -> Blocks.GREEN_SHULKER_BOX;
                case RED -> Blocks.RED_SHULKER_BOX;
                case BLACK -> Blocks.BLACK_SHULKER_BOX;
                case PURPLE -> Blocks.PURPLE_SHULKER_BOX;
            };
        }
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.SHULKER_MINECART.item().get();
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.SHULKER_BOX.defaultBlockState().setValue(ShulkerBoxBlock.FACING, Direction.UP);
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 8;
    }

    @Override
    public @NonNull AbstractContainerMenu createMenu(int syncId, @NonNull Inventory playerInventory) {

        return new MinecartChestMenu(MenuType.GENERIC_9x3, syncId, playerInventory, this, 3, this);
    }

    public void getInventoryAsNbt(ValueOutput nbt, Inventory playerInventory) {
        ContainerHelper.saveAllItems(nbt, getItemStacks(), false);
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
        if (!player.isSecondaryUseActive()) {
            InteractionResult actionResult = this.interactWithContainerVehicle(player);
            if (actionResult.consumesAction()) {
                if (!this.level().isClientSide()) {
                    this.openCount++;
                    if (this.openCount >= 1) {
                        this.level().playSound(this, this.blockPosition(),
                                SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS);
                    }
                    this.level().broadcastEntityEvent(this, (byte) 10);
                    this.gameEvent(GameEvent.CONTAINER_OPEN, player);
                    if (player.level() instanceof ServerLevel serverLevel) {
                        PiglinAi.angerNearbyPiglins(serverLevel, player, true);
                    }
                }
            }
            return actionResult;
        }
        return super.interact(player, hand, location);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            if (this.level().isClientSide()) {
                this.openCount++;
                this.animationStatus = AnimationStatus.OPENING;
            }
        } else if (id == 11) {
            if (this.level().isClientSide()) {
                this.openCount = Math.max(0, this.openCount - 1);
            }
            this.animationStatus = AnimationStatus.CLOSING;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void minecartrevolution$OnContainerClosed(Level level, Player player) {
        if (!this.level().isClientSide()) {
            this.openCount = Math.max(0, this.openCount - 1);
            this.level().broadcastEntityEvent(this, (byte) 11);
            if (this.openCount == 0) {
                this.gameEvent(GameEvent.CONTAINER_CLOSE, player);
                this.level().playSound(this, this.blockPosition(),
                        SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS);
            }
        }
    }
}
