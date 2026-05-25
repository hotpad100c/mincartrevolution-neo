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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.AMETHYST_BLOCK.defaultBlockState();
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
            boolean needsRecalculation = false;
            
            if (this.sourceJukeBoxMinecartEntity == null || this.sourceJukeBoxMinecartEntity.isRemoved() || 
                this.sourceJukeBoxMinecartEntity.getDisc().isEmpty() || this.sourceJukeBoxMinecartEntity.getPowerStrength(null, null) == 0) {
                needsRecalculation = true;
            }

            if (needsRecalculation) {
                JukeboxMinecartEntity bestJukebox = null;
                int bestDistance = Integer.MAX_VALUE;

                List<JukeboxMinecartEntity> jukeboxes = this.level().getEntitiesOfClass(
                        JukeboxMinecartEntity.class,
                        this.getBoundingBox().inflate(32)
                );

                for (JukeboxMinecartEntity jukebox : jukeboxes) {
                    if (!jukebox.getDisc().isEmpty() && jukebox.getPowerStrength(null, null) > 0) {
                        bestDistance = 1;
                        bestJukebox = jukebox;
                        break;
                    }
                }

                if (bestDistance > 1) {
                    List<AmethystMinecartEntity> amethysts = this.level().getEntitiesOfClass(
                            AmethystMinecartEntity.class,
                            this.getBoundingBox().inflate(32)
                    );
                    for (AmethystMinecartEntity amethyst : amethysts) {
                        if (amethyst != this && amethyst.sourceJukeBoxMinecartEntity != null) {
                            JukeboxMinecartEntity src = amethyst.sourceJukeBoxMinecartEntity;
                            if (!src.isRemoved() && !src.getDisc().isEmpty() && src.getPowerStrength(null, null) > 0) {
                                int potentialChainDist = amethyst.chainDistance + 1;
                                
                                if (potentialChainDist < 64) {
                                    if (potentialChainDist < bestDistance) {
                                        bestDistance = potentialChainDist;
                                        bestJukebox = src;
                                    }
                                }
                            }
                        }
                    }
                }

                if (this.sourceJukeBoxMinecartEntity != bestJukebox) {
                    if (this.sourceJukeBoxMinecartEntity != null) {
                        this.sourceJukeBoxMinecartEntity.removeConnectedAmethyst(this.getId());
                    }
                    this.sourceJukeBoxMinecartEntity = bestJukebox;
                    if (this.sourceJukeBoxMinecartEntity != null) {
                        this.sourceJukeBoxMinecartEntity.addConnectedAmethyst(this.getId());
                    }
                }
                this.chainDistance = bestDistance;
            }

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

    @Override
    public void remove(@NonNull RemovalReason reason) {
        if (!this.level().isClientSide() && this.sourceJukeBoxMinecartEntity != null) {
            this.sourceJukeBoxMinecartEntity.removeConnectedAmethyst(this.getId());
            this.sourceJukeBoxMinecartEntity = null;
        }
        super.remove(reason);
    }
}
