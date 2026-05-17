package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ml.mypals.minecartrevolution.mixin.blocks.PistonBlockInvoker;

import java.util.ArrayList;
import java.util.List;

public class EnderPortalMinecartEntity extends NetherPortalMinecartEntity {


    private static final int BLOCK_FLAG_REMOVE_SOURCE = Block.UPDATE_CLIENTS | 256;
    private static final int BLOCK_FLAG_SET_TARGET = Block.UPDATE_CLIENTS | 256 | 512 | 128;

    private static final int  AREA_RADIUS    = 5;
    private static final int  AREA_HEIGHT    = 2;
    private static final int  EXTRACT_PERIOD = 2;
    private static final int  EXTRACT_COUNT  = 3;

    private final List<PortalFallingBlockEntity> animFallingBlocks = new ArrayList<>();

    public EnderPortalMinecartEntity(EntityType<EnderPortalMinecartEntity> entityType, Level level) {
        super(entityType, level);
    }

    public EnderPortalMinecartEntity(EntityType<EnderPortalMinecartEntity> minecart, Level level,
                                     double x, double y, double z, Item item) {
        super(minecart, level, x, y, z, item);
    }

    @Override
    public void tick() {

        super.tick();
    }


    @Override
    protected void onChargingTick(ServerLevel serverLevel, int timer, float progress) {
        super.onChargingTick(serverLevel, timer, progress);

        if (timer % EXTRACT_PERIOD == 0) {
            extractRandomBlocks(serverLevel, EXTRACT_COUNT);
        }

        Vec3 center = this.position();
        for (PortalFallingBlockEntity fbe : animFallingBlocks) {
            if (!fbe.isRemoved()) {
                fbe.setTargetPos(center);
            }
        }
    }

    @Override
    protected void onFireTeleportEvent(ServerLevel serverLevel, NetherPortalMinecartEntity netherTarget) {
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.END_PORTAL_SPAWN, this.getSoundSource(), 2F, 1.0F);
        serverLevel.playSound(null, netherTarget.getX(), netherTarget.getY(), netherTarget.getZ(), SoundEvents.END_PORTAL_SPAWN, this.getSoundSource(), 2F, 1.0F);
        if (netherTarget instanceof EnderPortalMinecartEntity enderTarget) {
            swapAreaBlocks(serverLevel, enderTarget);
        }
        for (PortalFallingBlockEntity fbe : animFallingBlocks) {
            fbe.discard();
        }
        animFallingBlocks.clear();
    }

    @Override
    protected void onResetTeleportEvent() {
        for (PortalFallingBlockEntity fbe : animFallingBlocks) {
            fbe.discard();
        }
        animFallingBlocks.clear();
    }

    private void extractRandomBlocks(ServerLevel serverLevel, int maxCount) {
        BlockPos center = this.blockPosition();
        Vec3 cartPos    = this.position();
        int  extracted  = 0;

        for (int attempt = 0; attempt < 60 && extracted < maxCount; attempt++) {
            int dx = this.random.nextIntBetweenInclusive(-AREA_RADIUS, AREA_RADIUS);
            int dy = this.random.nextIntBetweenInclusive(-AREA_HEIGHT, AREA_HEIGHT);
            int dz = this.random.nextIntBetweenInclusive(-AREA_RADIUS, AREA_RADIUS);

            BlockPos worldPos = center.offset(dx, dy, dz);
            BlockState state  = serverLevel.getBlockState(worldPos);

            if (!isExtractable(state, serverLevel, worldPos)) continue;

            double bx = worldPos.getX() + 0.5;
            double by = worldPos.getY() + 0.5;
            double bz = worldPos.getZ() + 0.5;
            PortalFallingBlockEntity fbe = new PortalFallingBlockEntity(
                    serverLevel, bx, by, bz, state, cartPos);
            serverLevel.addFreshEntity(fbe);
            animFallingBlocks.add(fbe);
            extracted++;
        }
    }

    private boolean isExtractable(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false; // unbreakable (bedrock etc.)
        if (!PistonBaseBlock.isPushable(state, level, pos, Direction.UP, false, Direction.UP)) return false;

        if (!state.isSolidRender()) return false;
        return true;
    }


    private record BlockSnapshot(BlockState state, CompoundTag beData) {}

    private void swapAreaBlocks(ServerLevel serverLevel, EnderPortalMinecartEntity target) {
        BlockPos srcCenter = this.blockPosition();
        BlockPos dstCenter = target.blockPosition();

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

    private BlockSnapshot snapshot(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity be   = level.getBlockEntity(pos);
        CompoundTag data = (be != null) ? be.saveWithoutMetadata(level.registryAccess()) : null;
        return new BlockSnapshot(state, data);
    }

    private void placeSnapshot(ServerLevel level, BlockPos pos, BlockSnapshot snap) {
        level.setBlock(pos, snap.state(), BLOCK_FLAG_SET_TARGET, 0);
        if (snap.beData() != null && snap.state().getBlock() instanceof BaseEntityBlock base) {
            BlockEntity newBE = base.newBlockEntity(pos, snap.state());
            if (newBE != null) {
                newBE.loadWithComponents(
                        TagValueInput.create(ProblemReporter.DISCARDING,
                                level.registryAccess(), snap.beData()));
                level.setBlockEntity(newBE);
            }
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