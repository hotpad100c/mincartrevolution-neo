package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.mixin.simulation.ClientLevelAccessor;
import ml.mypals.minecartrevolution.mixin.simulation.LevelAccessor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.ClientClockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelDataManager;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SimulatedClientLevel extends ClientLevel implements SimulatedLevel {
  private final ClientLevel wrapped;
  private final CompatFriendlyBlockMinecartEntity minecart;

  public SimulatedClientLevel(ClientLevel wrapped, CompatFriendlyBlockMinecartEntity minecart) {
    this.wrapped = wrapped;
    this.minecart = minecart;
    super(
        ((ClientLevelAccessor) wrapped).minecartRevolution$getConnection(),
        wrapped.getLevelData(),
        wrapped.dimension(),
        wrapped.dimensionTypeRegistration(),
        0,
        0,
        Minecraft.getInstance().levelRenderer,
        wrapped.isDebug(),
        0,
        wrapped.getSeaLevel());
  }

  public Level getLevel() {
    return wrapped;
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
    if (wrapped != null) wrapped.setRespawnData(respawnData);
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
  public @NonNull List<PartEntity<?>> dragonParts() {
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
    return ((LevelAccessor) this.wrapped).minecartRevolution$getEntities();
  }

  @Override
  public @NonNull ClientClockManager clockManager() {
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
  public @NonNull ClientChunkCache getChunkSource() {
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
  public @NonNull List<AbstractClientPlayer> players() {
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

  public void addParticle(
      @NonNull ParticleOptions particle,
      double x,
      double y,
      double z,
      double xd,
      double yd,
      double zd) {
    wrapped.addParticle(
        particle, particle.getType().getOverrideLimiter(), false, x, y, z, xd, yd, zd);
  }

  public void addParticle(
      @NonNull ParticleOptions particle,
      boolean overrideLimiter,
      boolean alwaysShow,
      double x,
      double y,
      double z,
      double xd,
      double yd,
      double zd) {
    wrapped.addParticle(
        particle,
        particle.getType().getOverrideLimiter() || overrideLimiter,
        alwaysShow,
        x,
        y,
        z,
        xd,
        yd,
        zd);
  }

  public void addAlwaysVisibleParticle(
      @NonNull ParticleOptions particle,
      double x,
      double y,
      double z,
      double xd,
      double yd,
      double zd) {
    wrapped.addAlwaysVisibleParticle(particle, x, y, z, xd, yd, zd);
  }

  public void addAlwaysVisibleParticle(
      @NonNull ParticleOptions particle,
      boolean overrideLimiter,
      double x,
      double y,
      double z,
      double xd,
      double yd,
      double zd) {
    wrapped.addAlwaysVisibleParticle(particle, overrideLimiter, x, y, z, xd, yd, zd);
  }

  public void tick(@NonNull BooleanSupplier haveTime) {
    wrapped.tick(haveTime);
  }

  @Override
  public int getSignal(@NotNull BlockPos pos, @NotNull Direction direction) {
    return minecart.activated ? 15 : 0;
  }

  @Override
  public void addEntity(@NonNull Entity entity) {
    wrapped.addEntity(entity);
  }

  @Override
  public void removeEntity(int id, Entity.@NonNull RemovalReason reason) {
    wrapped.removeEntity(id, reason);
  }

  @Override
  public void playLocalSound(
      @NonNull Entity sourceEntity,
      @NonNull SoundEvent sound,
      @NonNull SoundSource source,
      float volume,
      float pitch) {
    wrapped.playLocalSound(sourceEntity, sound, source, volume, pitch);
  }

  @Override
  public void playPlayerSound(
      @NonNull SoundEvent sound, @NonNull SoundSource source, float volume, float pitch) {
    wrapped.playPlayerSound(sound, source, volume, pitch);
  }

  @Override
  public void playLocalSound(
      double x,
      double y,
      double z,
      @NonNull SoundEvent sound,
      @NonNull SoundSource source,
      float volume,
      float pitch,
      boolean distanceDelay) {
    wrapped.playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay);
  }

  @Override
  public @NonNull ClientLevelData getLevelData() {
    return wrapped.getLevelData();
  }

  @Override
  public int getEntityCount() {
    return wrapped.getEntityCount();
  }

  @Override
  public void disconnect(@NonNull Component message) {
    wrapped.disconnect(message);
  }

  @Override
  public void setServerSimulationDistance(int serverSimulationDistance) {
    wrapped.setServerSimulationDistance(serverSimulationDistance);
  }

  @Override
  public int getServerSimulationDistance() {
    return wrapped.getServerSimulationDistance();
  }

  @Override
  public void sendPacketToServer(@NonNull Packet<?> packet) {
    wrapped.sendPacketToServer(packet);
  }

  @Override
  public @NonNull ModelDataManager getModelDataManager() {
    return wrapped.getModelDataManager();
  }

  @Override
  public @NonNull ModelData getModelData(@NonNull BlockPos pos) {
    return wrapped.getModelData(pos);
  }

  @Override
  public void trackExplosionEffects(
      @NonNull Vec3 center,
      float radius,
      int blockCount,
      @NonNull WeightedList<ExplosionParticleInfo> blockParticles) {
    wrapped.trackExplosionEffects(center, radius, blockCount, blockParticles);
  }

  @Override
  public void clearTintCaches() {
    wrapped.clearTintCaches();
  }

  @Override
  public void animateTick(int xt, int yt, int zt) {
    wrapped.animateTick(xt, yt, zt);
  }

  @Override
  public void unload(@NonNull LevelChunk levelChunk) {
    wrapped.unload(levelChunk);
  }

  @Override
  public void onChunkLoaded(@NonNull ChunkPos pos) {
    wrapped.onChunkLoaded(pos);
  }

  @Override
  public void tickEntities() {
    wrapped.tickEntities();
  }

  @Override
  public @NonNull Iterable<Entity> entitiesForRendering() {
    return wrapped.entitiesForRendering();
  }

  @Override
  public void createFireworks(
      double x,
      double y,
      double z,
      double xd,
      double yd,
      double zd,
      @NonNull List<FireworkExplosion> explosions) {
    wrapped.createFireworks(x, y, z, xd, yd, zd, explosions);
  }

  @Override
  public @NonNull CrashReportCategory fillReportDetails(@NonNull CrashReport report) {
    return wrapped.fillReportDetails(report);
  }

  @Override
  public @NonNull String toString() {
    return wrapped.toString();
  }

  @Override
  public void update() {
    wrapped.update();
  }

  @Override
  public void setBlocksDirty(
      @NonNull BlockPos pos, @NonNull BlockState oldState, @NonNull BlockState newState) {
    wrapped.setBlocksDirty(pos, oldState, newState);
  }

  @Override
  public void setSectionDirtyWithNeighbors(int chunkX, int chunkY, int chunkZ) {
    wrapped.setSectionDirtyWithNeighbors(chunkX, chunkY, chunkZ);
  }

  @Override
  public void setSectionRangeDirty(
      int minSectionX,
      int minSectionY,
      int minSectionZ,
      int maxSectionX,
      int maxSectionY,
      int maxSectionZ) {
    wrapped.setSectionRangeDirty(
        minSectionX, minSectionY, minSectionZ, maxSectionX, maxSectionY, maxSectionZ);
  }

  @Override
  public void globalLevelEvent(int type, @NonNull BlockPos pos, int data) {
    wrapped.globalLevelEvent(type, pos, data);
  }

  @Override
  public void addDestroyBlockEffect(@NonNull BlockPos pos, @NonNull BlockState blockState) {
    wrapped.addDestroyBlockEffect(pos, blockState);
  }

  @Override
  public void addBreakingBlockEffect(
      @NonNull BlockPos pos, @NonNull Direction direction, @Nullable HitResult hitResult) {
    wrapped.addBreakingBlockEffect(pos, direction, hitResult);
  }

  @Override
  public int getBlockTint(@NonNull BlockPos pos, @NonNull ColorResolver resolver) {
    return wrapped.getBlockTint(pos, resolver);
  }

  @Override
  public int calculateBlockTint(@NonNull BlockPos pos, @NonNull ColorResolver colorResolver) {
    return wrapped.calculateBlockTint(pos, colorResolver);
  }

  @Override
  public @NonNull CardinalLighting cardinalLighting() {
    return wrapped.cardinalLighting();
  }

  @Override
  public boolean isTickingEntity(@NonNull Entity entity) {
    return wrapped.isTickingEntity(entity);
  }

  @Override
  public boolean shouldTickDeath(@NonNull Entity entity) {
    return wrapped.shouldTickDeath(entity);
  }

  @Override
  public void queueLightUpdate(@NonNull Runnable update) {
    wrapped.queueLightUpdate(update);
  }

  @Override
  public void pollLightUpdates() {
    wrapped.pollLightUpdates();
  }

  @Override
  public @Nullable EndFlashState endFlashState() {
    return wrapped.endFlashState();
  }

  @Override
  public void setSkyFlashTime(int skyFlashTime) {
    wrapped.setSkyFlashTime(skyFlashTime);
  }
}
