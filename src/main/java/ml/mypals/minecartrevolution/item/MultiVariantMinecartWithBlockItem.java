package ml.mypals.minecartrevolution.item;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import net.minecraft.world.level.block.Block;

public class MultiVariantMinecartWithBlockItem extends MinecartWithBlockItem {
    public MultiVariantMinecartWithBlockItem(AdvancedMinecartEntityTypes.Type type, Properties settings, Block blockInside) {
        super(type, settings, blockInside);
    }
}
