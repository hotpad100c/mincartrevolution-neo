package ml.mypals.minecartrevolution.mixin;

import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartRenderer.class)
public class AbstractMinecartRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;F)V", at = @At(value = "RETURN"))
    private void submitMinecartContents(AbstractMinecart entity, MinecartRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity.getDisplayBlockState().is(Blocks.ENCHANTING_TABLE) || entity.getDisplayBlockState().is(Blocks.ENDER_CHEST)) {
            ((BlockModelRenderStateAccessor) state.displayBlockModel).setSpecialRenderer(null);
        }
    }
}