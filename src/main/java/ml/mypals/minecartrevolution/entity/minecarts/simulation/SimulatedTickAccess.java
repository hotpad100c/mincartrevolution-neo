package ml.mypals.minecartrevolution.entity.minecarts.simulation;

import java.util.List;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import org.jspecify.annotations.NonNull;

public class SimulatedTickAccess<T> implements LevelTickAccess<T> {
  private final LevelTickAccess<T> wrapped;
  private final CompatFriendlyBlockMinecartEntity minecart;
  private final List<ScheduledTick<T>> pendingTicks;

  public SimulatedTickAccess(
      LevelTickAccess<T> wrapped,
      CompatFriendlyBlockMinecartEntity minecart,
      List<ScheduledTick<T>> pendingTicks) {
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
