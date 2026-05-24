package ml.mypals.minecartrevolution.mixin.minecart.furnace;

import ml.mypals.minecartrevolution.client.light.DynamicLightsSpread;
import ml.mypals.minecartrevolution.client.light.DynamicLightsStorage;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MinecartFurnace.class)
public abstract class FurnaceMinecartMixin extends AbstractMinecart implements Container {

    @Shadow
    private int fuel;
    @Unique
    private int mr$oldLight;

    protected FurnaceMinecartMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow
    protected abstract boolean hasFuel();

    @Shadow
    public Vec3 push;

    @Shadow
    protected abstract void setHasFuel(boolean fuel);

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_ID_FUEL;
    @Unique
    private int mincartrevolution$litTimeRemaining;
    @Unique
    private int mincartrevolution$litTotalTime;
    @Unique
    private NonNullList<ItemStack> mincartrevolution$items = NonNullList.withSize(3, ItemStack.EMPTY);

    @Unique
    private int mincartrevolution$cookingTimer = 0;

    @Unique
    private int mincartrevolution$cookingTotalTime = 200;

    @Unique
    private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> mincartrevolution$quickCheck;

    @Unique
    private final ContainerData mincartrevolution$dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> {
                    if (mincartrevolution$litTotalTime > Short.MAX_VALUE) {
                        // Neo: preserve litTime / litDuration ratio on the client as data slots are synced as shorts.
                        yield Mth.floor(((double) mincartrevolution$litTimeRemaining / mincartrevolution$litTotalTime) * Short.MAX_VALUE);
                    }

                    yield mincartrevolution$litTimeRemaining;
                }
                case 1 -> Math.min(mincartrevolution$litTotalTime, Short.MAX_VALUE);
                case 2 -> mincartrevolution$cookingTimer;
                case 3 -> mincartrevolution$cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    mincartrevolution$litTimeRemaining = value;
                    break;
                case 1:
                    mincartrevolution$litTotalTime = value;
                    break;
                case 2:
                    mincartrevolution$cookingTimer = value;
                    break;
                case 3:
                    mincartrevolution$cookingTotalTime = value;
                    break;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    @Inject(method = "getDefaultDisplayBlockState", at = @At("RETURN"), cancellable = true)
    private void getDefaultDisplayBlockState(CallbackInfoReturnable<BlockState> cir) {
        BlockState displayBlock = this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(null);
        BlockState state;
        if (displayBlock == null || !(displayBlock.getBlock() instanceof AbstractFurnaceBlock)) {
            state = Blocks.FURNACE.defaultBlockState();
        } else {
            state = displayBlock;
        }
        cir.setReturnValue(state.setValue(AbstractFurnaceBlock.FACING, Direction.NORTH)
                .setValue(AbstractFurnaceBlock.LIT, this.hasFuel()));
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initRecipeCheck(CallbackInfo ci) {
        this.mincartrevolution$quickCheck = RecipeManager.createCheck(mincartrevolution$getRecipie());
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTickSmelting(CallbackInfo ci) {
        super.tick();

        MinecartFurnace self = (MinecartFurnace) (Object) this;
        if (!self.level().isClientSide() && self.level() instanceof ServerLevel serverLevel) {
            this.mincartrevolution$processSmelting(serverLevel);
        }
        this.fuel = mincartrevolution$litTimeRemaining;

        if (!this.level().isClientSide()) {

            if (this.fuel <= 0) {
                this.push = Vec3.ZERO;
            }

            this.setHasFuel(this.fuel > 0);
        } else {
            mr$tickFurnaceDynamicLight((ClientLevel) self.level(), self);
        }

        if (this.entityData.get(DATA_ID_FUEL) && this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
        }
        ci.cancel();
    }

    @Override
    public void setCustomDisplayBlockState(@NonNull Optional<BlockState> state) {
        super.setCustomDisplayBlockState(state);
        this.mincartrevolution$quickCheck = RecipeManager.createCheck(mincartrevolution$getRecipie());
    }

    @Inject(method = "getDropItem", at = @At("HEAD"), cancellable = true)
    protected void getDropItem(CallbackInfoReturnable<Item> cir) {
        cir.setReturnValue(mincartrevolution$getItem());
    }

    @Inject(method = "getPickResult", at = @At("HEAD"), cancellable = true)
    public void getPickResult(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(mincartrevolution$getItem().getDefaultInstance());
    }

    @Unique
    private Item mincartrevolution$getItem() {
        BlockState displayBlock = this.entityData
                .get(DATA_ID_CUSTOM_DISPLAY_BLOCK)
                .orElse(Blocks.AIR.defaultBlockState());
        Block block = displayBlock.getBlock();
        return switch (block) {
            case BlastFurnaceBlock ignored -> MRMinecarts.BLAST_FURNACE_MINECART.item().asItem();
            case SmokerBlock ignored -> MRMinecarts.SMOKER_MINECART.item().asItem();
            default -> Items.FURNACE_MINECART;
        };
    }

    @Inject(method = "addFuel", at = @At("HEAD"), cancellable = true)
    public void addFuel(Vec3 interactingPos, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {

        ItemStack fuel = this.mincartrevolution$items.get(1);

        if (
                itemStack.is(ItemTags.FURNACE_MINECART_FUEL)
                        && (fuel.isEmpty() || fuel.is(itemStack.typeHolder()))
                        && fuel.count() < fuel.getMaxStackSize()
        ) {
            if (fuel.isEmpty()) {
                fuel = itemStack.copy();
                fuel.setCount(1);
            } else {
                fuel.grow(1);
            }
            this.mincartrevolution$items.set(1, fuel);
            if (this.fuel > 0) {
                this.push = this.position().subtract(interactingPos).horizontal();
            }

            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
        cir.cancel();
    }

    @Inject(method = "hasFuel", at = @At("RETURN"), cancellable = true)
    protected void hasFuel(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(mincartrevolution$litTimeRemaining > 0);
    }

    @Unique
    private static void mincartrevolution$consumeFuel(NonNullList<ItemStack> items, ItemStack fuel) {
        Item fuelItem = fuel.getItem();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            ItemStackTemplate remainder = fuelItem.getCraftingRemainder();
            items.set(1, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
    }

    @Unique
    private void mincartrevolution$processSmelting(ServerLevel level) {
        ItemStack input = this.mincartrevolution$items.get(0);

        boolean isLit;
        if (this.mincartrevolution$litTimeRemaining > 0) {
            mincartrevolution$litTimeRemaining--;
            isLit = mincartrevolution$litTimeRemaining > 0;
        } else {
            isLit = false;
        }

        ItemStack fuel = this.mincartrevolution$items.get(1);

        if (hasFuel() || !fuel.isEmpty()) {
            SingleRecipeInput recipeInput = new SingleRecipeInput(input);
            RecipeHolder<? extends AbstractCookingRecipe> recipe =
                    this.mincartrevolution$quickCheck.getRecipeFor(recipeInput, level).orElse(null);

            if (!isLit) {
                int newLitTime = mincartrevolution$getBurnDuration(level.fuelValues(), fuel);
                mincartrevolution$litTimeRemaining = newLitTime;
                mincartrevolution$litTotalTime = newLitTime;
                if (newLitTime > 0) {
                    mincartrevolution$consumeFuel(mincartrevolution$items, fuel);
                }
            }

            if (recipe != null) {
                ItemStack result = recipe.value().assemble(recipeInput);

                if (!result.isEmpty() && this.mincartrevolution$canSmelt(result)) {
                    this.mincartrevolution$cookingTimer++;
                    if (this.mincartrevolution$cookingTimer >= this.mincartrevolution$cookingTotalTime) {
                        this.mincartrevolution$cookingTimer = 0;
                        this.mincartrevolution$cookingTotalTime = recipe.value().cookingTime();
                        this.mincartrevolution$smelt(input, result);
                    }
                } else {
                    this.mincartrevolution$cookingTimer = 0;
                }


            } else {
                this.mincartrevolution$cookingTimer = 0;
            }
        } else if (this.mincartrevolution$cookingTimer > 0) {
            this.mincartrevolution$cookingTimer = Mth.clamp(
                    this.mincartrevolution$cookingTimer - 2,
                    0,
                    this.mincartrevolution$cookingTotalTime
            );
        }
    }

    @Unique
    protected int mincartrevolution$getBurnDuration(FuelValues fuelValues, ItemStack itemStack) {
        return itemStack.getBurnTime(mincartrevolution$getRecipie(), fuelValues);
    }

    @Unique
    private RecipeType<? extends AbstractCookingRecipe> mincartrevolution$getRecipie() {
        BlockState displayBlock = this.entityData
                .get(DATA_ID_CUSTOM_DISPLAY_BLOCK)
                .orElse(Blocks.AIR.defaultBlockState());
        Block block = displayBlock.getBlock();
        return switch (block) {
            case BlastFurnaceBlock ignored -> RecipeType.BLASTING;
            case SmokerBlock ignored -> RecipeType.SMOKING;
            default -> RecipeType.SMELTING;
        };
    }

    @Unique
    private boolean mincartrevolution$canSmelt(ItemStack result) {
        ItemStack currentOutput = this.mincartrevolution$items.get(2);

        if (currentOutput.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(currentOutput, result)) {
            return false;
        }

        int newCount = currentOutput.getCount() + result.getCount();
        return newCount <= Math.min(64, result.getMaxStackSize());
    }

    @Unique
    private void mincartrevolution$smelt(ItemStack input, ItemStack result) {
        ItemStack output = this.mincartrevolution$items.get(2);

        if (output.isEmpty()) {
            this.mincartrevolution$items.set(2, result.copy());
        } else {
            output.grow(result.getCount());
        }

        input.shrink(1);
    }


    @Inject(method = "interact", at = @At("TAIL"), cancellable = true)
    private void onInteractWithSmelting(
            Player player,
            InteractionHand hand,
            Vec3 location,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        MinecartFurnace self = (MinecartFurnace) (Object) this;

        if (player.isShiftKeyDown() && !self.level().isClientSide()) {
            ItemStack held = player.getItemInHand(hand);

            if (held.isEmpty() && !this.mincartrevolution$items.get(2).isEmpty()) {
                ItemStack output = this.mincartrevolution$items.get(2).copy();
                this.mincartrevolution$items.set(2, ItemStack.EMPTY);
                player.setItemInHand(hand, output);
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            if (!held.isEmpty() && this.mincartrevolution$items.get(0).isEmpty()) {
                ItemStack toInsert = held.split(held.count());
                this.mincartrevolution$items.set(0, toInsert);
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            if (held.isEmpty() && !this.mincartrevolution$items.get(0).isEmpty()) {
                ItemStack input = this.mincartrevolution$items.get(0).copy();
                this.mincartrevolution$items.set(0, ItemStack.EMPTY);
                player.setItemInHand(hand, input);
                this.mincartrevolution$cookingTimer = 0;
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            if (held.isEmpty() && !this.mincartrevolution$items.get(1).isEmpty()) {
                ItemStack input = this.mincartrevolution$items.get(1).copy();
                this.mincartrevolution$items.set(1, ItemStack.EMPTY);
                player.setItemInHand(hand, input);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        } else if (!self.level().isClientSide() && !player.isShiftKeyDown()) {
            ItemStack held = player.getItemInHand(hand);
            if (!held.is(net.minecraft.world.item.Items.COAL) && !held.is(net.minecraft.world.item.Items.CHARCOAL)) {

                BlockState displayBlock = this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
                Block block = displayBlock.getBlock();
                SimpleMenuProvider menuProvider = switch (block) {
                    case BlastFurnaceBlock ignored -> new SimpleMenuProvider(
                            (containerId, playerInventory, _) ->
                                    new BlastFurnaceMenu(containerId, playerInventory,
                                            FurnaceMinecartMixin.this,
                                            FurnaceMinecartMixin.this.mincartrevolution$dataAccess),
                            Component.translatable("container.blast_furnace")
                    );
                    case SmokerBlock ignored -> new SimpleMenuProvider(
                            (containerId, playerInventory, _) ->
                                    new SmokerMenu(containerId, playerInventory,
                                            FurnaceMinecartMixin.this,
                                            FurnaceMinecartMixin.this.mincartrevolution$dataAccess),
                            Component.translatable("container.smoker")
                    );
                    default -> new SimpleMenuProvider(
                            (containerId, playerInventory, _) ->
                                    new FurnaceMenu(containerId, playerInventory,
                                            FurnaceMinecartMixin.this,
                                            FurnaceMinecartMixin.this.mincartrevolution$dataAccess),
                            Component.translatable("container.furnace")
                    );
                };

                player.openMenu(menuProvider);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveFurnaceData(ValueOutput output, CallbackInfo ci) {
        BlockState displayBlock = this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

        ContainerHelper.saveAllItems(output, this.mincartrevolution$items);
        output.putShort("SmeltingProgress", (short) this.mincartrevolution$cookingTimer);
        output.putShort("SmeltingTime", (short) this.mincartrevolution$cookingTotalTime);
        output.putInt("BlockInside", Block.getId(displayBlock));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadFurnaceData(ValueInput input, CallbackInfo ci) {
        this.mincartrevolution$items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.mincartrevolution$items);
        this.mincartrevolution$cookingTimer = input.getShortOr("SmeltingProgress", (short) 0);
        this.mincartrevolution$cookingTotalTime = input.getShortOr("SmeltingTime", (short) 200);
        if (getDisplayBlockState().isEmpty())
            this.setCustomDisplayBlockState(Optional.of(Block.stateById(input.getIntOr("BlockInside", Block.getId(Blocks.FURNACE.defaultBlockState())))));
    }


    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.mincartrevolution$items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        return slot >= 0 && slot < 3 ? this.mincartrevolution$items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = this.mincartrevolution$items.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed;
        if (stack.getCount() <= amount) {
            removed = stack;
            this.mincartrevolution$items.set(slot, ItemStack.EMPTY);
        } else {
            removed = stack.split(amount);
        }

        if (slot == 0) {
            this.mincartrevolution$cookingTimer = 0;
        }

        return removed;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.mincartrevolution$items.get(slot);
        this.mincartrevolution$items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        this.mincartrevolution$items.set(slot, stack);
        if (slot == 0 && !stack.isEmpty()) {
            this.mincartrevolution$cookingTimer = 0;
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        MinecartFurnace self = (MinecartFurnace) (Object) this;
        return !self.isRemoved() && player.distanceToSqr(self) <= 64.0;
    }

    @Override
    public void clearContent() {
        this.mincartrevolution$items.clear();
        this.mincartrevolution$cookingTimer = 0;
    }

    @Override
    public void remove(Entity.@NonNull RemovalReason reason) {
        MinecartFurnace self = (MinecartFurnace) (Object) this;
        if (self.level().isClientSide()) {
            if (DynamicLightsStorage.LIGHT_SOURCES.remove(self) != null) {
                DynamicLightsSpread.markAreaDirty(self.blockPosition(), DynamicLightsSpread.RADIUS);
            }
        }
        super.remove(reason);
    }

    @Unique
    private void mr$tickFurnaceDynamicLight(ClientLevel world, MinecartFurnace thiz) {
        boolean isBurning = this.entityData.get(DATA_ID_FUEL);
        int lightLevel = isBurning ? 13 : 0;

        BlockPos blockPos = thiz.blockPosition();
        BlockPos oldPos = BlockPos.containing(thiz.oldPosition());
        boolean moved = !oldPos.equals(blockPos);

        if (lightLevel > 0 || mr$oldLight != lightLevel) {
            mr$oldLight = lightLevel;
            DynamicLightsStorage.LIGHT_SOURCES.put(thiz, lightLevel);
            if (moved) {
                DynamicLightsSpread.markAreaDirty(oldPos, DynamicLightsSpread.RADIUS);
                DynamicLightsSpread.markAreaDirty(blockPos, DynamicLightsSpread.RADIUS);
                mr$updateDynamicLight(world, blockPos);
            }
        } else {
            if (DynamicLightsStorage.LIGHT_SOURCES.remove(thiz) != null) {
                DynamicLightsSpread.markAreaDirty(oldPos, DynamicLightsSpread.RADIUS);
                DynamicLightsSpread.markAreaDirty(blockPos, DynamicLightsSpread.RADIUS);
                mr$updateDynamicLight(world, blockPos);
            }
        }
    }

    @Unique
    private void mr$updateDynamicLight(ClientLevel world, BlockPos pos) {
        int vanillaLight = world.getBlockState(pos).getLightEmission(world, pos);
        double dynamicLight = DynamicLightsStorage.getLightLevel(pos);

        if (dynamicLight >= vanillaLight) {
            world.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }
    @Override
    public void onClientRemoval() {
        mr$removeDynamicLight(this.blockPosition(), true);
        super.onClientRemoval();
    }
    @Unique
    private void mr$removeDynamicLight(BlockPos pos, boolean update) {
        if (update) {
            BlockPos blockPos = this.blockPosition();
            BlockPos oldPos = BlockPos.containing(this.oldPosition());
            DynamicLightsSpread.markAreaDirty(oldPos, DynamicLightsSpread.RADIUS);
            DynamicLightsSpread.markAreaDirty(blockPos, DynamicLightsSpread.RADIUS);
        }
    }

}