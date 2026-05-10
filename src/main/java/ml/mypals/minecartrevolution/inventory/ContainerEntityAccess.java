package ml.mypals.minecartrevolution.inventory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.BiFunction;

public class ContainerEntityAccess implements ContainerLevelAccess {
    private final Entity entity;

    public ContainerEntityAccess(Entity entity) {
        this.entity = entity;
    }

    @Override
    public <T> @NonNull Optional<T> evaluate(@NonNull BiFunction<Level, BlockPos, T> action) {
        if (!this.entity.isAlive()) {
            return Optional.empty();
        }
        return Optional.of(action.apply(this.entity.level(), this.entity.blockPosition()));
    }

    public Entity getEntity() {
        return this.entity;
    }
}