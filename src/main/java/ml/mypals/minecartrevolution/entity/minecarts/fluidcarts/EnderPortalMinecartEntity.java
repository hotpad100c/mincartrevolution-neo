package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import java.util.Optional;
import ml.mypals.minecartrevolution.packets.EnderPortalShakePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class EnderPortalMinecartEntity extends NetherPortalMinecartEntity {

  private static final int SHAKE_DURATION = 60;
  private static final float SHAKE_INTENSITY = 0.3f;
  private static final double SHAKE_RADIUS = 5;

  public EnderPortalMinecartEntity(EntityType<EnderPortalMinecartEntity> entityType, Level level) {
    super(entityType, level);
    this.setCustomDisplayBlockState(Optional.of(Blocks.END_PORTAL.defaultBlockState()));
  }

  public EnderPortalMinecartEntity(
      EntityType<EnderPortalMinecartEntity> minecart,
      Level level,
      double x,
      double y,
      double z,
      Item item) {
    super(minecart, level, x, y, z, item);
    this.setCustomDisplayBlockState(Optional.of(Blocks.END_PORTAL.defaultBlockState()));
  }

  @Override
  public void tick() {
    super.tick();
  }

  @Override
  protected void onChargingTick(Level level, int timer, float progress) {
    super.onChargingTick(level, timer, progress);

    if (timer == 0 && level instanceof ServerLevel serverLevel) {
      sendShakePacketToNearby(serverLevel);
    }
  }

  @Override
  protected void onFireTeleportEvent(
      ServerLevel serverLevel, NetherPortalMinecartEntity netherTarget) {
    serverLevel.playSound(
        null,
        getX(),
        getY(),
        getZ(),
        SoundEvents.END_PORTAL_SPAWN,
        this.getSoundSource(),
        2F,
        1.0F);
    serverLevel.playSound(
        null,
        netherTarget.getX(),
        netherTarget.getY(),
        netherTarget.getZ(),
        SoundEvents.END_PORTAL_SPAWN,
        this.getSoundSource(),
        2F,
        1.0F);

    if (netherTarget instanceof EnderPortalMinecartEntity enderTarget) {
      swapAreaBlocks(serverLevel, enderTarget);
    }
  }

  private static final int BLOCK_FLAG_SET_TARGET = Block.UPDATE_CLIENTS | 256 | 512 | 128;

  private static final int AREA_RADIUS = 5;
  private static final int AREA_HEIGHT = 2;

  private record BlockSnapshot(
      net.minecraft.nbt.CompoundTag beData,
      net.minecraft.world.level.block.state.BlockState state) {}

  private void swapAreaBlocks(ServerLevel serverLevel, EnderPortalMinecartEntity target) {
    net.minecraft.core.BlockPos srcCenter = this.blockPosition();
    net.minecraft.core.BlockPos dstCenter = target.blockPosition();

    int size = AREA_RADIUS * 2 + 1;
    int hsize = AREA_HEIGHT * 2 + 1;
    BlockSnapshot[][][] srcSnaps = new BlockSnapshot[size][hsize][size];
    BlockSnapshot[][][] dstSnaps = new BlockSnapshot[size][hsize][size];

    for (int dx = -AREA_RADIUS; dx <= AREA_RADIUS; dx++) {
      for (int dy = -AREA_HEIGHT; dy <= AREA_HEIGHT; dy++) {
        for (int dz = -AREA_RADIUS; dz <= AREA_RADIUS; dz++) {
          int xi = dx + AREA_RADIUS, yi = dy + AREA_HEIGHT, zi = dz + AREA_RADIUS;
          srcSnaps[xi][yi][zi] = snapshot(serverLevel, srcCenter.offset(dx, dy, dz));
          dstSnaps[xi][yi][zi] = snapshot(serverLevel, dstCenter.offset(dx, dy, dz));
        }
      }
    }

    for (int dx = -AREA_RADIUS; dx <= AREA_RADIUS; dx++) {
      for (int dy = -AREA_HEIGHT; dy <= AREA_HEIGHT; dy++) {
        for (int dz = -AREA_RADIUS; dz <= AREA_RADIUS; dz++) {
          int xi = dx + AREA_RADIUS, yi = dy + AREA_HEIGHT, zi = dz + AREA_RADIUS;
          placeSnapshot(serverLevel, srcCenter.offset(dx, dy, dz), dstSnaps[xi][yi][zi]);
          placeSnapshot(serverLevel, dstCenter.offset(dx, dy, dz), srcSnaps[xi][yi][zi]);
        }
      }
    }

    serverLevel.gameEvent(GameEvent.TELEPORT, srcCenter, GameEvent.Context.of(this));
  }

  private BlockSnapshot snapshot(ServerLevel level, net.minecraft.core.BlockPos pos) {
    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
    net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
    net.minecraft.nbt.CompoundTag data =
        (be != null) ? be.saveWithoutMetadata(level.registryAccess()) : null;
    return new BlockSnapshot(data, state);
  }

  private void placeSnapshot(
      ServerLevel level, net.minecraft.core.BlockPos pos, BlockSnapshot snap) {
    level.setBlock(pos, snap.state(), BLOCK_FLAG_SET_TARGET, 0);
    if (snap.beData() != null
        && snap.state().getBlock()
            instanceof net.minecraft.world.level.block.BaseEntityBlock base) {
      net.minecraft.world.level.block.entity.BlockEntity newBE =
          base.newBlockEntity(pos, snap.state());
      if (newBE != null) {
        newBE.loadWithComponents(
            net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING,
                level.registryAccess(),
                snap.beData()));
        level.setBlockEntity(newBE);
      }
    }
  }

  private void sendShakePacketToNearby(ServerLevel serverLevel) {
    EnderPortalShakePacket packet = new EnderPortalShakePacket(SHAKE_DURATION, SHAKE_INTENSITY);
    net.minecraft.world.phys.AABB box = this.getBoundingBox().inflate(SHAKE_RADIUS);
    for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, box)) {
      PacketDistributor.sendToPlayer(player, packet);
    }
  }

  @Override
  protected boolean isTransferBlocked() {
    return super.isTransferBlocked();
  }

  @Override
  protected boolean isCooldownActive(PortalMinecartEntity targetPortal) {
    boolean hasCooldown = super.isCooldownActive(targetPortal);
    return hasCooldown && this.getDeltaMovement().lengthSqr() < 0.1;
  }
}
