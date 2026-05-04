package ml.mypals.minecartrevolution.registeries;

import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.advancements.criterion.BlockCartCraftedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.MovingOnJukeboxCartCriterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MRModCriteria {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS  = DeferredRegister.create(Registries.TRIGGER_TYPE, MinecartRevolution.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, MovingOnJukeboxCartCriterion> ENTITY_MOVED = TRIGGERS
            .register(
                    "entity_moved",
                    MovingOnJukeboxCartCriterion::new
            );
    public static final DeferredHolder<CriterionTrigger<?>, BlockCartCraftedCriterion> BLOCK_CART_CRAFTED = TRIGGERS
            .register(
                    "block_cart_crafted",
                    BlockCartCraftedCriterion::new
            );

    public static void init(){
        System.out.println("MRModCriteria loaded");
    }
}
