package ml.mypals.minecartrevolution.mixin.compat.extrapower;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.SimulatedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.extra.power.block.blockentity.MushroomCloudBlockEntity")
public class MushroomCloudBlockEntityMixin {

  @ModifyExpressionValue(
      method = "tick",
      at =
          @At(
              value = "FIELD",
              target = "Lcom/extra/power/config/ModServerConfig$NuclearExplosion;Explosionlevel:I"),
      require = 0)
  private static int clampExplosionLevel(
      int original, Level level, BlockPos pos, BlockState state) {
    if (level instanceof SimulatedLevel) {
      return Math.min(original, 6);
    }
    return original;
  }
}
