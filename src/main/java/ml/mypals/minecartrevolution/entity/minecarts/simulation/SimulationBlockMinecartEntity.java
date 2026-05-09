package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
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
    private final List<ScheduledTick<Block>> pendingBlockTicks = Lists.newArrayList();
    private final List<ScheduledTick<Fluid>> pendingFluidTicks = Lists.newArrayList();

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
    public ItemStack getBlockStack(Block block){
        ItemStack stack = super.getBlockStack(block);
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
            this.blockEntity.worldPosition = this.blockPosition();
            BlockState state = getDisplayBlockState();
            BlockEntityTicker<BlockEntity> ticker = state.getTicker(simLevel, (BlockEntityType<BlockEntity>) this.blockEntity.getType());
            if (ticker != null) {
                ticker.tick(simLevel, this.blockPosition(), state, this.blockEntity);
            }
        }

        // Process scheduled ticks
        if(!simLevel.isClientSide()){
            long time = this.level().getGameTime();
            pendingBlockTicks.removeIf(tick -> {
                if (tick.triggerTick() <= time) {
                    BlockState state = getDisplayBlockState();
                    if (state.is(tick.type())) {
                        state.tick(this.simulatedServerLevel.getLevel(), this.blockPosition(), this.level().getRandom());
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
                        state.tick(this.simulatedServerLevel.getLevel(), this.blockPosition(), blockState);
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
    public static class SimulatedLevel extends Level {
        private final Level wrapped;
        private final SimulationBlockMinecartEntity minecart;

        protected SimulatedLevel(Level wrapped, SimulationBlockMinecartEntity minecart) {
            super((WritableLevelData) wrapped.getLevelData(), wrapped.dimension(), wrapped.registryAccess(), wrapped.dimensionTypeRegistration(), wrapped.isClientSide(), wrapped.isDebug(), 0, 0);
            this.wrapped = wrapped;
            this.minecart = minecart;
        }
        public Level getLevel(){
            return wrapped;
        }

        @Override
        public void sendBlockUpdated(@NonNull BlockPos pos, @NonNull BlockState old, @NonNull BlockState current, @Block.UpdateFlags int updateFlags) {
            wrapped.sendBlockUpdated(pos, old, current, updateFlags);
        }

        @Override
        public void playSeededSound(@Nullable Entity except, double x, double y, double z, @NonNull Holder<SoundEvent> sound, @NonNull SoundSource source, float volume, float pitch, long seed) {
            wrapped.playSeededSound(except, x, y, z, sound, source, volume, pitch, seed);
        }

        @Override
        public void playSeededSound(@Nullable Entity except, @NonNull Entity sourceEntity, @NonNull Holder<SoundEvent> sound, @NonNull SoundSource source, float volume, float pitch, long seed) {
            wrapped.playSeededSound(except, sourceEntity, sound, source, volume, pitch, seed);
        }

        @Override
        public @NonNull BlockState getBlockState(BlockPos pos) {
            if (pos.equals(minecart.blockPosition())) {
                return minecart.getDisplayBlockState();
            }
            return wrapped.getBlockState(pos);
        }

        @Override
        public boolean setBlock(BlockPos pos,@NonNull BlockState state, int flags) {
            if (pos.equals(minecart.blockPosition())) {
                minecart.setCustomDisplayBlockState(Optional.of(state));
                minecart.refreshBlockEntity();
                return true;
            }
            return wrapped.setBlock(pos, state, flags);
        }

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            if (pos.equals(minecart.blockPosition())) {
                return minecart.blockEntity;
            }
            return wrapped.getBlockEntity(pos);
        }

        @Override
        public void blockEvent(BlockPos pos, @NonNull Block block, int eventID, int eventParam) {
            if (pos.equals(minecart.blockPosition()) && minecart.blockEntity != null) {
                minecart.blockEntity.triggerEvent(eventID, eventParam);
            } else {
                wrapped.blockEvent(pos, block, eventID, eventParam);
            }
        }

        @Override
        public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r, boolean fire,@NonNull ExplosionInteraction interactionType,@NonNull ParticleOptions smallExplosionParticles,@NonNull ParticleOptions largeExplosionParticles,@NonNull WeightedList<ExplosionParticleInfo> blockParticles,@NonNull Holder<SoundEvent> explosionSound) {
            wrapped.explode(source, damageSource, damageCalculator, x, y, z, r, fire, interactionType, smallExplosionParticles, largeExplosionParticles, blockParticles, explosionSound);
        }

        @Override
        public @NonNull String gatherChunkSourceStats() {
            return wrapped.gatherChunkSourceStats();
        }

        @Override
        public void setRespawnData(LevelData.@NonNull RespawnData respawnData) {
            wrapped.setRespawnData(respawnData);
        }

        @Override
        public LevelData.@NonNull RespawnData getRespawnData() {
            return wrapped.getRespawnData();
        }

        @Override
        public @Nullable Entity getEntity(int id) {
            return wrapped.getEntity(id);
        }

        @Override
        public @NonNull Collection<? extends PartEntity<?>> dragonParts() {
            return wrapped.dragonParts();
        }

        @Override
        public @NonNull TickRateManager tickRateManager() {
            return wrapped.tickRateManager();
        }

        @Override
        public @Nullable MapItemSavedData getMapData(@NonNull MapId id) {
            return wrapped.getMapData(id);
        }

        @Override
        public void destroyBlockProgress(int id, @NonNull BlockPos blockPos, int progress) {
            wrapped.destroyBlockProgress(id, blockPos, progress);
        }

        @Override
        public @NonNull Scoreboard getScoreboard() {
            return wrapped.getScoreboard();
        }

        @Override
        public @NonNull RecipeAccess recipeAccess() {
            return wrapped.recipeAccess();
        }

        @Override
        protected @NonNull LevelEntityGetter<Entity> getEntities() {
            return ((LevelAccessor)this.wrapped).minecartRevolution$getEntities();
        }

        @Override
        public @NonNull ClockManager clockManager() {
            return wrapped.clockManager();
        }

        @Override
        public @NonNull Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
            return wrapped.getUncachedNoiseBiome(quartX, quartY, quartZ);
        }

        @Override
        public int getSeaLevel() {
            return wrapped.getSeaLevel();
        }

        @Override
        public @NonNull FeatureFlagSet enabledFeatures() {
            return wrapped.enabledFeatures();
        }

        @Override
        public @NonNull EnvironmentAttributeSystem environmentAttributes() {
            return wrapped.environmentAttributes();
        }

        @Override
        public @NonNull PotionBrewing potionBrewing() {
            return wrapped.potionBrewing();
        }

        @Override
        public @NonNull FuelValues fuelValues() {
            return wrapped.fuelValues();
        }

        @Override
        public @NonNull ChunkSource getChunkSource() {
            return wrapped.getChunkSource();
        }

        @Override
        public void levelEvent(@Nullable Entity source, int type, @NonNull BlockPos pos, int data) {
            wrapped.levelEvent(source, type, pos, data);
        }

        @Override
        public void gameEvent(@NonNull Holder<GameEvent> gameEvent, @NonNull Vec3 position, GameEvent.@NonNull Context context) {
            wrapped.gameEvent(gameEvent, position, context);
        }

        @Override
        public @NonNull List<? extends Player> players() {
            return wrapped.players();
        }

        @Override
        public @NonNull WorldBorder getWorldBorder() {
            return wrapped.getWorldBorder();
        }

        @Override
        public @NonNull LevelTickAccess<Block> getBlockTicks() {
            return new SimulatedTickAccess<>(wrapped.getBlockTicks(), minecart, minecart.pendingBlockTicks);
        }

        @Override
        public @NonNull LevelTickAccess<Fluid> getFluidTicks() {
            return new SimulatedTickAccess<>(wrapped.getFluidTicks(), minecart, minecart.pendingFluidTicks);
        }
    }

    public static class SimulatedServerLevel extends ServerLevel {
        private final ServerLevel wrapped;
        private final SimulationBlockMinecartEntity minecart;

        protected SimulatedServerLevel(ServerLevel wrapped, SimulationBlockMinecartEntity minecart) {
            this.wrapped = wrapped;
            this.minecart = minecart;
            super(wrapped.getServer(),((ServerAccessor)wrapped.getServer()).mcr$getExecutor(),
                    ((ServerAccessor)wrapped.getServer()).mcr$getStorageSource(),
                    (ServerLevelData)wrapped.getLevelData(), wrapped.dimension(),
                    Objects.requireNonNull(wrapped.getServer().registries().compositeAccess().lookupOrThrow(Registries.LEVEL_STEM).getValue(LevelStem.OVERWORLD)),
                    wrapped.isDebug(), 0, List.of(),true);
        }

        public @NonNull ServerLevel getLevel(){
            return this;
        }

        @Override
        public void sendBlockUpdated(@NonNull BlockPos pos, @NonNull BlockState old, @NonNull BlockState current, @Block.UpdateFlags int updateFlags) {
            wrapped.sendBlockUpdated(pos, old, current, updateFlags);
        }

        @Override
        public void playSeededSound(@Nullable Entity except, double x, double y, double z, @NonNull Holder<SoundEvent> sound, @NonNull SoundSource source, float volume, float pitch, long seed) {
            wrapped.playSeededSound(except, x, y, z, sound, source, volume, pitch, seed);
        }

        @Override
        public void playSeededSound(@Nullable Entity except, @NonNull Entity sourceEntity, @NonNull Holder<SoundEvent> sound, @NonNull SoundSource source, float volume, float pitch, long seed) {
            wrapped.playSeededSound(except, sourceEntity, sound, source, volume, pitch, seed);
        }

        @Override
        public @NonNull BlockState getBlockState(BlockPos pos) {
            if (pos.equals(minecart.blockPosition())) {
                return minecart.getDisplayBlockState();
            }
            return wrapped.getBlockState(pos);
        }

        @Override
        public boolean setBlock(BlockPos pos, @NonNull BlockState state, int flags) {
            if (pos.equals(minecart.blockPosition())) {
                minecart.setCustomDisplayBlockState(Optional.of(state));
                minecart.refreshBlockEntity();
                return true;
            }
            return wrapped.setBlock(pos, state, flags);
        }

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            if (pos.equals(minecart.blockPosition())) {
                return minecart.blockEntity;
            }
            return wrapped.getBlockEntity(pos);
        }

        @Override
        public void blockEvent(BlockPos pos, @NonNull Block block, int eventID, int eventParam) {
            if (pos.equals(minecart.blockPosition()) && minecart.blockEntity != null) {
                minecart.blockEntity.triggerEvent(eventID, eventParam);
            } else {
                wrapped.blockEvent(pos, block, eventID, eventParam);
            }
        }

        @Override
        public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r, boolean fire, @NonNull ExplosionInteraction interactionType, @NonNull ParticleOptions smallExplosionParticles, @NonNull ParticleOptions largeExplosionParticles, @NonNull WeightedList<ExplosionParticleInfo> blockParticles, @NonNull Holder<SoundEvent> explosionSound) {
            wrapped.explode(source, damageSource, damageCalculator, x, y, z, r, fire, interactionType, smallExplosionParticles, largeExplosionParticles, blockParticles, explosionSound);
        }

        @Override
        public @NonNull String gatherChunkSourceStats() {
            return wrapped.gatherChunkSourceStats();
        }

        @Override
        public void setRespawnData(LevelData.@NonNull RespawnData respawnData) {
            wrapped.setRespawnData(respawnData);
        }

        @Override
        public LevelData.@NonNull RespawnData getRespawnData() {
            return wrapped.getRespawnData();
        }

        @Override
        public @Nullable Entity getEntity(int id) {
            return wrapped.getEntity(id);
        }

        @Override
        public @NonNull Collection<PartEntity<?>> dragonParts() {
            return wrapped.dragonParts();
        }

        @Override
        public @NonNull TickRateManager tickRateManager() {
            return wrapped.tickRateManager();
        }

        @Override
        public @Nullable MapItemSavedData getMapData(@NonNull MapId id) {
            return wrapped.getMapData(id);
        }

        @Override
        public void destroyBlockProgress(int id, @NonNull BlockPos blockPos, int progress) {
            wrapped.destroyBlockProgress(id, blockPos, progress);
        }

        @Override
        public @NonNull  ServerScoreboard getScoreboard() {
            return wrapped.getScoreboard();
        }

        @Override
        public @NonNull RecipeManager recipeAccess() {
            return wrapped.recipeAccess();
        }

        @Override
        public @NonNull LevelEntityGetter<Entity> getEntities() {
            return ((ServerLevelAccessor)this.wrapped).minecartRevolution$getEntities();
        }

        @Override
        public @NonNull ServerClockManager clockManager() {
            return wrapped.clockManager();
        }

        @Override
        public @NonNull Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
            return wrapped.getUncachedNoiseBiome(quartX, quartY, quartZ);
        }

        @Override
        public int getSeaLevel() {
            return wrapped.getSeaLevel();
        }

        @Override
        public @NonNull FeatureFlagSet enabledFeatures() {
            return wrapped.enabledFeatures();
        }

        @Override
        public @NonNull EnvironmentAttributeSystem environmentAttributes() {
            return wrapped.environmentAttributes();
        }

        @Override
        public @NonNull PotionBrewing potionBrewing() {
            return wrapped.potionBrewing();
        }

        @Override
        public @NonNull FuelValues fuelValues() {
            return wrapped.fuelValues();
        }

        @Override
        public @NonNull ServerChunkCache getChunkSource() {
            return wrapped.getChunkSource();
        }

        @Override
        public void levelEvent(@Nullable Entity source, int type, @NonNull BlockPos pos, int data) {
            wrapped.levelEvent(source, type, pos, data);
        }

        @Override
        public void gameEvent(@NonNull Holder<GameEvent> gameEvent,@NonNull Vec3 position, GameEvent.@NonNull Context context) {
            wrapped.gameEvent(gameEvent, position, context);
        }

        @Override
        public @NonNull List<ServerPlayer> players() {
            return wrapped.players();
        }

        @Override
        public @NonNull WorldBorder getWorldBorder() {
            return wrapped.getWorldBorder();
        }

        @Override
        public @NonNull LevelTicks<Block> getBlockTicks() {

            return new SimulatedServerTickAccess<>(this.getLevel(), wrapped.getBlockTicks(), minecart, minecart.pendingBlockTicks);
        }

        @Override
        public @NonNull LevelTicks<Fluid> getFluidTicks() {
            return new SimulatedServerTickAccess<>(this.getLevel(), wrapped.getFluidTicks(), minecart, minecart.pendingFluidTicks);
        }

    }

    public static class SimulatedTickAccess<T> implements LevelTickAccess<T> {
        private final LevelTickAccess<T> wrapped;
        private final SimulationBlockMinecartEntity minecart;
        private final List<ScheduledTick<T>> pendingTicks;

        public SimulatedTickAccess(LevelTickAccess<T> wrapped, SimulationBlockMinecartEntity minecart, List<ScheduledTick<T>> pendingTicks) {
            this.wrapped = wrapped;
            this.minecart = minecart;
            this.pendingTicks = pendingTicks;
        }

        @Override
        public void schedule(ScheduledTick<T> tick) {
            if (tick.pos().equals(minecart.blockPosition())) {
                pendingTicks.add(tick);
            } else {
                wrapped.schedule(tick);
            }
        }

        @Override
        public boolean hasScheduledTick(BlockPos pos, @NonNull T type) {
            if (pos.equals(minecart.blockPosition())) {
                return pendingTicks.stream().anyMatch(t -> t.type().equals(type));
            }
            return wrapped.hasScheduledTick(pos, type);
        }

        @Override
        public int count() {
            return pendingTicks.size() + wrapped.count();
        }

        @Override
        public boolean willTickThisTick(@NonNull BlockPos pos, @NonNull T type) {
            return wrapped.willTickThisTick(pos, type);
        }
    }


    public static class SimulatedServerTickAccess<T> extends LevelTicks<T> {
        private final LevelTicks<T> wrapped;
        private final SimulationBlockMinecartEntity minecart;
        private final List<ScheduledTick<T>> pendingTicks;

        public SimulatedServerTickAccess(ServerLevel serverLevel, LevelTicks<T> wrapped, SimulationBlockMinecartEntity minecart, List<ScheduledTick<T>> pendingTicks) {
            super((l)->serverLevel.isPositionEntityTicking(BlockPos.of(l)));
            this.wrapped = wrapped;
            this.minecart = minecart;
            this.pendingTicks = pendingTicks;
        }

        @Override
        public void schedule(ScheduledTick<T> tick) {
            if (tick.pos().equals(minecart.blockPosition())) {
                pendingTicks.add(tick);
            } else {
                wrapped.schedule(tick);
            }
        }

        @Override
        public boolean hasScheduledTick(BlockPos pos, @NonNull T type) {
            if (pos.equals(minecart.blockPosition())) {
                return pendingTicks.stream().anyMatch(t -> t.type().equals(type));
            }
            return wrapped.hasScheduledTick(pos, type);
        }

        @Override
        public int count() {
            return pendingTicks.size() + wrapped.count();
        }

        @Override
        public boolean willTickThisTick(@NonNull BlockPos pos, @NonNull T type) {
            return wrapped.willTickThisTick(pos, type);
        }
    }
}
