package ml.mypals.minecartrevolution.datagen;

import mypals.ml.MinecartRevolution;
import mypals.ml.advancements.criterion.BlockCartCraftedCriterion;
import mypals.ml.advancements.criterion.MovingOnJukeboxCartCriterion;
import mypals.ml.advancements.criterion.MRModCriteria;
import mypals.ml.entity.minecarts.MRModEntities;
import mypals.ml.item.MRModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MRAdvancementProvider extends FabricAdvancementProvider {
    protected MRAdvancementProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider wrapperLookup, Consumer<AdvancementHolder> consumer) {
        AdvancementHolder getBlockMinecart = Advancement.Builder.advancement()
                .display(
                        Items.MINECART,
                        Component.literal("矿车革命"),
                        Component.literal("那个版本更新的？？"),
                        ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_block_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.BLOCK_MINECART.getDescriptionId(),
                                        true
                                ))
                )
                .save(consumer, MinecartRevolution.idString("got_block_minecart"));
        AdvancementHolder getJukeboxMinecart = Advancement.Builder.advancement()
                .parent(getBlockMinecart)
                .display(
                        MRModItems.JUKEBOX_MINECART,
                        Component.literal("音轨"),
                        Component.literal("获得唱片机矿车"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("craft_jukebox_minecart",
                        MRModCriteria.BLOCK_CART_CRAFTED.createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.JUKEBOX_MINECART.getDescriptionId(),
                                        false
                                ))
                )

                .save(consumer, MinecartRevolution.idString("got_jukebox_minecart"));
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
                        MRModCriteria.BLOCK_CART_CRAFTED.createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.DRAGON_EGG_MINECART.getDescriptionId(),
                                        false
                                ))
                )

                .save(consumer, MinecartRevolution.idString("craft_dragon_egg_minecart"));
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
                        MRModCriteria.BLOCK_CART_CRAFTED.createCriterion(
                                new BlockCartCraftedCriterion.Conditions(Optional.empty(),
                                        MRModEntities.DAMAGE_CAUSING_MINECART.getDescriptionId(),
                                        false
                                ))
                )

                .save(consumer, MinecartRevolution.idString("got_dangerous_minecart"));
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
                                                        .vehicle(EntityPredicate.Builder.entity().of(
                                                                MRModEntities.DRAGON_EGG_MINECART).
                                                                passenger(EntityPredicate.Builder.entity().of(EntityType.PLAYER)))
                                        ))
                                ))
                )

                .save(consumer, MinecartRevolution.idString("ride_dragon_egg_minecart"));

        AdvancementHolder jukeboxMinecartMoved = Advancement.Builder.advancement()
                .parent(getJukeboxMinecart)
                .display(
                        MRModItems.JUKEBOX_MINECART,
                        Component.literal("余音绕轨"),
                        Component.literal("坐上移动的唱片机矿车进行巡回演出"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true
                )

                .addCriterion("move_on_jukebox_minecart",
                        MRModCriteria.ENTITY_MOVED.createCriterion(
                                new MovingOnJukeboxCartCriterion.Conditions(Optional.empty(),
                                        MRModEntities.JUKEBOX_MINECART.getDescriptionId()
                                ))
                )
                .save(consumer, MinecartRevolution.idString("move_on_jukebox_minecart"));
    }
}
