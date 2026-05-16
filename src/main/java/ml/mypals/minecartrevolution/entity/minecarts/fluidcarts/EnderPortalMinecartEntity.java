package ml.mypals.minecartrevolution.entity.minecarts.fluidcarts;

import ml.mypals.minecartrevolution.mixin.blocks.PistonBlockInvoker;
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

public class EnderPortalMinecartEntity extends NetherPortalMinecartEntity {

    private static final int BLOCK_FLAG_REMOVE_SOURCE = Block.UPDATE_CLIENTS | 256;
    private static final int BLOCK_FLAG_SET_TARGET = Block.UPDATE_ALL;

    public EnderPortalMinecartEntity(EntityType<EnderPortalMinecartEntity> entityType, Level level) {
        super(entityType, level);
    }

    public EnderPortalMinecartEntity(EntityType<EnderPortalMinecartEntity> minecart, Level level, double x, double y,
            double z, Item item) {
        super(minecart, level, x, y, z, item);
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

    @Override
    protected void handleTeleportation(PortalMinecartEntity targetPortal) {
        super.handleTeleportation(targetPortal);
        if (targetPortal instanceof EnderPortalMinecartEntity enderTarget) {
            this.teleportBlock(this.level(), enderTarget);
        }
    }

    private void teleportBlock(Level level, EnderPortalMinecartEntity targetPortal) {
        BlockPos sourcePos = this.blockPosition().above();
        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.isAir() || this.getPortalCooldown() > 0) {
            return;
        }

        BlockPos targetPos = targetPortal.blockPosition().above();
        BlockState targetState = level.getBlockState(targetPos);

        if (!PistonBaseBlock.isPushable(targetState, level, targetPos, Direction.UP, true, Direction.UP)) {
            if(level instanceof ServerLevel serverLevel){
                this.handleTeleportFailure(serverLevel, targetPortal);
            }
            return;
        }

        BlockEntity sourceBE = level.getBlockEntity(sourcePos);
        CompoundTag beData = sourceBE != null ? sourceBE.saveWithoutMetadata(level.registryAccess()) : null;

        boolean pushed = ((PistonBlockInvoker) Blocks.PISTON).minecartRevolution$moveBlocks(level, targetPortal.blockPosition(), Direction.UP, true);

        if (pushed) {
            level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), BLOCK_FLAG_REMOVE_SOURCE);

            level.setBlock(targetPos, sourceState, BLOCK_FLAG_SET_TARGET);
            if (beData != null && sourceState.getBlock() instanceof BaseEntityBlock baseEntityBlock) {
                BlockEntity newBe = baseEntityBlock.newBlockEntity(targetPos, sourceState);
                if (newBe != null) {
                    newBe.loadWithComponents(
                            TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), beData));
                    level.setBlockEntity(newBe);
                }
            }

            targetPortal.setPortalCooldown(PORTAL_COOLDOWN);
            level.gameEvent(GameEvent.TELEPORT, sourcePos, GameEvent.Context.of(sourceState));

            if(level instanceof ServerLevel serverLevel){
                this.playPortalEffects(serverLevel, this);
                this.playPortalEffects(serverLevel, targetPortal);
            }
        }else {
            if(level instanceof ServerLevel serverLevel){
                this.handleTeleportFailure(serverLevel, targetPortal);
            }
        }

        this.setPortalCooldown(PORTAL_COOLDOWN);


    }

    private void handleTeleportFailure(ServerLevel serverLevel, EnderPortalMinecartEntity targetPortal) {
        this.setPortalCooldown(PORTAL_COOLDOWN);

        serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getRandomX(0.5), this.getY(), this.getRandomZ(0.5),
                1, 0, 0, 0, 0);
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.IRON_GOLEM_HURT, this.getSoundSource(), 1.0F, 1.0F);

        for (int i = 0; i < 20; i++) {
            serverLevel.sendParticles(
                    DustParticleOptions.REDSTONE,
                    targetPortal.getRandomX(1.0),
                    targetPortal.getRandomY() + 0.5,
                    targetPortal.getRandomZ(1.0),
                    0, 0.0, targetPortal.getRandom().nextDouble(), 0.0, 1.0);
        }

        this.playPortalEffects(serverLevel, this);
    }

    private void playPortalEffects(ServerLevel serverLevel, AbstractMinecart minecart) {
        serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                minecart.getRandomX(0.5),
                minecart.getRandomY(),
                minecart.getRandomZ(0.5),
                0,
                (minecart.getRandom().nextDouble() - 0.5) * 2.0,
                -minecart.getRandom().nextDouble(),
                (minecart.getRandom().nextDouble() - 0.5) * 2.0,
                1.0);

        minecart.setHurtDir(-minecart.getHurtDir());
        minecart.setHurtTime(10);
        minecart.setDamage(50.0F);

        serverLevel.playSound(null, minecart.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, minecart.getSoundSource(),
                1.0F, 1.0F);
    }
}