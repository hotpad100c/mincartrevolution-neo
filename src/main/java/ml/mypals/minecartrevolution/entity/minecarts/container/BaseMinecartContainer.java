package ml.mypals.minecartrevolution.entity.minecarts.container;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class BaseMinecartContainer extends AbstractMinecartContainer {
    protected BaseMinecartContainer(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory) {
        return null;
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return null;
    }

    @Override
    protected @NonNull Item getDropItem() {
        return null;
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
        if (player.isSecondaryUseActive()) {
            if (this.hasCustomDisplay()) {
                BlockState blockState = getDisplayBlockState();

                if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    Block block = blockState.getBlock();
                    playSound(block.defaultBlockState().getSoundType().getBreakSound(), 1, 1);
                    player.swing(hand);
                    if (!this.level().isClientSide()) {
                        clear();
                        ItemStack stack = block.asItem().getDefaultInstance();
                        player.setItemInHand(hand, stack);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.interact(player, hand, location);
    }

    private void clear() {
        setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
        Minecart minecartEntity = new Minecart(EntityType.MINECART, level());

        minecartEntity.restoreFrom(this);
        minecartEntity.copyPosition(this);
        minecartEntity.setDeltaMovement(this.getDeltaMovement());
        this.remove(RemovalReason.DISCARDED);
        this.level().addFreshEntity(minecartEntity);
        minecartEntity.setHurtDir(-minecartEntity.getHurtDir());
        minecartEntity.setHurtTime(10);
        minecartEntity.setDamage(50.0F);

    }

    public boolean hasCustomDisplay() {
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        return !(blockState.getBlock() instanceof AirBlock);
    }
}
