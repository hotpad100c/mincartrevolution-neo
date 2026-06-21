package ml.mypals.minecartrevolution.registeries;

import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.advancements.criterion.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MRModCriteria {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MinecartRevolution.MODID);

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

    public static final DeferredHolder<CriterionTrigger<?>, SofaAwayCriterion> SOFA_AWAY = TRIGGERS.
            register("sofa_away", SofaAwayCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, NoGravityCriterion> NO_GRAVITY = TRIGGERS.
            register("no_gravity", NoGravityCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, BabelTowerCriterion> IS_THAT_BABEL_TOWER = TRIGGERS.
            register("is_that_babel_tower", BabelTowerCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, BabelCriterion> BABEL = TRIGGERS.
            register("babel", BabelCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, DjangoUnchainedCriterion> DJANGO_UNCHAINED = TRIGGERS.
            register("django_unchained", DjangoUnchainedCriterion::new);
    public static void init() {
        System.out.println("MRModCriteria loaded");
    }
}
