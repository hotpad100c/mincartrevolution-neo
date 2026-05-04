package ml.mypals.minecartrevolution.registeries;


import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import ml.mypals.minecartrevolution.item.DamageCausingMinecartItem;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.item.MultiVariantMinecartWithBlockItem;
import ml.mypals.minecartrevolution.item.ShulkerMinecartItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static ml.mypals.minecartrevolution.MinecartRevolution.*;


public class MRModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);



    public static final ResourceKey<CreativeModeTab> MINECART_REVOLUTION_ITEM_GROUP_KEY =
            ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                    Identifier.fromNamespaceAndPath(MODID, "item_group"));

    public static final Supplier<Item> CACTUS_MINECART = ITEMS.registerItem(
            "minecart_cactus",
            p -> new DamageCausingMinecartItem(
                    AdvancedMinecartEntityTypes.Type.CAUSING_DAMAGE,
                    p.stacksTo(1),
                    1.0f,
                    Blocks.CACTUS,
                    DamageTypes.CACTUS
            )
    );

    public static final Supplier<Item> MAGMA_BLOCK_MINECART = ITEMS.registerItem(
            "minecart_magma",
            p -> new DamageCausingMinecartItem(
                    AdvancedMinecartEntityTypes.Type.CAUSING_DAMAGE,
                    p.stacksTo(1),
                    1.0f,
                    Blocks.MAGMA_BLOCK,
                    DamageTypes.HOT_FLOOR
            )
    );

    public static final Supplier<Item> CAMPFIRE_MINECART = ITEMS.registerItem(
            "minecart_campfire",
            p -> new DamageCausingMinecartItem(
                    AdvancedMinecartEntityTypes.Type.CAUSING_DAMAGE,
                    p.stacksTo(1),
                    2.0f,
                    Blocks.CAMPFIRE,
                    DamageTypes.CAMPFIRE
            )
    );

    public static final Supplier<Item> SOUL_CAMPFIRE_MINECART = ITEMS.registerItem(
            "minecart_soul_campfire",
            p -> new DamageCausingMinecartItem(
                    AdvancedMinecartEntityTypes.Type.CAUSING_DAMAGE,
                    p.stacksTo(1),
                    2.0f,
                    Blocks.SOUL_CAMPFIRE,
                    DamageTypes.CAMPFIRE
            )
    );

    public static final Supplier<Item> REDSTONE_MINECART = ITEMS.registerItem(
            "minecart_redstone",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.EMITTING_POWER,
                    p.stacksTo(1),
                    Blocks.REDSTONE_BLOCK
            )
    );

    public static final Supplier<Item> BLOCK_MINECART = ITEMS.registerItem(
            "minecart_with_block",
            p -> new MultiVariantMinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.REGULAR,
                    p.stacksTo(1),
                    Blocks.GRASS_BLOCK
            )
    );

    public static final Supplier<Item> REPEATER_MINECART = ITEMS.registerItem(
            "minecart_repeater",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.EMITTING_POWER_DIRECTIONAL,
                    p.stacksTo(1),
                    Blocks.REPEATER
            )
    );

    public static final Supplier<Item> SPONGE_MINECART = ITEMS.registerItem(
            "minecart_sponge",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.SPONGE,
                    p.stacksTo(1),
                    Blocks.SPONGE
            )
    );

    public static final Supplier<Item> WET_SPONGE_MINECART = ITEMS.registerItem(
            "minecart_wet_sponge",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.SPONGE,
                    p.stacksTo(1),
                    Blocks.WET_SPONGE
            )
    );

    public static final Supplier<Item> BARREL_MINECART = ITEMS.registerItem(
            "minecart_barrel",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.BARREL,
                    p.stacksTo(1),
                    Blocks.BARREL
            )
    );

    public static final Supplier<Item> TRAPPED_CHEST_MINECART = ITEMS.registerItem(
            "minecart_trapped_chest",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.TRAPPED_CHEST,
                    p.stacksTo(1),
                    Blocks.TRAPPED_CHEST
            )
    );

    public static final Supplier<Item> PRESHER_PLATE_MINECART = ITEMS.registerItem(
            "minecart_presher_plate",
            p -> new MultiVariantMinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.PRESSER_PLATE,
                    p.stacksTo(1),
                    Blocks.OAK_PRESSURE_PLATE
            )
    );

    public static final Supplier<Item> IRON_PRESHER_PLATE_MINECART = ITEMS.registerItem(
            "minecart_iron_presher_plate",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.WEIGHT_PRESSER_PLATE,
                    p.stacksTo(1),
                    Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
            )
    );

    public static final Supplier<Item> GOLDEN_PRESHER_PLATE_MINECART = ITEMS.registerItem(
            "minecart_golden_presher_plate",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.WEIGHT_PRESSER_PLATE,
                    p.stacksTo(1),
                    Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
            )
    );

    public static final Supplier<Item> JUKEBOX_MINECART = ITEMS.registerItem(
            "minecart_jukebox",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.JUKEBOX,
                    p.stacksTo(1),
                    Blocks.JUKEBOX
            )
    );

    public static final Supplier<Item> SHULKER_MINECART = ITEMS.registerItem(
            "minecart_shulker",
            p -> new ShulkerMinecartItem(
                    AdvancedMinecartEntityTypes.Type.SHULKER,
                    p.stacksTo(1),
                    Blocks.SHULKER_BOX
            )
    );

    public static final Supplier<Item> DRAGON_EGG_MINECART = ITEMS.registerItem(
            "minecart_dragon_egg",
            p -> new MinecartWithBlockItem(
                    AdvancedMinecartEntityTypes.Type.DRAGON_EGG,
                    p.stacksTo(1),
                    Blocks.DRAGON_EGG
            )
    );


    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MINECART_REVOLUTION_ITEM_GROUP
            = CREATIVE_MODE_TABS.register("minecart_revolution", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.minecartrevolution"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> new ItemStack(Items.MINECART))
            .displayItems((parameters, itemGroup) -> {
                itemGroup.accept(MRModItems.CACTUS_MINECART.get());
                itemGroup.accept(MRModItems.MAGMA_BLOCK_MINECART.get());
                itemGroup.accept(MRModItems.CAMPFIRE_MINECART.get());
                itemGroup.accept(MRModItems.SOUL_CAMPFIRE_MINECART.get());
                itemGroup.accept(Items.MINECART);
                itemGroup.accept(Items.FURNACE_MINECART);
                itemGroup.accept(Items.HOPPER_MINECART);
                itemGroup.accept(Items.CHEST_MINECART);
                itemGroup.accept(Items.TNT_MINECART);
                itemGroup.accept(MRModItems.REDSTONE_MINECART.get());
                itemGroup.accept(MRModItems.REPEATER_MINECART.get());
                itemGroup.accept(MRModItems.SPONGE_MINECART.get());
                itemGroup.accept(MRModItems.WET_SPONGE_MINECART.get());
                itemGroup.accept(MRModItems.BARREL_MINECART.get());
                itemGroup.accept(MRModItems.TRAPPED_CHEST_MINECART.get());
                itemGroup.accept(MRModItems.BLOCK_MINECART.get());
                itemGroup.accept(MRModItems.PRESHER_PLATE_MINECART.get());
                itemGroup.accept(MRModItems.IRON_PRESHER_PLATE_MINECART.get());
                itemGroup.accept(MRModItems.GOLDEN_PRESHER_PLATE_MINECART.get());
                itemGroup.accept(MRModItems.JUKEBOX_MINECART.get());
                itemGroup.accept(MRModItems.SHULKER_MINECART.get());
                itemGroup.accept(MRModItems.DRAGON_EGG_MINECART.get());
            }).build());

    public static void init(){
        System.out.println("MRModItems loaded");
    }
    public static void registerDispenserBehaviors(){
        DispenserBlock.registerBehavior(MRModItems.CACTUS_MINECART.get(), DamageCausingMinecartItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.CAMPFIRE_MINECART.get(), DamageCausingMinecartItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.MAGMA_BLOCK_MINECART.get(), DamageCausingMinecartItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.SOUL_CAMPFIRE_MINECART.get(), DamageCausingMinecartItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.REDSTONE_MINECART.get(), MinecartWithBlockItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.REPEATER_MINECART.get(), MinecartWithBlockItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.BLOCK_MINECART.get(), MultiVariantMinecartWithBlockItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.SPONGE_MINECART.get(), MinecartWithBlockItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.WET_SPONGE_MINECART.get(), MinecartWithBlockItem.DISPENSER_BEHAVIOR);
        DispenserBlock.registerBehavior(MRModItems.BARREL_MINECART.get(), MinecartWithBlockItem.DISPENSER_BEHAVIOR);
    }
}
