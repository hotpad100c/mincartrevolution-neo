package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.registeries.MRModItems;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class BarrelMinecartEntity extends AbstractMinecartContainer implements ContainerEntity {
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

//    @Overrid
//  public Type getMinecartType() {return Type.CHEST;}

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
    public @NonNull AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {

        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    /*
    @Override
    public void stopOpen(Player player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));
    }
    */
    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
        InteractionResult actionResult = this.interactWithContainerVehicle(player);
        if (actionResult.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            if(player.level() instanceof ServerLevel serverLevel) PiglinAi.angerNearbyPiglins(serverLevel, player, true);
        }

        return actionResult;
    }
}
