package ml.mypals.minecartrevolution.client.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public class SofaModelLoader {
  private static BlockModel sofaModel;

  @SubscribeEvent
  public static void onModelBake(ModelEvent.BakingCompleted event) {}

  public static BlockModel getSofaModel() {
    return sofaModel;
  }
}
