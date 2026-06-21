package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.simulation.ServerAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.ServerLevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTicks;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SimulatedServerLevel extends ServerLevel {
  private final ServerLevel wrapped;
  private final CompatFriendlyBlockMinecartEntity minecart;

  public SimulatedServerLevel(ServerLevel wrapped, CompatFriendlyBlockMinecartEntity minecart) {
    this.wrapped = wrapped;
    this.minecart = minecart;
    super(
        wrapped.getServer(),
        ((ServerAccessor) wrapped.getServer()).mcr$getExecutor(),
        ((ServerAccessor) wrapped.getServer()).mcr$getStorageSource(),
        (ServerLevelData) wrapped.getLevelData(),
        wrapped.dimension(),
        Objects.requireNonNull(
            wrapped
                .getServer()
                .registries()
                .compositeAccess()
                .lookupOrThrow(Registries.LEVEL_STEM)
                .getValue(LevelStem.OVERWORLD)),
        wrapped.isDebug(),
        0,
        List.of(),
        true);
  }

  public @NonNull ServerLevel getLevel() {
    return this;
  }

  @Override
  public void sendBlockUpdated(
      @NonNull BlockPos pos,
      @NonNull BlockState old,
      @NonNull BlockState current,
      @Block.UpdateFlags int updateFlags) {
    wrapped.sendBlockUpdated(pos, old, current, updateFlags);
  }

  @Override
  public void playSeededSound(
      @Nullable Entity except,
      double x,
      double y,
      double z,
      @NonNull Holder<SoundEvent> sound,
      @NonNull SoundSource source,
      float volume,
      float pitch,
      long seed) {
    wrapped.playSeededSound(except, x, y, z, sound, source, volume, pitch, seed);
  }

  @Override
  public void playSeededSound(
      @Nullable Entity except,
      @NonNull Entity sourceEntity,
      @NonNull Holder<SoundEvent> sound,
      @NonNull SoundSource source,
      float volume,
      float pitch,
      long seed) {
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
  public void explode(
      @Nullable Entity source,
      @Nullable DamageSource damageSource,
      @Nullable ExplosionDamageCalculator damageCalculator,
      double x,
      double y,
      double z,
      float r,
      boolean fire,
      @NonNull ExplosionInteraction interactionType,
      @NonNull ParticleOptions smallExplosionParticles,
      @NonNull ParticleOptions largeExplosionParticles,
      @NonNull WeightedList<ExplosionParticleInfo> blockParticles,
      @NonNull Holder<SoundEvent> explosionSound) {
    wrapped.explode(
        source,
        damageSource,
        damageCalculator,
        x,
        y,
        z,
        r,
        fire,
        interactionType,
        smallExplosionParticles,
        largeExplosionParticles,
        blockParticles,
        explosionSound);
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
  public @NonNull ServerScoreboard getScoreboard() {
    return wrapped.getScoreboard();
  }

  @Override
  public @NonNull RecipeManager recipeAccess() {
    return wrapped.recipeAccess();
  }

  @Override
  public @NonNull LevelEntityGetter<Entity> getEntities() {
    return ((ServerLevelAccessor) this.wrapped).minecartRevolution$getEntities();
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
  public void gameEvent(
      @NonNull Holder<GameEvent> gameEvent,
      @NonNull Vec3 position,
      GameEvent.@NonNull Context context) {
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

    return new SimulatedServerTickAccess<>(
        this.getLevel(), wrapped.getBlockTicks(), minecart, minecart.pendingBlockTicks);
  }

  @Override
  public @NonNull LevelTicks<Fluid> getFluidTicks() {
    return new SimulatedServerTickAccess<>(
        this.getLevel(), wrapped.getFluidTicks(), minecart, minecart.pendingFluidTicks);
  }

  public boolean addFreshEntity(@NonNull Entity entity) {
    return wrapped.addFreshEntity(entity);
  }

  public boolean addWithUUID(@NonNull Entity entity) {
    return wrapped.addWithUUID(entity);
  }

  public void addDuringTeleport(@NonNull Entity entity) {
    wrapped.addDuringTeleport(entity);
  }

  public <T extends Entity> void getEntities(
      @NonNull EntityTypeTest<Entity, T> type,
      @NonNull Predicate<? super T> selector,
      @NonNull List<? super T> result,
      int maxResults) {
    wrapped.getEntities(type, selector, result, maxResults);
  }

  public @NonNull List<? extends EnderDragon> getDragons() {
    return wrapped.getDragons();
  }

  public @NonNull List<ServerPlayer> getPlayers(@NonNull Predicate<? super ServerPlayer> selector) {
    return wrapped.getPlayers(selector, Integer.MAX_VALUE);
  }

  public @NonNull List<ServerPlayer> getPlayers(
      @NonNull Predicate<? super ServerPlayer> selector, int maxResults) {
    return wrapped.getPlayers(selector, maxResults);
  }

  public @Nullable ServerPlayer getRandomPlayer() {
    return wrapped.getRandomPlayer();
  }
}
