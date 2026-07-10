package ml.mypals.minecartrevolution.mixin;

import java.util.List;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.minecartrevolution.config.Config;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.BeaconMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.simulation.LastCartInteractionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor {
  @Shadow
  @Final
  private boolean isClientSide;

  @Shadow
  public abstract <T extends net.minecraft.world.entity.Entity> List<T> getEntities(EntityTypeTest<net.minecraft.world.entity.Entity, T> type, AABB bb, Predicate<? super T> selector);

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
  @WrapMethod(method = "getBlockEntity")
  public @Nullable BlockEntity getBlockEntity(BlockPos pos, Operation<BlockEntity> original) {
    if(!isClientSide || !Config.FORCE_COMPATIBILITY.get()) return original.call(pos);
    if(!Minecraft.getInstance().isSameThread()) return original.call(pos);

    CompatFriendlyBlockMinecartEntity cart = LastCartInteractionCache.LAST_INTERACTED;
    if(cart != null && cart.isAlive() && cart.blockPosition().equals(pos)){
      BlockEntity blockEntity = cart.blockEntity;
      if(blockEntity != null){
        return blockEntity;
      }
    }
    return original.call(pos);
  }
}
