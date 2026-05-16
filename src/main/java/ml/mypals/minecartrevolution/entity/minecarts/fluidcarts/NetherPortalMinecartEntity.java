package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class NetherPortalMinecartEntity extends PortalMinecartEntity {

    public NetherPortalMinecartEntity(EntityType<? extends NetherPortalMinecartEntity> entityType, Level level) {
        super(entityType, level);
    }

    public NetherPortalMinecartEntity(
            EntityType<? extends NetherPortalMinecartEntity> minecart,
            Level level,
            double x,
            double y,
            double z,
            Item item) {
        super(minecart, level, x, y, z, item);
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
}