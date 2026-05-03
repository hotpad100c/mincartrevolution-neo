package ml.mypals.minecartrevolution.advancements.criterion;

import ml.mypals.minecartrevolution.MinecartRevolution;
import net.minecraft.advancements.CriteriaTriggers;

public class MRModCriteria {
    public static final MovingOnJukeboxCartCriterion ENTITY_MOVED =
            CriteriaTriggers.register(MinecartRevolution.idString("entity_moved"), new MovingOnJukeboxCartCriterion());
    public static final BlockCartCraftedCriterion BLOCK_CART_CRAFTED =
            CriteriaTriggers.register(MinecartRevolution.idString("block_cart_crafted"), new BlockCartCraftedCriterion());

    public static void init(){

    }
}
