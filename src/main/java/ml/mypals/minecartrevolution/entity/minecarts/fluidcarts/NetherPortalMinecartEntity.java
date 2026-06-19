package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class NetherPortalMinecartEntity extends PortalMinecartEntity {

    private int teleportEventTimer = -1;
    private boolean wasActivated = false;

    private NetherPortalMinecartEntity cachedTarget = null;

    private static final int EVENT_DURATION = 60;   // 3 seconds
    private static final double EVENT_RADIUS = 5.0;

    public NetherPortalMinecartEntity(EntityType<? extends NetherPortalMinecartEntity> entityType, Level level) {
        super(entityType, level);
        this.setCustomDisplayBlockState(Optional.of(net.minecraft.world.level.block.Blocks.NETHER_PORTAL.defaultBlockState()));
    }

    public NetherPortalMinecartEntity(EntityType<? extends NetherPortalMinecartEntity> minecart, Level level, double x, double y, double z, Item item) {
        super(minecart, level, x, y, z, item);
        this.setCustomDisplayBlockState(Optional.of(net.minecraft.world.level.block.Blocks.NETHER_PORTAL.defaultBlockState()));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) return;
        if (!(level() instanceof ServerLevel serverLevel)) return;

        boolean risingEdge = !wasActivated && this.activated;
        wasActivated = this.activated;

        if (risingEdge && teleportEventTimer < 0) {
            startTeleportEvent();
        }

        if (teleportEventTimer < 0) return;

        if (teleportEventTimer < EVENT_DURATION) {
            tickEventCharging(serverLevel);
            teleportEventTimer++;
        } else {
            fireTeleportEvent(serverLevel);
            resetTeleportEvent();
        }
    }

    private void startTeleportEvent() {
        teleportEventTimer = 0;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.RESPAWN_ANCHOR_CHARGE, this.getSoundSource(), 2, 1.0F);

        PortalMinecartEntity found = findTargetPortal();
        cachedTarget = (found instanceof NetherPortalMinecartEntity n) ? n : null;

    }

    private void tickEventCharging(ServerLevel serverLevel) {
        float progress = (float) teleportEventTimer / EVENT_DURATION;

        this.noPhysics = true;
        this.setYRot(this.getYRot() + 2.0f + 4.0f * progress);

        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(50.0F);

        List<Entity> nearby = getNearbyEventEntities();
        for (Entity entity : nearby) {
            entity.noPhysics = true;
            entity.setDeltaMovement(Vec3.ZERO);

            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    entity.getRandomX(0.5),
                    entity.getRandomY(),
                    entity.getRandomZ(0.5),
                    10,
                    (entity.getRandom().nextDouble() - 0.5) * 2.0,
                    -entity.getRandom().nextDouble(),
                    (entity.getRandom().nextDouble() - 0.5) * 2.0,
                    1);
        }

        serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getRandomX(0.5),
                this.getRandomY(),
                this.getRandomZ(0.5),
                10,
                (this.getRandom().nextDouble() - 0.5) * 2.0,
                -this.getRandom().nextDouble(),
                (this.getRandom().nextDouble() - 0.5) * 2.0,
                1);

        if (cachedTarget != null && !cachedTarget.isRemoved() && teleportEventTimer % 5 == 0) {

            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    cachedTarget.getRandomX(0.5),
                    cachedTarget.getRandomY(),
                    cachedTarget.getRandomZ(0.5),
                    8,
                    (cachedTarget.getRandom().nextDouble() - 0.5) * 2.0,
                    -cachedTarget.getRandom().nextDouble(),
                    (cachedTarget.getRandom().nextDouble() - 0.5) * 2.0,
                    1);
            cachedTarget.setHurtDir(-cachedTarget.getHurtDir());
            cachedTarget.setHurtTime(5);
            cachedTarget.setDamage(30.0F);
        }

        if (teleportEventTimer == (int)(EVENT_DURATION * 0.8f)) {
            serverLevel.playSound(null, this.blockPosition(),
                    SoundEvents.ENDERMAN_AMBIENT,
                    this.getSoundSource(), 0.5f, 0.7F);
        }
        onChargingTick(serverLevel, teleportEventTimer, progress);
    }

    private void fireTeleportEvent(ServerLevel serverLevel) {
        if (cachedTarget == null || cachedTarget.isRemoved()) {
            PortalMinecartEntity found = findTargetPortal();
            cachedTarget = (found instanceof NetherPortalMinecartEntity n) ? n : null;
        }
        NetherPortalMinecartEntity netherTarget = cachedTarget;
        if (netherTarget == null) return;


        onFireTeleportEvent(serverLevel, netherTarget);

        Vec3 myPos     = this.position();
        Vec3 targetPos = netherTarget.position();

        List<Entity> sourceEntities = getNearbyEventEntities();
        List<Entity> targetEntities = netherTarget.getNearbyEventEntities();
        targetEntities.removeAll(sourceEntities);
        for (Entity entity : sourceEntities) {
            entity.noPhysics = false;
            entity.setNoGravity(false);
            if (entity.getPortalCooldown() > 0) continue;

            Vec3 offset      = entity.position().subtract(myPos);
            Vec3 destination = targetPos.add(offset);

            level().gameEvent(GameEvent.TELEPORT, entity.position(), GameEvent.Context.of(entity));
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 0.8F, 1.0F);
            entity.teleportTo(destination.x, destination.y, destination.z);
            Vec3 v = entity.getKnownSpeed();
            entity.setDeltaMovement(v.x, -v.y, v.z);
            entity.fallDistance = 0;
            entity.hurtMarked = true;
            entity.setPortalCooldown(PORTAL_COOLDOWN);
        }

        for (Entity entity : targetEntities) {
            if (entity.getPortalCooldown() > 0) continue;

            Vec3 offset      = entity.position().subtract(targetPos);
            Vec3 destination = myPos.add(offset);

            level().gameEvent(GameEvent.TELEPORT, entity.position(), GameEvent.Context.of(entity));
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, netherTarget.getSoundSource(), 0.8F, 1.0F);
            entity.teleportTo(destination.x, destination.y + netherTarget.getBbHeight(), destination.z);
            Vec3 v = entity.getKnownSpeed();
            entity.setDeltaMovement(v.x, -v.y, v.z);
            entity.fallDistance = 0;
            entity.hurtMarked = true;
            entity.setPortalCooldown(PORTAL_COOLDOWN);
        }

        serverLevel.playSound(null, netherTarget.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, netherTarget.getSoundSource(), 1.0F, 0.9F);
        serverLevel.playSound(null, this.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 0.9F);
        playPortalDamageAnimation(this);
        playPortalDamageAnimation(netherTarget);

    }

    private void resetTeleportEvent() {
        teleportEventTimer = -1;
        cachedTarget = null;
        this.noPhysics = false;
        this.activated = false;
        this.wasActivated = false;
        this.setDeltaMovement(Vec3.ZERO);
        this.setPortalCooldown(PORTAL_COOLDOWN * 4);
        onResetTeleportEvent();
    }

    protected void onChargingTick(Level level, int timer, float progress) {}
    protected void onFireTeleportEvent(ServerLevel serverLevel, NetherPortalMinecartEntity netherTarget) {
        serverLevel.playSound(null, this.blockPosition(),
                SoundEvents.PORTAL_TRAVEL, this.getSoundSource(), 1.0F, 0.8F);
        serverLevel.playSound(null, netherTarget.blockPosition(),
                SoundEvents.PORTAL_TRAVEL, netherTarget.getSoundSource(), 1.0F, 0.8F);
    }


    protected void onResetTeleportEvent() {}


    @Override
    protected boolean isTransferBlocked() {
        return super.isTransferBlocked() || teleportEventTimer >= 0;
    }

    @Override
    protected boolean isCooldownActive(PortalMinecartEntity targetPortal) {
        return false;
    }

    @Override
    protected void handleTeleportation(PortalMinecartEntity targetPortal) {
        super.handleTeleportation(targetPortal);
    }

    @Override
    protected void handleEntityTeleport(Entity entity, PortalMinecartEntity targetPortal) {
        if (targetPortal instanceof NetherPortalMinecartEntity netherTarget) {
            teleportEntity(entity, netherTarget);
        }
    }

    protected void teleportEntity(Entity entity, NetherPortalMinecartEntity targetPortal) {
        if (entity.getPortalCooldown() > 0) {
            return;
        }

        this.level().gameEvent(GameEvent.TELEPORT, entity.position(), GameEvent.Context.of(entity));
        this.level().playSound(null, entity.xo, entity.yo, entity.zo, SoundEvents.ENDERMAN_TELEPORT,
                this.getSoundSource(), 1.0F, 1.0F);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

        Vec3 relativePos = entity.position().subtract(position());
        Vec3 destination = targetPortal.position().add(relativePos);

        entity.teleportTo(0, 0, 0);
        entity.teleportTo(destination.x, destination.y + getBbHeight(), destination.z);

        Vec3 velocity = entity.getKnownSpeed();
        entity.setDeltaMovement(velocity.x, -velocity.y, velocity.z);

        entity.setOnGround(false);
        entity.fallDistance = 0;
        entity.hurtMarked = true;
        entity.setPortalCooldown(PORTAL_COOLDOWN);
        playPortalDamageAnimation(this);
        playPortalDamageAnimation(targetPortal);
    }


    protected List<Entity> getNearbyEventEntities() {
        AABB searchBox = this.getBoundingBox().inflate(EVENT_RADIUS);
        return level().getEntities(this, searchBox, e -> e != this && !e.isRemoved());
    }
}