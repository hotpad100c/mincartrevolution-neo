package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.simulation.BlockEntityAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
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

public class SimulationBlockMinecartEntity extends VariantBlockMinecartEntity {
    public BlockEntity blockEntity;
    public SimulatedLevel simulatedLevel;
    private SimulatedServerLevel simulatedServerLevel;
    private CompoundTag blockEntityTag;
    public final List<ScheduledTick<Block>> pendingBlockTicks = Lists.newArrayList();
    public final List<ScheduledTick<Fluid>> pendingFluidTicks = Lists.newArrayList();

    public SimulationBlockMinecartEntity(EntityType<SimulationBlockMinecartEntity> entityType, Level world) {
        super(entityType, world);
        if (this.simulatedLevel == null) {
            this.simulatedLevel = new SimulatedLevel(this.level(), this);
        }
        if (this.simulatedServerLevel == null && !level().isClientSide()) {
            this.simulatedServerLevel = new SimulatedServerLevel((ServerLevel) this.level(), this);
        }
    }

    public SimulationBlockMinecartEntity(EntityType<SimulationBlockMinecartEntity> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
        if (this.simulatedLevel == null) {
            this.simulatedLevel = new SimulatedLevel(this.level(), this);
        }
        if (this.simulatedServerLevel == null && !level().isClientSide()) {
            this.simulatedServerLevel = new SimulatedServerLevel((ServerLevel) this.level(), this);
        }
    }
    public SimulationBlockMinecartEntity(EntityType<SimulationBlockMinecartEntity> minecart, Level world, double x, double y, double z, Block block) {
        super(minecart, world, x, y, z);
        if (this.simulatedLevel == null) {
            this.simulatedLevel = new SimulatedLevel(this.level(), this);
        }
        if (this.simulatedServerLevel == null && !level().isClientSide()) {
            this.simulatedServerLevel = new SimulatedServerLevel((ServerLevel) this.level(), this);
        }
        this.setCustomDisplayBlockState(Optional.of(block.defaultBlockState()));
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
                    this.blockEntity.setLevel(this.simulatedLevel);
                    if(blockEntityTag != null){
                        this.blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,registryAccess(),this.blockEntityTag));
                    }
                }
            }
        } else {
            this.blockEntity = null;
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
    public void tick() {
        if (this.simulatedLevel == null) {
            this.simulatedLevel = new SimulatedLevel(this.level(), this);
        }
        if (this.simulatedServerLevel == null && !level().isClientSide()) {
            this.simulatedServerLevel = new SimulatedServerLevel((ServerLevel) this.level(), this);
        }

        Level simLevel = this.level().isClientSide()?simulatedLevel:simulatedServerLevel;

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
                        state.tick(this.simulatedServerLevel, this.blockPosition(), this.level().getRandom());
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
                        state.tick(this.simulatedServerLevel, this.blockPosition(), blockState);
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
            if(blockEntity != null){
                this.blockEntity.loadWithComponents(beData.get());
                blockEntityTag = this.blockEntity.saveWithFullMetadata(this.registryAccess());
            }
        }
    }
    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        if(player.isSprinting() || player.isShiftKeyDown()){
            return super.interact(player, hand, pos);
        }
        BlockState block = getDisplayBlockState();


        ItemStack stack = player.getItemInHand(hand);
        Level simLevel = this.level().isClientSide()?simulatedLevel:simulatedServerLevel;

        System.out.println(this.level());

        System.out.println(simLevel);
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
            return InteractionResult.SUCCESS;
        }

    }
    @Override
    public void remove(@NonNull RemovalReason reason) {
        super.remove(reason);
    }







}
