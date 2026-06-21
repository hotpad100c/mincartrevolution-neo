package ml.mypals.minecartrevolution.registeries;

import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.advancements.criterion.BabelCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BabelTowerCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BeaconActivatedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BlockCartCraftedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.HoneyMinecartStuckCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.MovingOnJukeboxCartCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.NoGravityCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.PortalMinecartTeleportCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.SofaAwayCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.SpongeAbsorbedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MRModCriteria {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MinecartRevolution.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, MovingOnJukeboxCartCriterion> ENTITY_MOVED = TRIGGERS
            .register("entity_moved", MovingOnJukeboxCartCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, BlockCartCraftedCriterion> BLOCK_CART_CRAFTED = TRIGGERS
            .register("block_cart_crafted", BlockCartCraftedCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, SofaAwayCriterion> SOFA_AWAY = TRIGGERS
            .register("sofa_away", SofaAwayCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, NoGravityCriterion> NO_GRAVITY = TRIGGERS
            .register("no_gravity", NoGravityCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, BabelTowerCriterion> IS_THAT_BABEL_TOWER = TRIGGERS
            .register("is_that_babel_tower", BabelTowerCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, BabelCriterion> BABEL = TRIGGERS
            .register("babel", BabelCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, DjangoUnchainedCriterion> DJANGO_UNCHAINED = TRIGGERS.
            register("django_unchained", DjangoUnchainedCriterion::new);
    // Behavior-triggered criteria
    public static final DeferredHolder<CriterionTrigger<?>, SpongeAbsorbedCriterion> SPONGE_ABSORBED = TRIGGERS
            .register("sponge_absorbed", SpongeAbsorbedCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, PortalMinecartTeleportCriterion> PORTAL_MINECART_TELEPORT = TRIGGERS
            .register("portal_minecart_teleport", PortalMinecartTeleportCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, BeaconActivatedCriterion> BEACON_ACTIVATED = TRIGGERS
            .register("beacon_activated", BeaconActivatedCriterion::new);
    public static final DeferredHolder<CriterionTrigger<?>, HoneyMinecartStuckCriterion> HONEY_STUCK = TRIGGERS
            .register("honey_stuck", HoneyMinecartStuckCriterion::new);

    public static void init() {
        System.out.println("MRModCriteria loaded");
    }
}
