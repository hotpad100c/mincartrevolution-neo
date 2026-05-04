package ml.mypals.minecartrevolution.mixin;

import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class RedstoneViewMixin implements LevelAccessor {
    @Override
    public int getSignal(@NotNull BlockPos pos, @NotNull Direction direction) {
        int redstonePowerFromBlock = minecartrevolution_neo$getRedstonePower(pos,direction);
        int redstonePowerFromEntity = this.minecartrevolution_neo$getRedstonePowerFromEntity(pos, direction);
        return Math.max(redstonePowerFromBlock, redstonePowerFromEntity);
    }
    @Unique
    private int minecartrevolution_neo$getRedstonePower(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = blockState.getSignal(this, pos, direction);
        return blockState.isRedstoneConductor(this, pos) ? Math.max(i, this.getDirectSignalTo(pos)) : i;
    }
    @Unique
    public int minecartrevolution_neo$getRedstonePowerFromEntity(BlockPos pos, Direction direction) {
        AABB box = new AABB(
                pos
        );
        List<? extends AbstractMinecart> powers = this.getEntitiesOfClass(
                AbstractMinecart.class,
                box,
                entity -> entity instanceof PowerEmitterMinecartEntity
        );
        Optional <? extends AbstractMinecart> strongest = powers.stream()
                .max(Comparator.comparingInt(cart -> ((PowerEmitterMinecartEntity)cart).getPowerStrength(direction,pos)));
        return strongest.map(redstoneEmitterPowerMinecart ->((PowerEmitterMinecartEntity) redstoneEmitterPowerMinecart).getPowerStrength(direction,pos)).orElse(0);
    }
}
