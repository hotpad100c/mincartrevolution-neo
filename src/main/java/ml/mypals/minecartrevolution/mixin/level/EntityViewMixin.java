package ml.mypals.minecartrevolution.mixin.level;


import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import ml.mypals.minecartrevolution.interfaces.MultiCollision;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EntityGetter.class)
public interface EntityViewMixin{
    @WrapOperation(
            method = "getEntityCollisions",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;"
            )
    )
    private ImmutableList.Builder<VoxelShape> wrapCollisionShape(
            ImmutableList.Builder<VoxelShape> instance,
            Object element,
            Operation<ImmutableList.Builder<VoxelShape>> original,
            @Local(name = "entity") Entity entity
    ) {
        if (entity instanceof MultiCollision multi) {
            for (AABB aabb : multi.getColliders()){
                instance.add(Shapes.create(aabb));
            }
            return instance;
        }
         return original.call(instance, element);
    }
}