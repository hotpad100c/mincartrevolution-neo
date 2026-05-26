package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.simulation.BlockEntityAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.LevelAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.ServerAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.ServerLevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
