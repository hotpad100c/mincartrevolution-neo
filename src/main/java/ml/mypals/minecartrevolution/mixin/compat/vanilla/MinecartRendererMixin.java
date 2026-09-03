package ml.mypals.minecartrevolution.mixin.compat.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderDispatcher.class)
public abstract class MinecartRendererMixin {

    @WrapMethod(method = "shouldRender")
    private boolean shouldRender(Entity entity, Frustum culler, double camX, double camY, double camZ, Operation<Boolean> original) {
        boolean org =  original.call(entity, culler, camX, camY, camZ);
        if(entity instanceof VariantBlockMinecartEntity){
            if(entity.position().distanceTo(new Vec3(camX,camY,camZ)) > 32){
                return false;
            }
        }
        return org;
    }
}
