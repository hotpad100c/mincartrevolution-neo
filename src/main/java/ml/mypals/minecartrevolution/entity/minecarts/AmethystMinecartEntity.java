package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class AmethystMinecartEntity extends SingleBlockMinecartEntity {
    protected static final EntityDataAccessor<ItemStack> DISC =
            SynchedEntityData.defineId(AmethystMinecartEntity.class, EntityDataSerializers.ITEM_STACK);

    public JukeboxMinecartEntity sourceJukeBoxMinecartEntity;
    public int chainDistance = 0;

    public AmethystMinecartEntity(EntityType<? extends SingleBlockMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    public AmethystMinecartEntity(EntityType<? extends SingleBlockMinecartEntity> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DISC, ItemStack.EMPTY);
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.AMETHYST_MINECART.item().get();
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return new ItemStack(MRMinecarts.AMETHYST_MINECART.item().get());
    }

    public ItemStack getDisc() {
        return entityData.get(DISC);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            JukeboxMinecartEntity bestJukebox = sourceJukeBoxMinecartEntity;
            int bestDistance = Integer.MAX_VALUE;

            if(sourceJukeBoxMinecartEntity == null || sourceJukeBoxMinecartEntity.isRemoved()){
                if(sourceJukeBoxMinecartEntity != null)bestJukebox = null;
                List<JukeboxMinecartEntity> jukeboxes = this.level().getEntitiesOfClass(
                        JukeboxMinecartEntity.class,
                        this.getBoundingBox().inflate(16.0D)
                );

                for (JukeboxMinecartEntity jukebox : jukeboxes) {
                    if (!jukebox.getDisc().isEmpty() && jukebox.getPowerStrength(null, null) > 0) {
                        bestDistance = 1;
                        bestJukebox = jukebox;
                        break;
                    }
                }
            }

            if (bestDistance > 1) {
                List<AmethystMinecartEntity> amethysts = this.level().getEntitiesOfClass(
                        AmethystMinecartEntity.class,
                        this.getBoundingBox().inflate(16.0D)
                );
                for (AmethystMinecartEntity amethyst : amethysts) {
                    if (amethyst != this && amethyst.sourceJukeBoxMinecartEntity != null) {
                        JukeboxMinecartEntity src = amethyst.sourceJukeBoxMinecartEntity;
                        if (!src.isRemoved() && !src.getDisc().isEmpty() && src.getPowerStrength(null, null) > 0) {
                            double physicalDist = this.distanceTo(src);
                            int potentialChainDist = amethyst.chainDistance + 1;
                            
                            if (potentialChainDist < 64 && physicalDist <= 16.0D * potentialChainDist) {
                                if (potentialChainDist < bestDistance) {
                                    bestDistance = potentialChainDist;
                                    bestJukebox = src;
                                }
                            }
                        }
                    }
                }
            }

            this.sourceJukeBoxMinecartEntity = bestJukebox;
            this.chainDistance = bestDistance;

            if (this.sourceJukeBoxMinecartEntity != null) {
                if (this.getHurtTime() <= 0) {
                    this.setHurtDir(-this.getHurtDir());
                    this.setHurtTime(20);
                    this.setDamage(10);
                }
                ItemStack currentPlayingDisc = this.sourceJukeBoxMinecartEntity.getDisc();
                if (!ItemStack.matches(this.getDisc(), currentPlayingDisc)) {
                    this.entityData.set(DISC, currentPlayingDisc.copy());
                }
            } else {
                if (!this.getDisc().isEmpty()) {
                    this.entityData.set(DISC, ItemStack.EMPTY);
                }
            }
        } else {
            if (!this.getDisc().isEmpty()) {
                if (this.level().getRandom().nextInt(10) == 0) {
                    this.level().addParticle(ParticleTypes.NOTE, this.getX(), this.getY() + 1.2D, this.getZ(), this.level().getRandom().nextDouble() * 24.0D / 24.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}
