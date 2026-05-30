package ml.mypals.minecartrevolution.mixin.client;

import ml.mypals.minecartrevolution.interfaces.MultiCollision;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityRendererMixin {
    @Inject(
            method = "showHitboxes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/gizmos/Gizmos;arrow(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;I)Lnet/minecraft/gizmos/GizmoProperties;",
                    shift = At.Shift.BEFORE
            )
    )
    private void renderExtraBoxes(
            Entity entity, float partialTicks, boolean isServerEntity, CallbackInfo ci
    ) {
        if (!(entity instanceof MultiCollision multi)) {
            return;
        }

        for (AABB aabb : multi.getColliders()){
            Vec3 latestSubPosition = entity.position();
            Vec3 currentSubPosition = entity.getPosition(partialTicks);
            Vec3 subOffset = currentSubPosition.subtract(latestSubPosition);
            Gizmos.cuboid(aabb.move(subOffset), GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 0.25F, 1.0F, 0.0F)));

        }
    }
}
