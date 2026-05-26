package ml.mypals.minecartrevolution.mixin.light;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.minecartrevolution.client.light.DynamicLightsStorage;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @WrapMethod(
            method = "getLightCoords(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I"

    )
    private static int mr$getLightMapCoordinates(
            LevelRenderer.BrightnessGetter brightnessGetter, BlockAndLightGetter level, BlockState state, BlockPos pos, Operation<Integer> original
    ) {
        final int vanillaLightMap = original.call(brightnessGetter, level, state, pos);

        final int blockLightCoordinates = vanillaLightMap >> 4 & 0xffff / 16;

        if (state.isSolidRender()) return vanillaLightMap;
        if (state.emissiveRendering(level, pos)) return vanillaLightMap;

        final double dynamicLightLevel = DynamicLightsStorage.getLightLevel(pos);
        final int light = (int) (Math.min(0xff, 16 * Math.max(dynamicLightLevel, blockLightCoordinates)));
        return vanillaLightMap & 0xffff_0000 | light;
    }
}
