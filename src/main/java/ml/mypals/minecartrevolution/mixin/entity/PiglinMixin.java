package ml.mypals.minecartrevolution.mixin.entity;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Piglin.class)
public abstract class PiglinMixin {

    @Unique
    private int minecartrevolution$goldAttractCooldown = 0;

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void minecartrevolution$attractToGoldMinecart(CallbackInfo ci) {
        Piglin self = (Piglin) (Object) this;
        if (!self.isAdult()) return;
        if (self.getTarget() != null) return;

        if (minecartrevolution$goldAttractCooldown > 0) {
            minecartrevolution$goldAttractCooldown--;
            return;
        }
        minecartrevolution$goldAttractCooldown = 20;

        Brain<Piglin> brain = self.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT)) return;
        if (brain.hasMemoryValue(MemoryModuleType.ADMIRING_ITEM)) return;

        List<AbstractMinecart> minecarts = self.level().getEntitiesOfClass(
                AbstractMinecart.class,
                self.getBoundingBox().inflate(10.0)
        );

        AbstractMinecart closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (AbstractMinecart cart : minecarts) {
            if (!cart.getDisplayBlockState().is(Blocks.GOLD_BLOCK)) continue;
            double distSq = cart.distanceToSqr(self);
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = cart;
            }
        }

        if (closest == null) return;
        if (closestDistSq < 4.0) return;
        if (!self.getSensing().hasLineOfSight(closest)) return;

        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(closest.blockPosition(), 1.0F, 2));
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(closest, true));
    }
}
