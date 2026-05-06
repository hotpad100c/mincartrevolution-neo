package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.client.menu.MinecartChestMenu;
import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class BarrelMinecartEntity extends AbstractMinecartContainer implements ContainerEntity, IMinecartContainer {
    private int openCount = 0;

    public BarrelMinecartEntity(EntityType<? extends AbstractMinecartContainer> entityType, Level world) {
        super(entityType, world);
    }

    public BarrelMinecartEntity(EntityType<? extends AbstractMinecartContainer> entityType, Level world, double x, double y, double z) {
        super(entityType, world);
        setInitialPos(x, y, z);
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.BARREL_MINECART.item().get().getDefaultInstance();
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.BARREL_MINECART.item().get();
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 8;
    }

    @Override
    public @NonNull AbstractContainerMenu createMenu(int syncId, @NonNull Inventory playerInventory) {
        return new MinecartChestMenu(MenuType.GENERIC_9x3, syncId, playerInventory, this, 3, this);
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
        InteractionResult actionResult = this.interactWithContainerVehicle(player);
        if (actionResult.consumesAction()) {
            this.openCount++;
            if (this.openCount >= 1) {
                this.level().playSound(this, this.blockPosition(),
                        SoundEvents.BARREL_OPEN, SoundSource.BLOCKS);
            }
            this.level().broadcastEntityEvent(this, (byte) 10);
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            this.setCustomDisplayBlockState(Optional.of(Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP).setValue(BarrelBlock.OPEN, true)));
            player.awardStat(Stats.OPEN_BARREL);
            this.playSound(SoundEvents.BARREL_OPEN);
            if (player.level() instanceof ServerLevel serverLevel)
                PiglinAi.angerNearbyPiglins(serverLevel, player, true);
        }

        return actionResult;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            if (this.level().isClientSide()) {
                this.openCount++;
            }
            if (this.openCount >= 1) {
                this.setCustomDisplayBlockState(Optional.of(this.getDefaultDisplayBlockState().setValue(BarrelBlock.OPEN, true)));
            }
        } else if (id == 11) {
            if (this.level().isClientSide()) {
                this.openCount = Math.max(0, this.openCount - 1);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void minecartrevolution$OnContainerClosed(Level level, Player player) {
        if (!this.level().isClientSide()) {
            this.openCount = Math.max(0, this.openCount - 1);
            this.level().broadcastEntityEvent(this, (byte) 11);
            if (this.openCount == 0) {
                this.gameEvent(GameEvent.CONTAINER_CLOSE, player);
                this.level().playSound(this, this.blockPosition(),
                        SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS);

            }
        }
    }
}
