package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class TrappedChestMinecartEntity extends AbstractMinecartContainer implements PowerEmitterMinecartEntity {
    private int viwers = 0;
    public TrappedChestMinecartEntity(EntityType<? extends AbstractMinecartContainer> entityType, Level world) {
        super(entityType, world);
    }
    public TrappedChestMinecartEntity( Level world, double x, double y, double z) {
        super(MRMinecarts.TRAPPED_CHEST_MINECART.entity().get(), world);
        this.setInitialPos(x, y, z);
    }

    @Override
    public ItemStack getPickResult() {
        return MRMinecarts.TRAPPED_CHEST_MINECART.item().get().getDefaultInstance();
    }
  /*  @Override
    public Type getMinecartType() {
        return Type.CHEST;
    }
*/
    @Override
    public Item getDropItem() {
        return MRMinecarts.BARREL_MINECART.item().get();
    }

    @Override
    public int getContainerSize() {
        return 27;
    }
    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.TRAPPED_CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 8;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    @Override
    public void tick() {
        super.tick();

        this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).ifPresent(blockState -> {
            if(!updatedBlocks || !this.isAlive()){
                if(getPreviousBlockPos() != null)updateNeighbors(this.level(),previousBlockPos, blockState.getBlock());
                updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
            }
            if (this.getPreviousBlockPos() == null || !this.getPreviousBlockPos().equals(this.blockPosition())) {
                if(this.getPreviousBlockPos() == null) this.setPreviousBlockPos(this.blockPosition());
                updateNeighbors(this.level(),previousBlockPos, blockState.getBlock());
                this.setPreviousBlockPos(this.blockPosition());
                updateNeighbors(this.level(),this.blockPosition(), blockState.getBlock());
            }
        });


    }
    /*@Override
    public void stopOpen(Player player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));
        updateNeighbors(this.level(),this.blockPosition(), Block.stateById(this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY_BLOCK)).getBlock());
        viwers = Math.min(0, viwers - 1);
    }*/
    @Override
   public @NonNull InteractionResult interactWithContainerVehicle(@NonNull Player player) {
        viwers++;
        return super.interactWithContainerVehicle(player);
    }
    @Override
    public int getPowerStrength(Direction direction, BlockPos pos) {
        return direction == Direction.UP ? 0:
                Mth.clamp(viwers, 0, 15);
    }
    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        InteractionResult actionResult = this.interactWithContainerVehicle(player);
        if (actionResult.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            if(player.level() instanceof ServerLevel serverLevel) PiglinAi.angerNearbyPiglins(serverLevel, player, true);
            this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).ifPresent(blockState -> {
                updateNeighbors(this.level(),this.blockPosition(), blockState.getBlock());
            });
        }

        return actionResult;
    }

    public int getViwers() {
        return viwers;
    }

    public void setViwers(int viwers) {
        this.viwers = viwers;
    }
}
