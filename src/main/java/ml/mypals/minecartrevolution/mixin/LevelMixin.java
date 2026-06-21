package ml.mypals.minecartrevolution.mixin;

import java.util.List;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.BeaconMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor {
  @Inject(method = "markAndNotifyBlock", at = @At("TAIL"))
  private void mr$onBlockUpdated(
      BlockPos pos,
      LevelChunk chunk,
      BlockState oldState,
      BlockState blockState,
      int updateFlags,
      int updateLimit,
      CallbackInfo ci) {

    AABB columnBB =
        new AABB(
            pos.getX(), this.getMinY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    List<BeaconMinecartEntity> minecarts =
        this.getEntitiesOfClass(BeaconMinecartEntity.class, columnBB);
    for (BeaconMinecartEntity minecart : minecarts) {
      minecart.updateBeam();
    }
  }
}
