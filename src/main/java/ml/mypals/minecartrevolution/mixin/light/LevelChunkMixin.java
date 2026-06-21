package ml.mypals.minecartrevolution.mixin.light;

import ml.mypals.minecartrevolution.client.light.DynamicLightsStorage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public abstract class LevelChunkMixin extends Level implements BlockAndTintGetter {

  protected LevelChunkMixin(
      WritableLevelData levelData,
      ResourceKey<Level> dimension,
      RegistryAccess registryAccess,
      Holder<DimensionType> dimensionTypeRegistration,
      boolean isClientSide,
      boolean isDebug,
      long biomeZoomSeed,
      int maxChainedNeighborUpdates) {
    super(
        levelData,
        dimension,
        registryAccess,
        dimensionTypeRegistration,
        isClientSide,
        isDebug,
        biomeZoomSeed,
        maxChainedNeighborUpdates);
  }

  @Override
  public int getBrightness(@NonNull LightLayer layer, @NonNull BlockPos pos) {
    int blockLight = super.getBrightness(layer, pos);
    final BlockState state = this.getBlockState(pos);
    if (layer == LightLayer.SKY || state.isSolidRender()) {
      return blockLight;
    }
    double dynamicLight = DynamicLightsStorage.getLightLevel(pos);
    return (int) Math.max(dynamicLight, blockLight);
  }
}
