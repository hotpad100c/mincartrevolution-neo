package ml.mypals.minecartrevolution.mixin.entity;

import ml.mypals.minecartrevolution.entity.minecarts.CobwebMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private @Nullable Entity vehicle;

    @Shadow
    @Final
    protected RandomSource random;

    @Shadow
    private Level level;

    @Shadow
    private BlockPos blockPosition;

    @Shadow
    public abstract EntityType<?> getType();

    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
    public void stopRiding(CallbackInfo ci) {
        Entity me = (Entity) (Object) this;
        if (this.vehicle instanceof CobwebMinecartEntity && me instanceof Player && this.random.nextFloat() < 0.7f) {
            if (!this.level.isClientSide()) {
                ci.cancel();
            } else {
                this.level.addDestroyBlockEffect(this.blockPosition, Blocks.COBWEB.defaultBlockState());
            }
        }
    }

}
