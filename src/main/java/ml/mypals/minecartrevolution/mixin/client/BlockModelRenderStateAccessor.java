package ml.mypals.minecartrevolution.mixin.client;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModelRenderState.class)
public interface BlockModelRenderStateAccessor {
  @Accessor("specialRenderer")
  void setSpecialRenderer(SpecialModelRenderer<?> renderer);
}
