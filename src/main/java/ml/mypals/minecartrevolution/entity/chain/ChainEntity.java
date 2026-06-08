package ml.mypals.minecartrevolution.entity.chain;

import ml.mypals.minecartrevolution.manager.MinecartChainManager;
import ml.mypals.minecartrevolution.packets.ChainSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChainEntity extends Entity {

    private static final double GRAVITY = 0.04;
    private static final double DAMPING = 0.96;
    private static final int CONSTRAINT_ITERATIONS = 3;
    private static final double TARGET_SEGMENT_SPACING = 0.3;
    private static final int MIN_SEGMENTS = 3;
    private static final int MAX_SEGMENTS = 40;
    private static final double MAX_DISTANCE_SQ = 1024.0;
    private static final int TAUT_SEGMENT_LIMIT = 15;
    private static final double TAUT_LENGTH = (TAUT_SEGMENT_LIMIT - 1) * TARGET_SEGMENT_SPACING;

    private final List<ChainSegment> segments = new ArrayList<>();

    private UUID minecartAUuid;
    private UUID minecartBUuid;
    private AbstractMinecart cachedCartA;
    private AbstractMinecart cachedCartB;
    private List<Vec3> lastSyncedPositions = List.of();
    private int cacheTick = -1;

    public List<Vec3> clientSegments = List.of();

    public ChainEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public ChainEntity(EntityType<?> type, Level level, UUID cartA, UUID cartB) {
        this(type, level);
        this.minecartAUuid = cartA;
        this.minecartBUuid = cartB;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
    }

    public UUID getMinecartAUuid() {
        return minecartAUuid;
    }

    public UUID getMinecartBUuid() {
        return minecartBUuid;
    }

    public void setMinecartA(UUID uuid) {
        this.minecartAUuid = uuid;
    }

    public void setMinecartB(UUID uuid) {
        this.minecartBUuid = uuid;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        AbstractMinecart cartA = findMinecart(minecartAUuid);
        AbstractMinecart cartB = findMinecart(minecartBUuid);

        if (cartA == null || cartB == null || cartA == cartB) {
            breakChain();
            return;
        }

        if (cartA.position().distanceToSqr(cartB.position()) > MAX_DISTANCE_SQ) {
            breakChain();
            return;
        }

        if (!cartA.isAlive() || !cartB.isAlive()) {
            breakChain();
            return;
        }

        simulatePhysics(cartA, cartB);
        updateBounds();
        syncIfNeeded();
    }

    private void simulatePhysics(AbstractMinecart cartA, AbstractMinecart cartB) {
        Vec3 attachA = getAttachmentPoint(cartA);
        Vec3 attachB = getAttachmentPoint(cartB);
        double distance = attachA.distanceTo(attachB);
        int count = Math.clamp((int) (distance / TARGET_SEGMENT_SPACING) + 1, TAUT_SEGMENT_LIMIT, MAX_SEGMENTS);
        double spacing = distance / Math.max(count - 1, 1);

        ensureSegmentCount(count, attachA, attachB, distance);

        for (ChainSegment seg : segments) {
            Vec3 vel = seg.position.subtract(seg.oldPosition).scale(DAMPING);
            seg.oldPosition = seg.position;
            seg.position = seg.position.add(vel.x, vel.y - GRAVITY, vel.z);
        }

        for (int iter = 0; iter < CONSTRAINT_ITERATIONS; iter++) {
            for (int i = 0; i < segments.size() - 1; i++) {
                ChainSegment a = segments.get(i);
                ChainSegment b = segments.get(i + 1);
                Vec3 delta = b.position.subtract(a.position);
                double dist = delta.length();
                if (dist < 0.0001) continue;
                double correction = (dist - TARGET_SEGMENT_SPACING) / dist * 0.5;
                Vec3 corr = delta.scale(correction);
                if (i > 0) a.position = a.position.add(corr);
                if (i < segments.size() - 2) b.position = b.position.subtract(corr);
            }
            segments.getFirst().position = attachA;
            segments.getLast().position = attachB;
        }

        applyPullingForce(cartA, cartB, attachA, attachB, spacing);

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (ChainSegment seg : segments) {
            double sx = seg.position.x;
            double sy = seg.position.y;
            double sz = seg.position.z;
            double ox = seg.oldPosition.x;
            double oy = seg.oldPosition.y;
            double oz = seg.oldPosition.z;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        mpos.set((int) Math.floor(sx) + dx, (int) Math.floor(sy) + dy, (int) Math.floor(sz) + dz);
                        BlockState state = level().getBlockState(mpos);
                        if (state.isAir()) continue;
                        VoxelShape shape = state.getCollisionShape(level(), mpos);
                        if (shape.isEmpty()) continue;
                        AABB blockBox = shape.bounds().move(mpos);

                        boolean inside = sx >= blockBox.minX && sx <= blockBox.maxX
                                && sy >= blockBox.minY && sy <= blockBox.maxY
                                && sz >= blockBox.minZ && sz <= blockBox.maxZ;
                        if (!inside) continue;

                        double margin = 0.02;
                        double nx = sx, ny = sy, nz = sz;

                        if (oy > blockBox.maxY) ny = blockBox.maxY + margin;
                        else if (oy < blockBox.minY) ny = blockBox.minY - margin;

                        if (ox > blockBox.maxX) nx = blockBox.maxX + margin;
                        else if (ox < blockBox.minX) nx = blockBox.minX - margin;

                        if (oz > blockBox.maxZ) nz = blockBox.maxZ + margin;
                        else if (oz < blockBox.minZ) nz = blockBox.minZ - margin;

                        seg.position = new Vec3(nx, ny, nz);
                        sx = nx; sy = ny; sz = nz;
                    }
                }
            }
        }
    }

    private void applyPullingForce(AbstractMinecart cartA, AbstractMinecart cartB,
                                     Vec3 attachA, Vec3 attachB, double spacing) {
        double actualDistance = attachA.distanceTo(attachB);
        if (actualDistance > TAUT_LENGTH) {
            double excess = actualDistance - TAUT_LENGTH;
            double springForce = Math.min(excess * 0.08, 0.3);
            Vec3 pullDir = attachB.subtract(attachA).normalize();
            Vec3 relativeVel = cartB.getDeltaMovement().subtract(cartA.getDeltaMovement());
            double approachSpeed = relativeVel.dot(pullDir);
            double damping = approachSpeed * 0.15;
            double totalForce = Math.max(springForce - damping, 0);
            cartA.addDeltaMovement(pullDir.scale(totalForce));
            cartB.addDeltaMovement(pullDir.scale(-totalForce));
        }
    }

    private void ensureSegmentCount(int count, Vec3 attachA, Vec3 attachB, double distance) {
        if (segments.size() == count) return;
        if (segments.isEmpty()) {
            for (int i = 0; i < count; i++) {
                double t = (double) i / Math.max(count - 1, 1);
                Vec3 pos = attachA.lerp(attachB, t);
                double sag = Math.sin(t * Math.PI) * Math.min(distance * 0.15, 1.2);
                pos = pos.add(0, -sag, 0);
                ChainSegment seg = new ChainSegment();
                seg.position = pos;
                seg.oldPosition = pos;
                segments.add(seg);
            }
            return;
        }
        if (count < segments.size()) {
            segments.subList(count, segments.size()).clear();
        } else {
            ChainSegment last = segments.getLast();
            for (int i = segments.size(); i < count; i++) {
                ChainSegment seg = new ChainSegment();
                seg.position = last.position;
                seg.oldPosition = last.position;
                segments.add(seg);
            }
        }
    }

    private Vec3 getAttachmentPoint(AbstractMinecart cart) {
        return cart.position().add(0, cart.getBbHeight() * 0.75, 0);
    }

    private AbstractMinecart findMinecart(UUID uuid) {
        if (uuid == null) return null;
        if (level() instanceof ServerLevel sl) {
            Entity entity = sl.getEntity(uuid);
            if (entity instanceof AbstractMinecart cart && cart.isAlive()) return cart;
        }
        return null;
    }

    private void syncIfNeeded() {
        List<Vec3> current = segments.stream().map(s -> s.position).toList();
        if (lastSyncedPositions.size() != current.size()) {
            doSync(current);
            return;
        }
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).distanceToSqr(lastSyncedPositions.get(i)) > 0.0001) {
                doSync(current);
                return;
            }
        }
    }

    private void doSync(List<Vec3> positions) {
        lastSyncedPositions = List.copyOf(positions);
        PacketDistributor.sendToPlayersTrackingEntity(this, new ChainSyncPacket(getId(), positions));
    }

    private void updateBounds() {
        if (segments.isEmpty()) return;
        Vec3 first = segments.getFirst().position;
        Vec3 last = segments.getLast().position;
        double minX = Math.min(first.x, last.x) - 0.5;
        double minY = Math.min(first.y, last.y) - 1.5;
        double minZ = Math.min(first.z, last.z) - 0.5;
        double maxX = Math.max(first.x, last.x) + 0.5;
        double maxY = Math.max(first.y, last.y) + 0.5;
        double maxZ = Math.max(first.z, last.z) + 0.5;
        for (ChainSegment seg : segments) {
            minX = Math.min(minX, seg.position.x);
            minY = Math.min(minY, seg.position.y);
            minZ = Math.min(minZ, seg.position.z);
            maxX = Math.max(maxX, seg.position.x);
            maxY = Math.max(maxY, seg.position.y);
            maxZ = Math.max(maxZ, seg.position.z);
        }
        setPos((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        setBoundingBox(new AABB(
                minX - getX(), minY - getY(), minZ - getZ(),
                maxX - getX(), maxY - getY(), maxZ - getZ()
        ));
    }

    private void breakChain() {
        MinecartChainManager.onChainBroken(this);
        discard();
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean hurtClient(@NonNull DamageSource source) {
        return false;
    }

    @Override
    public void remove(@NonNull RemovalReason reason) {
        super.remove(reason);
        if (!level().isClientSide()) {
            MinecartChainManager.onChainRemoved(this);
        }
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        long mostA = input.getLongOr("minecartAUuidMost", 0L);
        long leastA = input.getLongOr("minecartAUuidLeast", 0L);
        if (mostA != 0 || leastA != 0) minecartAUuid = new UUID(mostA, leastA);
        long mostB = input.getLongOr("minecartBUuidMost", 0L);
        long leastB = input.getLongOr("minecartBUuidLeast", 0L);
        if (mostB != 0 || leastB != 0) minecartBUuid = new UUID(mostB, leastB);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        if (minecartAUuid != null) {
            output.putLong("minecartAUuidMost", minecartAUuid.getMostSignificantBits());
            output.putLong("minecartAUuidLeast", minecartAUuid.getLeastSignificantBits());
        }
        if (minecartBUuid != null) {
            output.putLong("minecartBUuidMost", minecartBUuid.getMostSignificantBits());
            output.putLong("minecartBUuidLeast", minecartBUuid.getLeastSignificantBits());
        }
    }

    @Override
    public boolean canCollideWith(@NonNull Entity entity) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public boolean linksMinecart(UUID uuid) {
        return uuid != null && (uuid.equals(minecartAUuid) || uuid.equals(minecartBUuid));
    }

    private static class ChainSegment {
        Vec3 position;
        Vec3 oldPosition;
    }
}
