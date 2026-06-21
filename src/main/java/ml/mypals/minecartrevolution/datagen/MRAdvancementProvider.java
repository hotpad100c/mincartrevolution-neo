package ml.mypals.minecartrevolution.datagen;

import ml.mypals.minecartrevolution.advancements.criterion.BeaconActivatedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BlockCartCraftedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.HoneyMinecartStuckCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.MovingOnJukeboxCartCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.NoGravityCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.PortalMinecartTeleportCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.SofaAwayCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BabelTowerCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.BabelCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.SpongeAbsorbedCriterion;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
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
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

public class MRAdvancementProvider {

    private static final String PREFIX = "advancement.minecartrevolution.";
    private static final Identifier BG_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "gui/advancements/backgrounds/minecart.png");

    private static Component title(String advancementId) {
        return Component.translatable(PREFIX + advancementId + ".title");
    }

    private static Component desc(String advancementId) {
        return Component.translatable(PREFIX + advancementId + ".description");
    }

    /** Helper: craft-trigger advancement using BlockCartCraftedCriterion */
    private static AdvancementHolder craftAdv(
            AdvancementHolder parent,
            net.minecraft.world.level.ItemLike icon,
            String id,
            String descriptionId,
            AdvancementType type,
            boolean announce,
            Consumer<AdvancementHolder> saver
    ) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(id), desc(id), null, type, true, announce, false)
                .addCriterion("craft_" + id,
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(), descriptionId, false)))
                .save(saver, id);
    }

    public static void generate(HolderLookup.@NotNull Provider registries, @NotNull Consumer<AdvancementHolder> saver) {

        HolderGetter<EntityType<?>> entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);

        // ─── ROOT ────────────────────────────────────────────────────────────
        AdvancementHolder getBlockMinecart = Advancement.Builder.advancement()
                .display(
                        Items.MINECART,
                        title("got_block_minecart"),
                        desc("got_block_minecart"),
                        BG_TEXTURE,
                        AdvancementType.GOAL,
                        true, true, false
                )
                .addCriterion("craft_block_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.BLOCK_MINECART.get().getDescriptionId(), true)))
                .save(saver, "got_block_minecart");

        // ─── JUKEBOX BRANCH ──────────────────────────────────────────────────
        AdvancementHolder getJukeboxMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.JUKEBOX_MINECART.item().get(),
                "got_jukebox_minecart",
                MRMinecarts.JUKEBOX_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        AdvancementHolder jukeboxMinecartMoved = Advancement.Builder.advancement()
                .parent(getJukeboxMinecart)
                .display(MRMinecarts.JUKEBOX_MINECART.item().get(), title("move_on_jukebox_minecart"), desc("move_on_jukebox_minecart"),
                        null, AdvancementType.CHALLENGE, true, true, true)
                .addCriterion("move_on_jukebox_minecart",
                        MRModCriteria.ENTITY_MOVED.get().createCriterion(
                                new MovingOnJukeboxCartCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.JUKEBOX_MINECART.entity().get().getDescriptionId())))
                .save(saver, "move_on_jukebox_minecart");

        // Amethyst Minecart — satellite of the jukebox branch
        craftAdv(getJukeboxMinecart,
                MRMinecarts.AMETHYST_MINECART.item().get(),
                "got_amethyst_minecart",
                MRMinecarts.AMETHYST_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        // ─── OBSERVER / ANT BRANCH ───────────────────────────────────────────
        craftAdv(getBlockMinecart,
                MRMinecarts.OBSERVER_MINECART.item().get(),
                "got_observer_minecart",
                MRMinecarts.OBSERVER_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        // ─── OBSIDIAN BRANCH ─────────────────────────────────────────────────
        craftAdv(getBlockMinecart,
                MRMinecarts.OBSIDIAN_MINECART.item().get(),
                "got_lava_proof_minecart",
                MRMinecarts.OBSIDIAN_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        // ─── DRAGON EGG BRANCH ───────────────────────────────────────────────
        AdvancementHolder getDragonEggMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(Items.DRAGON_EGG, title("craft_dragon_egg_minecart"), desc("craft_dragon_egg_minecart"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("craft_dragon_egg_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.DRAGON_EGG_MINECART.entity().get().getDescriptionId(), false)))
                .save(saver, "craft_dragon_egg_minecart");

        Advancement.Builder.advancement()
                .parent(getDragonEggMinecart)
                .display(Items.DRAGON_EGG, title("ride_dragon_egg_minecart"), desc("ride_dragon_egg_minecart"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("ride_dragon_egg_minecart",
                        CriteriaTriggers.START_RIDING_TRIGGER.createCriterion(
                                new StartRidingTrigger.TriggerInstance(
                                        Optional.of(EntityPredicate.wrap(
                                                EntityPredicate.Builder.entity()
                                                        .vehicle(EntityPredicate.Builder.entity()
                                                                .of(entityTypes, MRMinecarts.DRAGON_EGG_MINECART.entity().get())
                                                                .passenger(EntityPredicate.Builder.entity()
                                                                        .of(entityTypes, EntityType.PLAYER))))))))
                .save(saver, "ride_dragon_egg_minecart");

        // ─── DANGEROUS MINECART BRANCH ───────────────────────────────────────
        AdvancementHolder getDangerousMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(MRMinecarts.CACTUS_MINECART.item().get(), title("got_dangerous_minecart"), desc("got_dangerous_minecart"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("got_dangerous_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRMinecarts.DAMAGE_CAUSING_MINECART.get().getDescriptionId(), false)))
                .save(saver, "got_dangerous_minecart");

        // Lava Minecart — dangerously hot cousin
        craftAdv(getDangerousMinecart,
                MRMinecarts.LAVA_MINECART.item().get(),
                "got_lava_minecart",
                MRMinecarts.LAVA_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        // ─── SOFA BRANCH ─────────────────────────────────────────────────────
        Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(MRMinecarts.SOFA_MINECART.item().get(), title("sofa_away_logic"), desc("sofa_away_logic"),
                        null, AdvancementType.CHALLENGE, true, true, true)
                .addCriterion("sofa_away_criterion", SofaAwayCriterion.TriggerInstance.sofaAway())
                .save(saver, "sofa_away_logic");

        // ─── WOOL (NO GRAVITY) BRANCH ────────────────────────────────────────
        Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(MRMinecarts.WOOL_MINECART.item().get(), title("no_gravity_logic"), desc("no_gravity_logic"),
                        null, AdvancementType.CHALLENGE, true, true, true)
                .addCriterion("no_gravity_criterion", NoGravityCriterion.TriggerInstance.noGravity())
                .save(saver, "no_gravity_logic");

        // ─── RAIL / BABEL BRANCH ─────────────────────────────────────────────
        AdvancementHolder isThatBabelTower = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(MRMinecarts.NORMAL_RAIL_MINECART.item().get(), title("is_that_babel_tower"), desc("is_that_babel_tower"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("is_that_babel_tower_criterion", BabelTowerCriterion.TriggerInstance.trigger())
                .save(saver, "is_that_babel_tower");

        Advancement.Builder.advancement()
                .parent(isThatBabelTower)
                .display(MRMinecarts.NORMAL_RAIL_MINECART.item().get(), title("babel"), desc("babel"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("babel_criterion", BabelCriterion.TriggerInstance.trigger())
                .save(saver, "babel");

        // ─── SPONGE BRANCH ───────────────────────────────────────────────────
        AdvancementHolder getSpongeMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.SPONGE_MINECART.item().get(),
                "got_sponge_minecart",
                MRMinecarts.SPONGE_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        Advancement.Builder.advancement()
                .parent(getSpongeMinecart)
                .display(Blocks.WET_SPONGE, title("sponge_absorbed"), desc("sponge_absorbed"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("sponge_absorbed_criterion", SpongeAbsorbedCriterion.TriggerInstance.spongeAbsorbed())
                .save(saver, "sponge_absorbed");

        // ─── CONTAINER MINECART BRANCH ───────────────────────────────────────
        AdvancementHolder getBarrelMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.BARREL_MINECART.item().get(),
                "got_barrel_minecart",
                MRMinecarts.BARREL_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBarrelMinecart,
                MRMinecarts.TRAPPED_CHEST_MINECART.item().get(),
                "got_trapped_chest_minecart",
                MRMinecarts.TRAPPED_CHEST_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBarrelMinecart,
                MRMinecarts.COPPER_CHEST_MINECART.item().get(),
                "got_copper_chest_minecart",
                MRMinecarts.COPPER_CHEST_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        AdvancementHolder getShulkerMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.SHULKER_MINECART.item().get(),
                "got_shulker_minecart",
                MRMinecarts.SHULKER_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        craftAdv(getShulkerMinecart,
                MRMinecarts.ENDER_CHEST_MINECART.item().get(),
                "got_ender_chest_minecart",
                MRMinecarts.ENDER_CHEST_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.DISPENSER_MINECART.item().get(),
                "got_dispenser_minecart",
                MRMinecarts.DISPENSER_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        // ─── BEACON BRANCH ───────────────────────────────────────────────────
        AdvancementHolder getBeaconMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.BEACON_MINECART.item().get(),
                "got_beacon_minecart",
                MRMinecarts.BEACON_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        Advancement.Builder.advancement()
                .parent(getBeaconMinecart)
                .display(Blocks.BEACON, title("beacon_activated"), desc("beacon_activated"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("beacon_activated_criterion", BeaconActivatedCriterion.TriggerInstance.beaconActivated())
                .save(saver, "beacon_activated");

        // ─── HONEY BRANCH ────────────────────────────────────────────────────
        AdvancementHolder getHoneyMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.HONEY_MINECART.item().get(),
                "got_honey_minecart",
                MRMinecarts.HONEY_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        Advancement.Builder.advancement()
                .parent(getHoneyMinecart)
                .display(Blocks.HONEY_BLOCK, title("honey_stuck"), desc("honey_stuck"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("honey_stuck_criterion", HoneyMinecartStuckCriterion.TriggerInstance.stuck())
                .save(saver, "honey_stuck");

        // ─── PORTAL BRANCH ───────────────────────────────────────────────────
        AdvancementHolder getPortalMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.PORTAL_MINECART.item().get(),
                "got_portal_minecart",
                MRMinecarts.PORTAL_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        Advancement.Builder.advancement()
                .parent(getPortalMinecart)
                .display(Items.ENDER_PEARL, title("portal_teleport"), desc("portal_teleport"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("portal_teleport_criterion", PortalMinecartTeleportCriterion.TriggerInstance.teleported())
                .save(saver, "portal_teleport");

        craftAdv(getPortalMinecart,
                MRMinecarts.ENDER_PORTAL_MINECART.item().get(),
                "got_ender_portal_minecart",
                MRMinecarts.ENDER_PORTAL_MINECART.entity().get().getDescriptionId(),
                AdvancementType.CHALLENGE, true, saver);

        // ─── UTILITY MINECARTS ───────────────────────────────────────────────
        craftAdv(getBlockMinecart,
                MRMinecarts.MAGNET_MINECART.item().get(),
                "got_magnet_minecart",
                MRMinecarts.MAGNET_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.PISTON_MINECART.item().get(),
                "got_piston_minecart",
                MRMinecarts.PISTON_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.SCAFFOLD_MINECART.item().get(),
                "got_scaffold_minecart",
                MRMinecarts.SCAFFOLD_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.COBWEB_MINECART.item().get(),
                "got_cobweb_minecart",
                MRMinecarts.COBWEB_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.WATER_MINECART.item().get(),
                "got_water_minecart",
                MRMinecarts.WATER_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.MOB_HEAD_MINECART.item().get(),
                "got_mob_head_minecart",
                MRMinecarts.MOB_HEAD_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.ENCHANTING_TABLE_MINECART.item().get(),
                "got_enchanting_table_minecart",
                MRMinecarts.ENCHANTING_TABLE_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        AdvancementHolder getAnvilMinecart = craftAdv(
                getBlockMinecart,
                MRMinecarts.ANVIL_MINECART.item().get(),
                "got_anvil_minecart",
                MRMinecarts.ANVIL_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        // Ride the anvil minecart — brave soul
        Advancement.Builder.advancement()
                .parent(getAnvilMinecart)
                .display(Blocks.ANVIL, title("ride_anvil_minecart"), desc("ride_anvil_minecart"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("ride_anvil_minecart",
                        CriteriaTriggers.START_RIDING_TRIGGER.createCriterion(
                                new StartRidingTrigger.TriggerInstance(
                                        Optional.of(EntityPredicate.wrap(
                                                EntityPredicate.Builder.entity()
                                                        .vehicle(EntityPredicate.Builder.entity()
                                                                .of(entityTypes, MRMinecarts.ANVIL_MINECART.entity().get())
                                                                .passenger(EntityPredicate.Builder.entity()
                                                                        .of(entityTypes, EntityType.PLAYER))))))))
                .save(saver, "ride_anvil_minecart");

        // Working block minecarts
        craftAdv(getBlockMinecart,
                MRMinecarts.CRAFTING_TABLE_MINECART.item().get(),
                "got_crafting_table_minecart",
                MRMinecarts.CRAFTING_TABLE_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.SMITHING_TABLE_MINECART.item().get(),
                "got_smithing_table_minecart",
                MRMinecarts.SMITHING_TABLE_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);

        craftAdv(getBlockMinecart,
                MRMinecarts.PICKER_MINECART.item().get(),
                "got_picker_minecart",
                MRMinecarts.PICKER_MINECART.entity().get().getDescriptionId(),
                AdvancementType.TASK, false, saver);
    }
}