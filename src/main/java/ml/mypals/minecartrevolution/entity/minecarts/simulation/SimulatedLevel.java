package ml.mypals.minecartrevolution.entity.minecarts.simulation;


import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.simulation.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SimulatedLevel extends Level {
    private final Level wrapped;
    private final CompatFriendlyBlockMinecartEntity minecart;

    public SimulatedLevel(Level wrapped, CompatFriendlyBlockMinecartEntity minecart) {
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
    @Override
    public int getSignal(@NotNull BlockPos pos, @NotNull Direction direction) {
        return minecart.activated?15:0;
    }
}