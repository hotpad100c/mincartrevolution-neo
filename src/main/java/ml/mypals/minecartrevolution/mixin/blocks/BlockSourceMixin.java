package ml.mypals.minecartrevolution.mixin.blocks;

import ml.mypals.minecartrevolution.interfaces.IMinecartSource;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockSource.class)
public class BlockSourceMixin implements IMinecartSource {
    @Unique
    private Entity mincartrevolution_neo$minecart;

    @Override
    public void mincartrevolution_neo$setMinecart(Entity entity) { this.mincartrevolution_neo$minecart = entity; }

    @Override
    public Entity mincartrevolution_neo$getMinecart() { return this.mincartrevolution_neo$minecart; }
}
