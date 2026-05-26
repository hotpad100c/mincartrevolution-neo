package ml.mypals.minecartrevolution.entity.minecarts.simulation;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SimulatedServerTickAccess<T> extends LevelTicks<T> {
    private final LevelTicks<T> wrapped;
    private final SimulationBlockMinecartEntity minecart;
    private final List<ScheduledTick<T>> pendingTicks;

    public SimulatedServerTickAccess(ServerLevel serverLevel, LevelTicks<T> wrapped, SimulationBlockMinecartEntity minecart, List<ScheduledTick<T>> pendingTicks) {
        super((l)->serverLevel.isPositionEntityTicking(BlockPos.of(l)));
        this.wrapped = wrapped;
        this.minecart = minecart;
        this.pendingTicks = pendingTicks;
    }

    @Override
    public void schedule(ScheduledTick<T> tick) {
        if (tick.pos().equals(minecart.blockPosition())) {
            pendingTicks.add(tick);
        } else {
            wrapped.schedule(tick);
        }
    }

    @Override
    public boolean hasScheduledTick(BlockPos pos, @NonNull T type) {
        if (pos.equals(minecart.blockPosition())) {
            return pendingTicks.stream().anyMatch(t -> t.type().equals(type));
        }
        return wrapped.hasScheduledTick(pos, type);
    }

    @Override
    public int count() {
        return pendingTicks.size() + wrapped.count();
    }

    @Override
    public boolean willTickThisTick(@NonNull BlockPos pos, @NonNull T type) {
        return wrapped.willTickThisTick(pos, type);
    }
}