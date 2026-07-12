package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import java.util.*;
import java.util.function.Predicate;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.entity.EntityAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.ServerAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.ServerLevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
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
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.SavedDataStorage;
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
  public @NonNull Holder<Biome> getBiome(@NonNull BlockPos pos) {
    return wrapped.getBiome(pos);
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
    if (id == -Integer.MAX_VALUE) return minecart;
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
    ((EntityAccessor) entity).minecartrevolution$setLevel(wrapped);
    return wrapped.addFreshEntity(entity);
  }

  public boolean addWithUUID(@NonNull Entity entity) {
    ((EntityAccessor) entity).minecartrevolution$setLevel(wrapped);
    return wrapped.addWithUUID(entity);
  }

  public void addDuringTeleport(@NonNull Entity entity) {
    ((EntityAccessor) entity).minecartrevolution$setLevel(wrapped);
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

  @Override
  public @NonNull RegistryAccess registryAccess() {
    return wrapped.registryAccess();
  }

  @Override
  public @NonNull GameRules getGameRules() {
    return wrapped.getGameRules();
  }

  @Override
  public @NonNull MinecraftServer getServer() {
    return wrapped.getServer();
  }

  @Override
  public @NonNull SavedDataStorage getDataStorage() {
    return wrapped.getDataStorage();
  }

  @Override
  public <T extends ParticleOptions> int sendParticles(
      @NonNull T particle,
      double x,
      double y,
      double z,
      int count,
      double xDist,
      double yDist,
      double zDist,
      double speed) {
    return wrapped.sendParticles(particle, x, y, z, count, xDist, yDist, zDist, speed);
  }

  @Override
  public <T extends ParticleOptions> int sendParticles(
      @NonNull T particle,
      boolean overrideLimiter,
      boolean alwaysShow,
      double x,
      double y,
      double z,
      int count,
      double xDist,
      double yDist,
      double zDist,
      double speed) {
    return wrapped.sendParticles(
        particle, overrideLimiter, alwaysShow, x, y, z, count, xDist, yDist, zDist, speed);
  }

  @Override
  public <T extends ParticleOptions> boolean sendParticles(
      @NonNull ServerPlayer player,
      @NonNull T particle,
      boolean overrideLimiter,
      boolean alwaysShow,
      double x,
      double y,
      double z,
      int count,
      double xDist,
      double yDist,
      double zDist,
      double speed) {
    return wrapped.sendParticles(
        player, particle, overrideLimiter, alwaysShow, x, y, z, count, xDist, yDist, zDist, speed);
  }

  @Override
  public void addNewPlayer(@NonNull ServerPlayer player) {
    wrapped.addNewPlayer(player);
  }

  @Override
  public void addRespawnedPlayer(@NonNull ServerPlayer player) {
    wrapped.addRespawnedPlayer(player);
  }

  @Override
  public void removePlayerImmediately(
      @NonNull ServerPlayer player, Entity.@NonNull RemovalReason reason) {
    wrapped.removePlayerImmediately(player, reason);
  }

  @Override
  public @Nullable Entity getEntityInAnyDimension(@NonNull UUID uuid) {
    return wrapped.getEntityInAnyDimension(uuid);
  }

  @Override
  public @Nullable Player getPlayerInAnyDimension(@NonNull UUID uuid) {
    return wrapped.getPlayerInAnyDimension(uuid);
  }

  @Override
  public void globalLevelEvent(int type, @NonNull BlockPos pos, int data) {
    wrapped.globalLevelEvent(type, pos, data);
  }

  @Override
  public float getMoonBrightness(@NonNull BlockPos pos) {
    return wrapped.getMoonBrightness(pos);
  }

  @Override
  public void resetWeatherCycle() {
    wrapped.resetWeatherCycle();
  }

  @Override
  public @NonNull WeatherData getWeatherData() {
    return wrapped.getWeatherData();
  }

  @Override
  public @NonNull DifficultyInstance getCurrentDifficultyAt(@NonNull BlockPos pos) {
    return wrapped.getCurrentDifficultyAt(pos);
  }

  @Override
  public long getSeed() {
    return wrapped.getSeed();
  }

  @Override
  public boolean isFlat() {
    return wrapped.isFlat();
  }

  @Override
  public boolean noSave() {
    return wrapped.noSave();
  }

  @Override
  public boolean isPvpAllowed() {
    return wrapped.isPvpAllowed();
  }

  @Override
  public boolean isCommandBlockEnabled() {
    return wrapped.isCommandBlockEnabled();
  }

  @Override
  public boolean isSpawnerBlockEnabled() {
    return wrapped.isSpawnerBlockEnabled();
  }

  @Override
  public boolean tryAddFreshEntityWithPassengers(@NonNull Entity entity) {
    return wrapped.tryAddFreshEntityWithPassengers(entity);
  }

  @Override
  public void updateNeighborsAt(@NonNull BlockPos pos, @NonNull Block sourceBlock) {
    wrapped.updateNeighborsAt(pos, sourceBlock);
  }

  @Override
  public void neighborChanged(
      @NonNull BlockPos pos, @NonNull Block changedBlock, @Nullable Orientation orientation) {
    wrapped.neighborChanged(pos, changedBlock, orientation);
  }

  @Override
  public void broadcastEntityEvent(@NonNull Entity entity, byte event) {
    wrapped.broadcastEntityEvent(entity, event);
  }

  @Override
  public void broadcastDamageEvent(@NonNull Entity entity, @NonNull DamageSource source) {
    wrapped.broadcastDamageEvent(entity, source);
  }

  @Override
  public int getLogicalHeight() {
    return wrapped.getLogicalHeight();
  }

  @Override
  public void setMapData(@NonNull MapId id, @NonNull MapItemSavedData data) {
    wrapped.setMapData(id, data);
  }

  @Override
  public @NonNull MapId getFreeMapId() {
    return wrapped.getFreeMapId();
  }

  @Override
  public @NonNull ServerWaypointManager getWaypointManager() {
    return wrapped.getWaypointManager();
  }

  @Override
  public @NonNull PoiManager getPoiManager() {
    return wrapped.getPoiManager();
  }

  @Override
  public boolean isVillage(@NonNull BlockPos pos) {
    return wrapped.isVillage(pos);
  }

  @Override
  public @NonNull Raids getRaids() {
    return wrapped.getRaids();
  }

  @Override
  public @Nullable Raid getRaidAt(@NonNull BlockPos pos) {
    return wrapped.getRaidAt(pos);
  }

  @Override
  public boolean isRaided(@NonNull BlockPos pos) {
    return wrapped.isRaided(pos);
  }

  @Override
  public @NonNull String toString() {
    return "SimulatedServerLevelWrapped{" + wrapped.toString() + "}";
  }
}
