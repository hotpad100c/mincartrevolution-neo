package ml.mypals.minecartrevolution.datagen;

import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.advancements.criterion.BlockCartCraftedCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.MovingOnJukeboxCartCriterion;
import ml.mypals.minecartrevolution.advancements.criterion.MRModCriteria;
import ml.mypals.minecartrevolution.entity.minecarts.MRModEntities;
import ml.mypals.minecartrevolution.item.MRModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.StartRidingTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Consumer;

public class MRAdvancementProvider {

    public static void generate(HolderLookup.@NonNull Provider registries, @NonNull Consumer<AdvancementHolder> saver) {


        HolderGetter<EntityType<?>> entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);

        AdvancementHolder getBlockMinecart = Advancement.Builder.advancement()
                .display(
                        Items.MINECART,
                        Component.literal("矿车革命"),
                        Component.literal("那个版本更新的？？"),
                        Identifier.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_block_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.BLOCK_MINECART.get().getDescriptionId(),
                                        true
                                ))
                )
                .save(saver, "got_block_minecart");

        AdvancementHolder getJukeboxMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRModItems.JUKEBOX_MINECART.get(),
                        Component.literal("音轨"),
                        Component.literal("获得唱片机矿车"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_jukebox_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.JUKEBOX_MINECART.get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "got_jukebox_minecart");

        AdvancementHolder getDragonEggMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        Items.DRAGON_EGG,
                        Component.literal("这是摇篮吗？"),
                        Component.literal("它需要一些刺激"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_dragon_egg_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.DRAGON_EGG_MINECART.get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "craft_dragon_egg_minecart");

        AdvancementHolder getDangerousMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        Items.DRAGON_EGG,
                        Component.literal("敌意满满"),
                        Component.literal("我最好不要靠近它.."),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("got_dangerous_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.get().createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.DAMAGE_CAUSING_MINECART.get().getDescriptionId(),
                                        false
                                ))
                )
                .save(saver, "got_dangerous_minecart");

        AdvancementHolder sitOnDragonEggMinecart = Advancement.Builder.advancement()
                .parent(getDragonEggMinecart)
                .display(
                        Items.DRAGON_EGG,
                        Component.literal("孵蛋"),
                        Component.literal("坐在龙蛋矿车上"),
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
                                                MRModEntities.DRAGON_EGG_MINECART.get()).
                                                passenger(EntityPredicate.Builder.entity().of(entityTypes,
                                                EntityType.PLAYER)))
                                        ))
                                ))
                )
                .save(saver, "ride_dragon_egg_minecart");

        AdvancementHolder jukeboxMinecartMoved = Advancement.Builder.advancement()
                .parent(getJukeboxMinecart)
                .display(
                        MRModItems.JUKEBOX_MINECART.get(),
                        Component.literal("余音绕轨"),
                        Component.literal("坐上移动的唱片机矿车进行巡回演出"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true
                )
                .addCriterion("move_on_jukebox_minecart",
                        MRModCriteria.ENTITY_MOVED.get().createCriterion(
                                new MovingOnJukeboxCartCriterion.Conditions(Optional.empty(),
                                        MRModEntities.JUKEBOX_MINECART.get().getDescriptionId()
                                ))
                )
                .save(saver, "move_on_jukebox_minecart");
    }
}