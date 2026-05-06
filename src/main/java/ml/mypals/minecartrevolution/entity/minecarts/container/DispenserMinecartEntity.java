package ml.mypals.minecartrevolution.entity.minecarts.container;

import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import ml.mypals.minecartrevolution.interfaces.IMinecartSource;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
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
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class DispenserMinecartEntity extends AbstractMinecartContainer implements ContainerEntity, IMinecartContainer {

    private boolean activated = false; // 用于检测红石边沿信号（从无电到有电）
    private int dispenseCooldown = 0;   // 发射冷却

    public DispenserMinecartEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.dispenseCooldown > 0) {
            this.dispenseCooldown--;
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
            // 离开铁轨或铁轨断电，重置状态
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
            float rawYaw = this.getYRot();
            boolean isClockwise = this.isOnRails();
            float targetYaw = rawYaw + (isClockwise ? 90.0F : -90.0F);

            int offsetX;
            int offsetY = 0;
            int offsetZ;
            Direction placeDir;

            if (pitch < -15.0F) {
                float yaw = Math.round(targetYaw / 90.0F) * 90.0F;
                placeDir = Direction.fromYRot(yaw);
                offsetX = placeDir.getStepX();
                offsetZ = placeDir.getStepZ();
                offsetY = 1; // 往上一层放
            } else if (pitch > 15.0F) {
                // --- 向下 4 向 ---
                float yaw = Math.round(targetYaw / 90.0F) * 90.0F;
                placeDir = Direction.fromYRot(yaw);
                offsetX = placeDir.getStepX();
                offsetZ = placeDir.getStepZ();
                offsetY = -1; // 往下一层放
            } else {
                // --- 水平 8 向 ---
                float yaw = Math.round(targetYaw / 45.0F) * 45.0F;
                placeDir = Direction.fromYRot(yaw);

                // 这里的关键：8向需要根据 Yaw 手动算 XZ 偏移
                // 因为 Direction 只有 4 个水平向，不能表示斜 45 度
                float rad = yaw * ((float) Math.PI / 180F);
                offsetX = -Math.round(Mth.sin(rad));
                offsetZ = Math.round(Mth.cos(rad));
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
        if (behavior == null || !itemStack.isItemEnabled(this.level().enabledFeatures())) behavior = (DispenseItemBehavior) Blocks.DISPENSER.asItem();
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
        return Blocks.DISPENSER.defaultBlockState();
    }

    @Override
    public int getContainerSize() { return 9; }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory) {
        return new DispenserMenu(i, inventory, this);
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 location) {
        return this.interactWithContainerVehicle(player);
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