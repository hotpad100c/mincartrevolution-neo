package ml.mypals.minecartrevolution.mixin.blocks;

import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractMinecartRenderer.class)
public abstract class BlockRenderManagerMixin<
        T extends AbstractMinecart, S extends MinecartRenderState>
    extends EntityRenderer<T, S> {
  protected BlockRenderManagerMixin(EntityRendererProvider.Context ctx) {
    super(ctx);
  }
}
