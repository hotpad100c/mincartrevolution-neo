package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import ml.mypals.minecartrevolution.interfaces.IMinecartSource;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.util.MinecartRotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.Optional;

public class DispenserMinecartEntity extends BaseMinecartContainer implements ContainerEntity, IMinecartContainer {

    private boolean activated = false; // 用于检测红石边沿信号（从无电到有电）
    private int dispenseCooldown = 0;

    public DispenserMinecartEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    public DispenserMinecartEntity(EntityType<?> type, Level level, double x, double y, double z) {
        super(type, level);
        setInitialPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            updateDispenserFacing();
            if (this.dispenseCooldown > 0) {
                this.dispenseCooldown--;
            }
        }
        if(activated && level() instanceof ServerLevel serverLevel &&
                !(this.level().getBlockState(BlockPos.containing(this.position())).getBlock()
                        instanceof PoweredRailBlock poweredRailBlock
                        && poweredRailBlock.isActivatorRail())
        ){
            activateMinecart(serverLevel,this.blockPosition().getX(),this.blockPosition().getY(),this.blockPosition().getZ(),false);
        }
    }

    private void updateDispenserFacing() {
        float minecartYaw = this.getYRot();
        float baseOffset = 90.0F;
        float targetYaw = (minecartYaw + baseOffset) % 360.0F;
        if (targetYaw < 0) targetYaw += 360.0F;
        Direction finalFacing = Direction.fromYRot(targetYaw);
        BlockState state = this.getDisplayBlockState();
        if (state.getValue(DispenserBlock.FACING) != finalFacing) {
            this.setCustomDisplayBlockState(Optional.of(state.setValue(DispenserBlock.FACING, finalFacing)));
        }
    }

    @Override
    public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {
        if (powered) {
            if (!this.activated) {
                this.dispense();
                this.activated = true;
            }
        } else {
            this.activated = false;
        }
    }

    public void dispense() {
        if (this.level().isClientSide() || this.dispenseCooldown > 0) return;

        int slot = this.getRandomSlot();
        if (slot < 0) {
            this.level().levelEvent(1001, this.blockPosition(), 0);
        } else {
            float pitch = this.getXRot();
            BlockState currentState = this.getDisplayBlockState();
            Direction localDir = currentState.hasProperty(DispenserBlock.FACING) ? 
                    currentState.getValue(DispenserBlock.FACING) : Direction.NORTH;
            
            Direction placeDir = MinecartRotationUtils.getAbsoluteDirection(localDir, this.getYRot());

            int offsetX = placeDir.getStepX();
            int offsetY = placeDir.getStepY();
            int offsetZ = placeDir.getStepZ();

            // 如果方块水平放置，但矿车有明显的俯仰角，我们根据俯仰角调整 Y 偏移
            // 保持原有的逻辑：大俯仰角时强制上下发射
            if (pitch < -15.0F) {
                offsetY = 1;
            } else if (pitch > 15.0F) {
                offsetY = -1;
            }

            // 3. 计算最终方块坐标
            BlockPos targetPos = this.blockPosition().offset(offsetX, offsetY, offsetZ);
            ItemStack itemstack = this.getItem(slot);
            executeDispenseAt(targetPos, placeDir, slot, itemstack);
        }
    }

    private void executeDispenseAt(BlockPos targetPos, Direction dir, int slot, ItemStack itemStack) {
        ItemStack itemstack = this.getItem(slot);
        DispenseItemBehavior behavior = DispenserBlock.DISPENSER_REGISTRY.get(itemstack.getItem());
        if(behavior == null) behavior = DispenseItemBehavior.NOOP;
        BlockState fakeState = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, dir);

        BlockSource source = new BlockSource(
                (ServerLevel)this.level(),
                targetPos,
                fakeState,
                new DispenserBlockEntity(targetPos, fakeState)
        );

        this.setItem(slot, behavior.dispense(source, itemstack));
    }

    public Vec3 getOffsetPreciseVector(boolean clockwise) {
        // 1. 获取基础角度
        float baseYaw = this.getYRot();
        float pitch = this.getXRot();

        // 2. 应用 90 度偏移
        // 如果是顺时针旋转 90 度，则 +90；逆时针则 -90
        float targetYaw = baseYaw + (clockwise ? 90.0F : -90.0F);

        // 3. 将角度转换为弧度
        float yawRad = targetYaw * ((float)Math.PI / 180F);
        float pitchRad = pitch * ((float)Math.PI / 180F);

        // 4. 计算 3D 空间向量
        // Minecraft 中：x = -sin(yaw) * cos(pitch), y = -sin(pitch), z = cos(yaw) * cos(pitch)
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        return new Vec3(x, y, z).normalize();
    }

    public int getRandomSlot() {
        int replaceSlot = -1;
        int replaceOdds = 1;
        for (int i = 0; i < this.getContainerSize(); ++i) {
            if (!this.getItem(i).isEmpty() && this.random.nextInt(replaceOdds++) == 0) {
                replaceSlot = i;
            }
        }
        return replaceSlot;
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        // WEST in block-space maps to "Forward" in world-space (given 270-yaw rotation)
        return Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.WEST);
    }

    @Override
    public int getContainerSize() { return 9; }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory) {
        return new DispenserMenu(i, inventory, this);
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
        if (!player.isSecondaryUseActive()) {
            return this.interactWithContainerVehicle(player);
        }
        return super.interact(player, hand, location);
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.DISPENSER_MINECART.item().get().getDefaultInstance();
    }

    @Override
    protected @NonNull Item getDropItem() {
        return MRMinecarts.DISPENSER_MINECART.item().get();
    }

    @Override
    public void minecartrevolution$OnContainerClosed(Level level, Player player) {}
}