package ml.mypals.minecartrevolution.datagen;

import ml.mypals.minecartrevolution.advancements.criterion.BlockCartCraftedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.MovingOnJukeboxCartCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.NoGravityCriterion;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.advancements.criterion.SofaAwayCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BabelTowerCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BabelCriterion;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.StartRidingTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

public class MRAdvancementProvider {

    private static final String PREFIX = "advancement.minecartrevolution.";
    private static final Identifier BG_TEXTURE = Identifier.fromNamespaceAndPath(MODID,"gui/advancements/backgrounds/minecart.png");
    private static Component title(String advancementId) {
        return Component.translatable(PREFIX + advancementId + ".title");
    }

    private static Component desc(String advancementId) {
        return Component.translatable(PREFIX + advancementId + ".description");
    }

    public static void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<AdvancementHolder> saver) {

        HolderGetter<EntityType<?>> entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);

        AdvancementHolder getBlockMinecart = Advancement.Builder.advancement()
                .display(
                        Items.MINECART,
                        title("got_block_minecart"),
                        desc("got_block_minecart"),
                        BG_TEXTURE,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_block_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.BLOCK_MINECART.get().getDescriptionId(),
                                        true
                                ))
                )
                .save(saver, "got_block_minecart");

        AdvancementHolder getJukeboxMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRMinecarts.JUKEBOX_MINECART.item().get(),
                        title("got_jukebox_minecart"),
                        desc("got_jukebox_minecart"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_jukebox_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.JUKEBOX_MINECART.entity().get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "got_jukebox_minecart");

        AdvancementHolder getAntMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRMinecarts.OBSERVER_MINECART.item().get(),
                        title("got_observer_minecart"),
                        desc("got_observer_minecart"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_observer_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.OBSERVER_MINECART.entity().get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "got_observer_minecart");

        AdvancementHolder getLavaProofMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRMinecarts.OBSIDIAN_MINECART.item().get(),
                        title("got_lava_proof_minecart"),
                        desc("got_lava_proof_minecart"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_lava_proof_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.OBSIDIAN_MINECART.entity().get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "got_lava_proof_minecart");

        AdvancementHolder getDragonEggMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        Items.DRAGON_EGG,
                        title("craft_dragon_egg_minecart"),
                        desc("craft_dragon_egg_minecart"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_dragon_egg_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.DRAGON_EGG_MINECART.entity().get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "craft_dragon_egg_minecart");

        AdvancementHolder getDangerousMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRMinecarts.CACTUS_MINECART.item(),
                        title("got_dangerous_minecart"),
                        desc("got_dangerous_minecart"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("got_dangerous_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.DAMAGE_CAUSING_MINECART.get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "got_dangerous_minecart");

        AdvancementHolder sitOnDragonEggMinecart = Advancement.Builder.advancement()
                .parent(getDragonEggMinecart)
                .display(
                        Items.DRAGON_EGG,
                        title("ride_dragon_egg_minecart"),
                        desc("ride_dragon_egg_minecart"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("ride_dragon_egg_minecart",
                        CriteriaTriggers.START_RIDING_TRIGGER.createCriterion(
                                new StartRidingTrigger.TriggerInstance(
                                        Optional.of(EntityPredicate.wrap(
                                                EntityPredicate.Builder.entity()
                                                        .vehicle(EntityPredicate.Builder.entity().of(entityTypes,
                                                                        MRMinecarts.DRAGON_EGG_MINECART.entity().get()).
                                                                passenger(EntityPredicate.Builder.entity().of(entityTypes,
                                                                        EntityType.PLAYER)))
                                        )))
                        )
                )
                .save(saver, "ride_dragon_egg_minecart");

        AdvancementHolder jukeboxMinecartMoved = Advancement.Builder.advancement()
                .parent(getJukeboxMinecart)
                .display(
                        MRMinecarts.JUKEBOX_MINECART.item().get(),
                        title("move_on_jukebox_minecart"),
                        desc("move_on_jukebox_minecart"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true
                )
                .addCriterion("move_on_jukebox_minecart",
                        MRModCriteria.ENTITY_MOVED.get().createCriterion(
                                new MovingOnJukeboxCartCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.JUKEBOX_MINECART.entity().get().getDescriptionId()
                                ))
                )
                .save(saver, "move_on_jukebox_minecart");

        AdvancementHolder sofaAway = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRMinecarts.SOFA_MINECART.item().get(),
                        title("sofa_away_logic"),
                        desc("sofa_away_logic"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true
                )
                .addCriterion("sofa_away_criterion", SofaAwayCriterion.TriggerInstance.sofaAway())
                .save(saver, "sofa_away_logic");

        AdvancementHolder noGravity = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRMinecarts.WOOL_MINECART.item().get(),
                        title("no_gravity_logic"),
                        desc("no_gravity_logic"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true
                )
                .addCriterion("no_gravity_criterion", NoGravityCriterion.TriggerInstance.noGravity())
                .save(saver, "no_gravity_logic");

        AdvancementHolder isThatBabelTower = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRMinecarts.NORMAL_RAIL_MINECART.item().get(),
                        title("is_that_babel_tower"),
                        desc("is_that_babel_tower"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("is_that_babel_tower_criterion", BabelTowerCriterion.TriggerInstance.trigger())
                .save(saver, "is_that_babel_tower");

        AdvancementHolder babel = Advancement.Builder.advancement()
                .parent(isThatBabelTower)
                .display(
                        MRMinecarts.NORMAL_RAIL_MINECART.item().get(),
                        title("babel"),
                        desc("babel"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("babel_criterion", BabelCriterion.TriggerInstance.trigger())
                .save(saver, "babel");
    }
}