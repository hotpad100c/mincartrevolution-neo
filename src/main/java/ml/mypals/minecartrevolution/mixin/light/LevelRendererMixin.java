package ml.mypals.minecartrevolution.mixin.light;
import ml.mypals.minecartrevolution.client.light.DynamicLightsStorage;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(
            method = "getLightCoords(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void injectHeadCancellableGetLightMapCoordinates(
            LevelRenderer.BrightnessGetter brightnessGetter, BlockAndLightGetter level, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir
    ) {
        if (state.isSolidRender()) return;
        if (state.emissiveRendering(level, pos)) return;

        final double dynamicLightLevel = DynamicLightsStorage.getLightLevel(pos);
        final int vanillaLightMap = cir.getReturnValue();

        final int blockLightCoordinates = vanillaLightMap >> 4 & 0xffff / 16;
        final int light = (int) (Math.min(0xff, 16 * Math.max(dynamicLightLevel, blockLightCoordinates)));

        cir.setReturnValue(vanillaLightMap & 0xffff_0000 | light);
    }
}
