package ml.mypals.minecartrevolution.registeries;

import ml.mypals.minecartrevolution.entity.minecarts.*;
import ml.mypals.minecartrevolution.entity.minecarts.container.*;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MagnetMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.*;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.*;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.BeaconMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.NonInventoryWorkingBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.*;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

public class MRMinecarts {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MODID);
    public static final List<MinecartEntry<?, ?>> MINECARTS = new ArrayList<>();

    public record MinecartEntry<E extends AbstractMinecart, I extends MinecartWithBlockItem>(
            String itemName,
            DeferredHolder<EntityType<?>, EntityType<E>> entity,
            DeferredItem<I> item,
            DispenseItemBehavior dispenseBehavior,
            @Nullable BiFunction<Level, Vec3, AbstractMinecart> spawnFactory
    ) {
    }

    public static <E extends AbstractMinecart, I extends MinecartWithBlockItem> MinecartEntry<E, I> register(
            String baseName,
            EntityType.EntityFactory<E> entityFactory,
            Function<Item.Properties, I> itemFactory
    ) {
        return register(baseName, entityFactory, itemFactory, MinecartWithBlockItem.DISPENSER_BEHAVIOR, null);
    }

    public static <E extends AbstractMinecart, I extends MinecartWithBlockItem> MinecartEntry<E, I> register(
            String baseName,
            EntityType.EntityFactory<E> entityFactory,
            Function<Item.Properties, I> itemFactory,
            DispenseItemBehavior dispenseBehavior
    ) {
        return register(baseName, entityFactory, itemFactory, dispenseBehavior, null);
    }

    public static <E extends AbstractMinecart, I extends MinecartWithBlockItem> MinecartEntry<E, I> register(
            String baseName,
            EntityType.EntityFactory<E> entityFactory,
            Function<Item.Properties, I> itemFactory,
            @Nullable DispenseItemBehavior dispenseBehavior,
            @Nullable BiFunction<DeferredHolder<EntityType<?>, EntityType<E>>, DeferredItem<I>, BiFunction<Level, Vec3, AbstractMinecart>> spawnFactoryBuilder
    ) {
        String entityName = baseName + "_minecart";
        String itemName = "minecart_" + baseName;

        var entity = ENTITIES.register(entityName, () -> EntityType.Builder.of(entityFactory, MobCategory.MISC)
                .sized(0.98F, 0.7F)
                .passengerAttachments(0.1875F)
                .clientTrackingRange(8)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, entityName)))
        );
        var item = ITEMS.registerItem(itemName, itemFactory);
        
        BiFunction<Level, Vec3, AbstractMinecart> spawnFactory = (spawnFactoryBuilder != null)
                ? spawnFactoryBuilder.apply(entity, item)
                : null;

        var entry = new MinecartEntry<>(itemName, entity, item, dispenseBehavior, spawnFactory);
        MINECARTS.add(entry);
        return entry;
    }

    public static <E extends AbstractMinecart, I extends MinecartWithBlockItem> MinecartEntry<E, I> registerItemOnly(
            String itemName,
            DeferredHolder<EntityType<?>, EntityType<E>> existingEntity,
            Function<Item.Properties, I> itemFactory
    ) {
        return registerItemOnly(itemName, existingEntity, itemFactory, MinecartWithBlockItem.DISPENSER_BEHAVIOR, null);
    }

    public static <E extends AbstractMinecart, I extends MinecartWithBlockItem> MinecartEntry<E, I> registerItemOnly(
            String itemName,
            DeferredHolder<EntityType<?>, EntityType<E>> existingEntity,
            Function<Item.Properties, I> itemFactory,
            DispenseItemBehavior dispenseBehavior
    ) {
        return registerItemOnly(itemName, existingEntity, itemFactory, dispenseBehavior, null);
    }

    public static <E extends AbstractMinecart, I extends MinecartWithBlockItem> MinecartEntry<E, I> registerItemOnly(
            String itemName,
            DeferredHolder<EntityType<?>, EntityType<E>> existingEntity,
            Function<Item.Properties, I> itemFactory,
            @Nullable DispenseItemBehavior dispenseBehavior,
            @Nullable BiFunction<DeferredHolder<EntityType<?>, EntityType<E>>, DeferredItem<I>, BiFunction<Level, Vec3, AbstractMinecart>> spawnFactoryBuilder
    ) {
        var item = ITEMS.registerItem(itemName, itemFactory);
        
        BiFunction<Level, Vec3, AbstractMinecart> spawnFactory = (spawnFactoryBuilder != null)
                ? spawnFactoryBuilder.apply(existingEntity, item)
                : null;

        var entry = new MinecartEntry<>(itemName, existingEntity, item, dispenseBehavior, spawnFactory);
        MINECARTS.add(entry);
        return entry;
    }

    public static <E extends AbstractMinecart> DeferredHolder<EntityType<?>, EntityType<E>> registerEntityOnly(
            String entityName,
            EntityType.EntityFactory<E> entityFactory
    ) {
        return ENTITIES.register(entityName, () -> EntityType.Builder.of(entityFactory, MobCategory.MISC)
                .sized(0.98F, 0.7F)
                .passengerAttachments(0.1875F)
                .clientTrackingRange(8)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, entityName)))
        );
    }

    public static final DeferredHolder<EntityType<?>, EntityType<DamageCausingMinecartEntity>> DAMAGE_CAUSING_MINECART = registerEntityOnly(
            "harmful_minecart", DamageCausingMinecartEntity::new);

    public static final DeferredHolder<EntityType<?>, EntityType<RedstoneBlockMinecartEntity>> POWER_PROVIDER_MINECART = registerEntityOnly(
            "power_minecart", RedstoneBlockMinecartEntity::new);

    public static final DeferredHolder<EntityType<?>, EntityType<HorizontalDirectionalRedstoneEmitterPowerMinecartEntity>> DIRECTIONAL_POWER_PROVIDER_MINECART = registerEntityOnly(
            "power_minecart_directional", HorizontalDirectionalRedstoneEmitterPowerMinecartEntity::new);

    public static final DeferredHolder<EntityType<?>, EntityType<PressurePlateMinecartEntity>> PRESHER_PLATE_MINECART = registerEntityOnly(
            "pressure_plate_minecart", PressurePlateMinecartEntity::new);

    public static final DeferredHolder<EntityType<?>, EntityType<WeightPressurePlateMinecartEntity>> WEIGHT_PRESHER_PLATE_MINECART = registerEntityOnly(
            "weight_pressure_plate_minecart", WeightPressurePlateMinecartEntity::new);

    public static final DeferredHolder<EntityType<?>, EntityType<VariantBlockMinecartEntity>> BLOCK_MINECART = registerEntityOnly(
            "block_minecart", VariantBlockMinecartEntity::new);

    public static final DeferredHolder<EntityType<?>, EntityType<NonInventoryWorkingBlockMinecartEntity>> NON_INVENTORY_WORKING_MINECART =
            registerEntityOnly("working_minecart", NonInventoryWorkingBlockMinecartEntity::new);

    public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem> CACTUS_MINECART = registerItemOnly(
            "minecart_cactus", DAMAGE_CAUSING_MINECART,
            p -> new DamageCausingMinecartItem(p.stacksTo(1), 1.0f, Blocks.CACTUS, DamageTypes.CACTUS),
            DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new DamageCausingMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, 1.0f, item.get(), DamageTypes.CACTUS));

    public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem> MAGMA_BLOCK_MINECART = registerItemOnly(
            "minecart_magma", DAMAGE_CAUSING_MINECART,
            p -> new DamageCausingMinecartItem(p.stacksTo(1), 1.0f, Blocks.MAGMA_BLOCK, DamageTypes.HOT_FLOOR),
            DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new DamageCausingMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, 1.0f, item.get(), DamageTypes.HOT_FLOOR));

    public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem> CAMPFIRE_MINECART = registerItemOnly(
            "minecart_campfire", DAMAGE_CAUSING_MINECART,
            p -> new DamageCausingMinecartItem(p.stacksTo(1), 2.0f, Blocks.CAMPFIRE, DamageTypes.CAMPFIRE),
            DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new DamageCausingMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, 2.0f, item.get(), DamageTypes.CAMPFIRE));

    public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem> SOUL_CAMPFIRE_MINECART = registerItemOnly(
            "minecart_soul_campfire", DAMAGE_CAUSING_MINECART,
            p -> new DamageCausingMinecartItem(p.stacksTo(1), 2.0f, Blocks.SOUL_CAMPFIRE, DamageTypes.CAMPFIRE),
            DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new DamageCausingMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, 2.0f, item.get(), DamageTypes.CAMPFIRE));

    public static final MinecartEntry<RedstoneBlockMinecartEntity, MinecartWithBlockItem> REDSTONE_MINECART = registerItemOnly(
            "minecart_redstone", POWER_PROVIDER_MINECART,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.REDSTONE_BLOCK),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new RedstoneBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<HorizontalDirectionalRedstoneEmitterPowerMinecartEntity, MinecartWithBlockItem> REPEATER_MINECART = registerItemOnly(
            "minecart_repeater", DIRECTIONAL_POWER_PROVIDER_MINECART,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.REPEATER),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new HorizontalDirectionalRedstoneEmitterPowerMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<VariantBlockMinecartEntity, MultiVariantMinecartWithBlockItem> BLOCK_MINECART_ITEM = registerItemOnly(
            "minecart_with_block", BLOCK_MINECART,
            p -> new MultiVariantMinecartWithBlockItem(p.stacksTo(1), Blocks.GRASS_BLOCK),
            MultiVariantMinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new VariantBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<PressurePlateMinecartEntity, MultiVariantMinecartWithBlockItem> PRESHER_PLATE_MINECART_ITEM = registerItemOnly(
            "minecart_presher_plate", PRESHER_PLATE_MINECART,
            p -> new MultiVariantMinecartWithBlockItem(p.stacksTo(1), Blocks.OAK_PRESSURE_PLATE),
            null,
            (entity, item) -> (w, pos) -> new PressurePlateMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<WeightPressurePlateMinecartEntity, MinecartWithBlockItem> IRON_PRESHER_PLATE_MINECART = registerItemOnly(
            "minecart_iron_presher_plate", WEIGHT_PRESHER_PLATE_MINECART,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
            null,
            (entity, item) -> (w, pos) -> new WeightPressurePlateMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<WeightPressurePlateMinecartEntity, MinecartWithBlockItem> GOLDEN_PRESHER_PLATE_MINECART = registerItemOnly(
            "minecart_golden_presher_plate", WEIGHT_PRESHER_PLATE_MINECART,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE),
            null,
            (entity, item) -> (w, pos) -> new WeightPressurePlateMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));


    public static final MinecartEntry<SpongeMinecartEntity, MinecartWithBlockItem> SPONGE_MINECART = register(
            "sponge", SpongeMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SPONGE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new SpongeMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, SpongeMinecartEntity.ABSORB_RADIUS, SpongeMinecartEntity.ABSORB_LIMIT, item.get()));

    public static final MinecartEntry<SpongeMinecartEntity, MinecartWithBlockItem> WET_SPONGE_MINECART = registerItemOnly(
            "wet_sponge", SPONGE_MINECART.entity(),
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WET_SPONGE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new SpongeMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, SpongeMinecartEntity.ABSORB_RADIUS, SpongeMinecartEntity.ABSORB_LIMIT, item.get()));

    public static final MinecartEntry<BarrelMinecartEntity, MinecartWithBlockItem> BARREL_MINECART = register(
            "barrel", BarrelMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BARREL),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new BarrelMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z));

    public static final MinecartEntry<TrappedChestMinecartEntity, MinecartWithBlockItem> TRAPPED_CHEST_MINECART = register(
            "trapped_chest", TrappedChestMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.TRAPPED_CHEST),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new TrappedChestMinecartEntity(w, pos.x, pos.y, pos.z));

    public static final MinecartEntry<CopperChestMinecartEntity, MinecartWithBlockItem> COPPER_CHEST_MINECART = register(
            "copper_chest", CopperChestMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.COPPER_CHEST),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new CopperChestMinecartEntity(w, pos.x, pos.y, pos.z));

    public static final MinecartEntry<MinecartFurnace, MinecartWithBlockItem> BLAST_FURNACE_MINECART = registerItemOnly(
            "blast_furnace", DeferredHolder.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("minecraft", "furnace_minecart")),
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BLAST_FURNACE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> {
                MinecartFurnace m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
                m.setInitialPos(pos.x, pos.y, pos.z);
                m.setCustomDisplayBlockState(Optional.of(Blocks.BLAST_FURNACE.defaultBlockState()));
                return m;
            });

    public static final MinecartEntry<MinecartFurnace, MinecartWithBlockItem> SMOKER_MINECART = registerItemOnly(
            "smoker", DeferredHolder.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("minecraft", "furnace_minecart")),
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SMOKER),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> {
                MinecartFurnace m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
                m.setInitialPos(pos.x, pos.y, pos.z);
                m.setCustomDisplayBlockState(Optional.of(Blocks.SMOKER.defaultBlockState()));
                return m;
            });

    public static final MinecartEntry<JukeboxMinecartEntity, MinecartWithBlockItem> JUKEBOX_MINECART = register(
            "jukebox", JukeboxMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.JUKEBOX),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new JukeboxMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<ShulkerMinecartEntity, ShulkerMinecartItem> SHULKER_MINECART = register(
            "shulker", ShulkerMinecartEntity::new,
            p -> new ShulkerMinecartItem(p.stacksTo(1), Blocks.SHULKER_BOX),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new ShulkerMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, (net.minecraft.world.level.block.ShulkerBoxBlock)Blocks.SHULKER_BOX));

    public static final MinecartEntry<DragonEggMinecart, MinecartWithBlockItem> DRAGON_EGG_MINECART = register(
            "dragon_egg", DragonEggMinecart::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DRAGON_EGG),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new DragonEggMinecart(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<CobwebMinecartEntity, MinecartWithBlockItem> COBWEB_MINECART = register(
            "cobweb", CobwebMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.COBWEB),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new CobwebMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> SMITHING_TABLE_MINECART = register(
            "smithing_table", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SMITHING_TABLE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> CRAFTING_TABLE_MINECART = register(
            "crafting_table", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CRAFTING_TABLE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> STONECUTTER_MINECART = register(
            "stonecutter", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.STONECUTTER),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> LOOM_MINECART = register(
            "loom", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LOOM),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> CARTOGRAPHY_TABLE_MINECART = register(
            "cartography_table", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CARTOGRAPHY_TABLE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> GRINDSTONE_MINECART = register(
            "grindstone", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.GRINDSTONE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> ANVIL_MINECART = register(
            "anvil", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ANVIL),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> ENCHANTING_TABLE_MINECART = register(
            "enchanting_table", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ENCHANTING_TABLE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem> ENDER_CHEST_MINECART = register(
            "ender_chest", NonInventoryWorkingBlockMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ENDER_CHEST),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new NonInventoryWorkingBlockMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));


    public static final MinecartEntry<BeaconMinecartEntity, MinecartWithBlockItem> BEACON_MINECART = register(
            "beacon", BeaconMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BEACON),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new BeaconMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<DispenserMinecartEntity, MinecartWithBlockItem> DISPENSER_MINECART = register(
            "dispenser", DispenserMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DISPENSER),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new DispenserMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z));

    public static final MinecartEntry<FluidMinecartEntity, MinecartWithBlockItem> WATER_MINECART = register(
            "water", (type, world) -> new FluidMinecartEntity(type, world, Items.WATER_BUCKET),
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WATER),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new FluidMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<FluidMinecartEntity, MinecartWithBlockItem> LAVA_MINECART = register(
            "lava", (type, world) -> new FluidMinecartEntity(type, world, Items.LAVA_BUCKET),
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LAVA),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new FluidMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<MagnetMinecartEntity, MinecartWithBlockItem> MAGNET_MINECART = register(
            "magnet", MagnetMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LODESTONE),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new MagnetMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<MobHeadMinecartEntity, MinecartWithBlockItem> MOB_HEAD_MINECART = register(
            "mob_head", MobHeadMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DRAGON_HEAD),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new MobHeadMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<SofaMinecart, MinecartWithBlockItem> SOFA_MINECART = register(
            "sofa", SofaMinecart::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BEDROCK),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new SofaMinecart(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
    public static final MinecartEntry<WoolMinecartEntity, MinecartWithBlockItem> WOOL_MINECART = register(
            "wool", WoolMinecartEntity::new,
            p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WHITE_WOOL),
            MinecartWithBlockItem.DISPENSER_BEHAVIOR,
            (entity, item) -> (w, pos) -> new WoolMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
}
