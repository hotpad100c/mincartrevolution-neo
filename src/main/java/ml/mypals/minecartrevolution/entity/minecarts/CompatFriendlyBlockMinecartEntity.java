package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.entity.minecarts.simulation.ClientSimLevelFactory;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulatedClientLevel;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulatedLevel;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulatedServerLevel;
import ml.mypals.minecartrevolution.item.WrenchItem;
import ml.mypals.minecartrevolution.mixin.simulation.BlockEntityAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.ScheduledTick;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

import static ml.mypals.minecartrevolution.registeries.MREntityDataSerializers.COMPOUND_TAG_SERIALIZER;

public class CompatFriendlyBlockMinecartEntity extends VariantBlockMinecartEntity {
    public BlockEntity blockEntity;
    public Level simulatedLevel;
    //private SimulatedServerLevel simulatedServerLevel;
    private CompoundTag blockEntityTag;
    public final List<ScheduledTick<Block>> pendingBlockTicks = Lists.newArrayList();
    public final List<ScheduledTick<Fluid>> pendingFluidTicks = Lists.newArrayList();


    private static final EntityDataAccessor<CompoundTag> DATA_ID_BLOCK_ENTITY_NBT;
    static {
        DATA_ID_BLOCK_ENTITY_NBT = SynchedEntityData.defineId(CompatFriendlyBlockMinecartEntity.class, COMPOUND_TAG_SERIALIZER.get());
    }
    public CompatFriendlyBlockMinecartEntity(EntityType<CompatFriendlyBlockMinecartEntity> entityType, Level world) {
        super(entityType, world);
        initSimulatedLevel();
        refreshBlockEntity();
    }

    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_ID_BLOCK_ENTITY_NBT, new CompoundTag());
    }


    public CompatFriendlyBlockMinecartEntity(EntityType<CompatFriendlyBlockMinecartEntity> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
       initSimulatedLevel();
        refreshBlockEntity();
    }
    public CompatFriendlyBlockMinecartEntity(EntityType<CompatFriendlyBlockMinecartEntity> minecart, Level world, double x, double y, double z, Block block) {
        super(minecart, world, x, y, z);
        initSimulatedLevel();
        this.setCustomDisplayBlockState(Optional.of(block.defaultBlockState()));
        refreshBlockEntity();
    }
    public void initSimulatedLevel(){
        if (this.simulatedLevel == null) {
            this.simulatedLevel = level().isClientSide()?
                    ClientSimLevelFactory.create((ClientLevel) level(), this)
                    :new SimulatedServerLevel((ServerLevel) level(), this);
        }
    }


    public void refreshBlockEntity() {
        BlockState state = getDisplayBlockState();
        if (state.hasBlockEntity()) {
            if (this.blockEntity == null || !this.blockEntity.getType().isValid(state)) {
                if (this.blockEntity != null) {
                    this.blockEntity.setRemoved();
                }
                this.blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(this.blockPosition(), state);
                if (this.blockEntity != null) {
                    Level simLevel = simulatedLevel;
                    this.blockEntity.setLevel(simLevel);
                    this.entityData.get(DATA_ID_BLOCK_ENTITY_NBT);
                    this.blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registryAccess(), this.entityData.get(DATA_ID_BLOCK_ENTITY_NBT)));
                }
            }
        } else {
            this.blockEntity = null;
        }
    }
    /*
    @Override
    public void setCustomDisplayBlockState(@NonNull Optional<BlockState> state) {
        BlockState oldState = getDisplayBlockState();
        this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY_BLOCK, state);
        state.ifPresent(state1 -> {
            Level simLevel = this.level().isClientSide()?simulatedLevel:simulatedServerLevel;
            state1.onPlace(simLevel, blockPosition(), oldState, false);
        });
    }*/
    public void setBlockEntityTag(CompoundTag tag) {
        this.blockEntityTag = tag;
        this.entityData.set(DATA_ID_BLOCK_ENTITY_NBT, tag);
        // If blockEntity already exists (e.g. refreshed before first tick), apply immediately
        if (this.blockEntity != null) {
            this.blockEntity.loadWithComponents(
                TagValueInput.create(ProblemReporter.DISCARDING, registryAccess(), tag)
            );
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ID_BLOCK_ENTITY_NBT.equals(key)) {
            CompoundTag tag = this.entityData.get(DATA_ID_BLOCK_ENTITY_NBT);
            this.blockEntityTag = tag;
            if (this.blockEntity != null && !tag.isEmpty()) {
                this.blockEntity.loadWithComponents(
                    TagValueInput.create(ProblemReporter.DISCARDING, registryAccess(), tag)
                );
            }
        }
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        ItemStack stack = super.getPickResult();
        if(blockEntityTag != null && blockEntity != null){
            TypedEntityData<BlockEntityType<?>> customData = TypedEntityData.of(this.blockEntity.getType(), blockEntityTag);
            stack.set(DataComponents.BLOCK_ENTITY_DATA, customData);
        }
        return stack;
    }
    @Override
    public ItemStack addDataToStack(ItemStack stack) {
        if(blockEntityTag != null && blockEntity != null){
            TypedEntityData<BlockEntityType<?>> customData = TypedEntityData.of(this.blockEntity.getType(), blockEntityTag);
            stack.set(DataComponents.BLOCK_ENTITY_DATA, customData);
        }
        return stack;
    }


    @Override
    public void tick() {
        initSimulatedLevel();

        Level simLevel = simulatedLevel;

        refreshBlockEntity();
        if (this.blockEntity != null) {
            ((BlockEntityAccessor)this.blockEntity).mr$setWorldPosition(this.blockPosition());

            BlockState state = getDisplayBlockState();
            BlockEntityTicker<BlockEntity> ticker = state.getTicker(simLevel, (BlockEntityType<BlockEntity>) this.blockEntity.getType());
            if (ticker != null) {
                ticker.tick(simLevel, this.blockPosition(), state, this.blockEntity);
            }
        }

        if(!simLevel.isClientSide()){
            long time = this.level().getGameTime();
            pendingBlockTicks.removeIf(tick -> {
                if (tick.triggerTick() <= time) {
                    BlockState state = getDisplayBlockState();
                    if (state.is(tick.type())) {
                        state.tick((ServerLevel) simLevel, this.blockPosition(), this.level().getRandom());
                    }
                    return true;
                }
                return false;
            });
            pendingFluidTicks.removeIf(tick -> {
                if (tick.triggerTick() <= time) {
                    FluidState state = this.simulatedLevel.getFluidState(this.blockPosition());
                    BlockState blockState = getDisplayBlockState();
                    if (state.is(tick.type())) {
                        state.tick((ServerLevel) simLevel, this.blockPosition(), blockState);
                    }
                    return true;
                }
                return false;
            });
        }

        super.tick();
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        if(this.blockEntity != null){
            ValueOutput be = compound.child("BlockEntityTag");
            this.blockEntity.saveWithFullMetadata(be);
            blockEntityTag = this.blockEntity.saveWithFullMetadata(this.registryAccess());
        }
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput compound) {
        super.readAdditionalSaveData(compound);
        var beData = compound.child("BlockEntityTag");
        if(beData.isPresent()){
            if(blockEntity == null){
                refreshBlockEntity();
            }

            this.blockEntity.loadWithComponents(beData.get());
            blockEntityTag = this.blockEntity.saveWithFullMetadata(this.registryAccess());
            this.entityData.set(DATA_ID_BLOCK_ENTITY_NBT, blockEntityTag);
        }
    }
    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        if(player.isSprinting() || player.isShiftKeyDown() || blockEntity != null){
            return super.interact(player, hand, pos);
        }
        ItemStack stack = player.getItemInHand(hand);
        if(stack.getItem() instanceof WrenchItem){
            return super.interact(player, hand, pos);
        }
        BlockState block = getDisplayBlockState();
        Level simLevel = simulatedLevel;

        try {
            return stack.isEmpty()?
                    block.useWithoutItem(
                            simLevel,
                            player,
                            new BlockHitResult(pos,player.getDirection(),this.blockPosition(),false)
                    ):
                    block.useItemOn(
                            stack,
                            simLevel,
                            player,
                            hand,
                            new BlockHitResult(pos,player.getDirection(),this.blockPosition(),false)
                    );
        }catch (Exception e){
            return super.interact(player, hand, pos);
        }

    }
    @Override
    public void remove(@NonNull RemovalReason reason) {
        if(this.blockEntity != null){
            blockEntity.setRemoved();
            blockEntity = null;
        }
        super.remove(reason);
    }


    @Override
    public void handleActive( ServerLevel level, int x, int y, int z, boolean powered) {
        if(activated != powered){
            this.activated = powered;
            getDisplayBlockState().handleNeighborChanged(simulatedLevel, blockPosition(), Blocks.AIR, null, false);
        }
    }

}
