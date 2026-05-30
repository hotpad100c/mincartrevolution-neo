package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.IServerLevelExt;
import ml.mypals.minecartrevolution.manager.PortalMinecartStorage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class PortalMinecartEntity extends VariantBlockMinecartEntity {

    protected static final int PORTAL_COOLDOWN = 8;
    protected static final double SEARCH_Y_INFLATE = 0.2;

    public PortalMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level level) {
        super(entityType, level);
    }

    public PortalMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level level, double x, double y,
            double z, Item item) {
        super(minecart, level, x, y, z, item);
    }

    @Override
    public boolean canCollideWith(@NonNull Entity entity) {
        return super.canCollideWith(entity) && !shouldTransfer();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return super.canBeCollidedWith(other) && !shouldTransfer();
    }
    protected boolean shouldTransfer(){
         return level().getBestNeighborSignal(blockPosition()) <= 0;
    }

    @Override
    public void tick() {

        super.tick();

        if (level().isClientSide()) {
            onClientTick();
            return;
        }

        if (isTransferBlocked() || !shouldTransfer()) {
            return;
        }

        PortalMinecartEntity targetPortal = findTargetPortal();
        if (targetPortal == null || isCooldownActive(targetPortal)) {
            return;
        }

        handleTeleportation(targetPortal);

    }

    protected void onClientTick() {
        if (this.getPortalCooldown() > 0) {
            this.setPortalCooldown(this.getPortalCooldown() - 1);
        }
    }

    protected boolean isTransferBlocked() {
        return getCustomName() == null;
    }

    protected boolean isCooldownActive(PortalMinecartEntity targetPortal) {
        return this.getPortalCooldown() > 0 || targetPortal.getPortalCooldown() > 0;
    }

    protected void handleTeleportation(PortalMinecartEntity targetPortal) {
        List<Entity> entities = getTouchingEntities();
        for (Entity entity : entities) {
            handleEntityTeleport(entity, targetPortal);
        }
    }

    protected abstract void handleEntityTeleport(Entity entity, PortalMinecartEntity targetPortal);

    protected List<Entity> getTouchingEntities() {
        return level().getEntities(
                this,
                getBoundingBox().inflate(0, SEARCH_Y_INFLATE, 0),
                entity -> entity != this);
    }

    @Nullable
    protected PortalMinecartEntity findTargetPortal() {
        if (getCustomName() == null)
            return null;

        String name = getCustomName().getString();
        PortalMinecartStorage storage = ((IServerLevelExt) level()).mincartrevolution_neo$getPortalMinecartStorage();

        PortalMinecartEntity pt = storage.getClosest(this, level(), position(), this.getClass(), name);
        if(pt != null && level() instanceof ServerLevel serverLevel && !level().isLoaded(pt.blockPosition())){
            placeTicket(serverLevel, pt.chunkPosition());
        }
        return pt;
    }
    public static long placeTicket(ServerLevel level, ChunkPos chunk) {
        level.getChunkSource().addTicketWithRadius(TicketType.ENDER_PEARL, chunk, 2);
        return TicketType.ENDER_PEARL.timeout();
    }
    protected static void playPortalDamageAnimation(AbstractMinecart minecart) {
        ((ServerLevel) minecart.level())
                .sendParticles(
                        ParticleTypes.PORTAL,
                        minecart.getRandomX(0.5),
                        minecart.getRandomY(),
                        minecart.getRandomZ(0.5),
                        10,
                        (minecart.getRandom().nextDouble() - 0.5) * 2.0,
                        -minecart.getRandom().nextDouble(),
                        (minecart.getRandom().nextDouble() - 0.5) * 2.0,
                        1);

        minecart.setHurtDir(-minecart.getHurtDir());
        minecart.setHurtTime(10);
        minecart.setDamage(50.0F);
    }
}
