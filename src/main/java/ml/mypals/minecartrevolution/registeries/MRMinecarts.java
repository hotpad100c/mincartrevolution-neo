package ml.mypals.minecartrevolution.registeries;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import ml.mypals.minecartrevolution.entity.chain.ChainEntity;
import ml.mypals.minecartrevolution.entity.minecarts.*;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.*;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.*;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.*;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.*;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

public class MRMinecarts {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
  public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MODID);
  public static final List<MinecartEntry<?, ?>> MINECARTS = new ArrayList<>();

  public record MinecartEntry<E extends AbstractMinecart, I extends MinecartWithBlockItem>(
      String itemName,
      DeferredHolder<EntityType<?>, EntityType<E>> entity,
      DeferredItem<I> item,
      DispenseItemBehavior dispenseBehavior,
      @Nullable BiFunction<Level, Vec3, AbstractMinecart> spawnFactory) {}

  public static <E extends AbstractMinecart, I extends MinecartWithBlockItem>
      MinecartEntry<E, I> register(
          String baseName,
          EntityType.EntityFactory<E> entityFactory,
          Function<Item.Properties, I> itemFactory) {
    return register(
        baseName, entityFactory, itemFactory, MinecartWithBlockItem.DISPENSER_BEHAVIOR, null);
  }

  public static <E extends AbstractMinecart, I extends MinecartWithBlockItem>
      MinecartEntry<E, I> register(
          String baseName,
          EntityType.EntityFactory<E> entityFactory,
          Function<Item.Properties, I> itemFactory,
          DispenseItemBehavior dispenseBehavior) {
    return register(baseName, entityFactory, itemFactory, dispenseBehavior, null);
  }

  public static <E extends AbstractMinecart, I extends MinecartWithBlockItem>
      MinecartEntry<E, I> register(
          String baseName,
          EntityType.EntityFactory<E> entityFactory,
          Function<Item.Properties, I> itemFactory,
          @Nullable DispenseItemBehavior dispenseBehavior,
          @Nullable
              BiFunction<
                      DeferredHolder<EntityType<?>, EntityType<E>>,
                      DeferredItem<I>,
                      BiFunction<Level, Vec3, AbstractMinecart>>
                  spawnFactoryBuilder) {
    String entityName = baseName + "_minecart";
    String itemName = "minecart_" + baseName;

    var entity =
        ENTITIES.register(
            entityName,
            () ->
                EntityType.Builder.of(entityFactory, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(
                        ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(MODID, entityName))));
    var item = ITEMS.registerItem(itemName, itemFactory);

    BiFunction<Level, Vec3, AbstractMinecart> spawnFactory =
        (spawnFactoryBuilder != null) ? spawnFactoryBuilder.apply(entity, item) : null;

    var entry = new MinecartEntry<>(itemName, entity, item, dispenseBehavior, spawnFactory);
    MINECARTS.add(entry);
    return entry;
  }

  public static <E extends AbstractMinecart, I extends MinecartWithBlockItem>
      MinecartEntry<E, I> registerItemOnly(
          String itemName,
          DeferredHolder<EntityType<?>, EntityType<E>> existingEntity,
          Function<Item.Properties, I> itemFactory) {
    return registerItemOnly(
        itemName, existingEntity, itemFactory, MinecartWithBlockItem.DISPENSER_BEHAVIOR, null);
  }

  public static <E extends AbstractMinecart, I extends MinecartWithBlockItem>
      MinecartEntry<E, I> registerItemOnly(
          String itemName,
          DeferredHolder<EntityType<?>, EntityType<E>> existingEntity,
          Function<Item.Properties, I> itemFactory,
          DispenseItemBehavior dispenseBehavior) {
    return registerItemOnly(itemName, existingEntity, itemFactory, dispenseBehavior, null);
  }

  public static <E extends AbstractMinecart, I extends MinecartWithBlockItem>
      MinecartEntry<E, I> registerItemOnly(
          String itemName,
          DeferredHolder<EntityType<?>, EntityType<E>> existingEntity,
          Function<Item.Properties, I> itemFactory,
          @Nullable DispenseItemBehavior dispenseBehavior,
          @Nullable
              BiFunction<
                      DeferredHolder<EntityType<?>, EntityType<E>>,
                      DeferredItem<I>,
                      BiFunction<Level, Vec3, AbstractMinecart>>
                  spawnFactoryBuilder) {
    var item = ITEMS.registerItem(itemName, itemFactory);

    BiFunction<Level, Vec3, AbstractMinecart> spawnFactory =
        (spawnFactoryBuilder != null) ? spawnFactoryBuilder.apply(existingEntity, item) : null;

    var entry = new MinecartEntry<>(itemName, existingEntity, item, dispenseBehavior, spawnFactory);
    MINECARTS.add(entry);
    return entry;
  }

  public static <E extends AbstractMinecart>
      DeferredHolder<EntityType<?>, EntityType<E>> registerEntityOnly(
          String entityName, EntityType.EntityFactory<E> entityFactory) {
    return ENTITIES.register(
        entityName,
        () ->
            EntityType.Builder.of(entityFactory, MobCategory.MISC)
                .sized(0.98F, 0.7F)
                .passengerAttachments(0.1875F)
                .clientTrackingRange(8)
                .build(
                    ResourceKey.create(
                        Registries.ENTITY_TYPE,
                        Identifier.fromNamespaceAndPath(MODID, entityName))));
  }

  public static final DeferredHolder<EntityType<?>, EntityType<DamageCausingMinecartEntity>>
      DAMAGE_CAUSING_MINECART =
          registerEntityOnly("harmful_minecart", DamageCausingMinecartEntity::new);

  public static final DeferredHolder<EntityType<?>, EntityType<RedstoneBlockMinecartEntity>>
      POWER_PROVIDER_MINECART =
          registerEntityOnly("power_minecart", RedstoneBlockMinecartEntity::new);

  public static final DeferredHolder<
          EntityType<?>, EntityType<HorizontalDirectionalRedstoneEmitterPowerMinecartEntity>>
      DIRECTIONAL_POWER_PROVIDER_MINECART =
          registerEntityOnly(
              "power_minecart_directional",
              HorizontalDirectionalRedstoneEmitterPowerMinecartEntity::new);

  public static final DeferredHolder<EntityType<?>, EntityType<PressurePlateMinecartEntity>>
      PRESHER_PLATE_MINECART =
          registerEntityOnly("pressure_plate_minecart", PressurePlateMinecartEntity::new);

  public static final DeferredHolder<EntityType<?>, EntityType<WeightPressurePlateMinecartEntity>>
      WEIGHT_PRESHER_PLATE_MINECART =
          registerEntityOnly(
              "weight_pressure_plate_minecart", WeightPressurePlateMinecartEntity::new);

  public static final DeferredHolder<EntityType<?>, EntityType<CompatFriendlyBlockMinecartEntity>>
      BLOCK_MINECART = registerEntityOnly("block_minecart", CompatFriendlyBlockMinecartEntity::new);

  public static final DeferredHolder<EntityType<?>, EntityType<RailMinecartEntity>> RAIL_MINECART =
      registerEntityOnly("rail_minecart", RailMinecartEntity::new);

  public static final DeferredHolder<
          EntityType<?>, EntityType<NonInventoryWorkingBlockMinecartEntity>>
      NON_INVENTORY_WORKING_MINECART =
          registerEntityOnly("working_minecart", NonInventoryWorkingBlockMinecartEntity::new);

  public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem>
      CACTUS_MINECART =
          registerItemOnly(
              "minecart_cactus",
              DAMAGE_CAUSING_MINECART,
              p ->
                  new DamageCausingMinecartItem(
                      p.stacksTo(1), 1.0f, Blocks.CACTUS, DamageTypes.CACTUS),
              DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DamageCausingMinecartEntity(
                          entity.get(),
                          w,
                          pos.x,
                          pos.y,
                          pos.z,
                          1.0f,
                          item.get(),
                          DamageTypes.CACTUS));

  public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem>
      MAGMA_BLOCK_MINECART =
          registerItemOnly(
              "minecart_magma",
              DAMAGE_CAUSING_MINECART,
              p ->
                  new DamageCausingMinecartItem(
                      p.stacksTo(1), 1.0f, Blocks.MAGMA_BLOCK, DamageTypes.HOT_FLOOR),
              DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DamageCausingMinecartEntity(
                          entity.get(),
                          w,
                          pos.x,
                          pos.y,
                          pos.z,
                          1.0f,
                          item.get(),
                          DamageTypes.HOT_FLOOR));

  public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem>
      CAMPFIRE_MINECART =
          registerItemOnly(
              "minecart_campfire",
              DAMAGE_CAUSING_MINECART,
              p ->
                  new DamageCausingMinecartItem(
                      p.stacksTo(1), 2.0f, Blocks.CAMPFIRE, DamageTypes.CAMPFIRE),
              DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DamageCausingMinecartEntity(
                          entity.get(),
                          w,
                          pos.x,
                          pos.y,
                          pos.z,
                          2.0f,
                          item.get(),
                          DamageTypes.CAMPFIRE));

  public static final MinecartEntry<DamageCausingMinecartEntity, DamageCausingMinecartItem>
      SOUL_CAMPFIRE_MINECART =
          registerItemOnly(
              "minecart_soul_campfire",
              DAMAGE_CAUSING_MINECART,
              p ->
                  new DamageCausingMinecartItem(
                      p.stacksTo(1), 2.0f, Blocks.SOUL_CAMPFIRE, DamageTypes.CAMPFIRE),
              DamageCausingMinecartItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DamageCausingMinecartEntity(
                          entity.get(),
                          w,
                          pos.x,
                          pos.y,
                          pos.z,
                          2.0f,
                          item.get(),
                          DamageTypes.CAMPFIRE));

  public static final MinecartEntry<RedstoneBlockMinecartEntity, MinecartWithBlockItem>
      REDSTONE_MINECART =
          registerItemOnly(
              "minecart_redstone",
              POWER_PROVIDER_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.REDSTONE_BLOCK),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new RedstoneBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<
          HorizontalDirectionalRedstoneEmitterPowerMinecartEntity, MinecartWithBlockItem>
      REPEATER_MINECART =
          registerItemOnly(
              "minecart_repeater",
              DIRECTIONAL_POWER_PROVIDER_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.REPEATER),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new HorizontalDirectionalRedstoneEmitterPowerMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<
          CompatFriendlyBlockMinecartEntity, MultiVariantMinecartWithBlockItem>
      BLOCK_MINECART_ITEM =
          registerItemOnly(
              "minecart_with_block",
              BLOCK_MINECART,
              p -> new MultiVariantMinecartWithBlockItem(p.stacksTo(1), Blocks.GRASS_BLOCK),
              MultiVariantMinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new VariantBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<PressurePlateMinecartEntity, MultiVariantMinecartWithBlockItem>
      PRESHER_PLATE_MINECART_ITEM =
          registerItemOnly(
              "minecart_presher_plate",
              PRESHER_PLATE_MINECART,
              p -> new MultiVariantMinecartWithBlockItem(p.stacksTo(1), Blocks.OAK_PRESSURE_PLATE),
              null,
              (entity, item) ->
                  (w, pos) ->
                      new PressurePlateMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<WeightPressurePlateMinecartEntity, MinecartWithBlockItem>
      IRON_PRESHER_PLATE_MINECART =
          registerItemOnly(
              "minecart_iron_presher_plate",
              WEIGHT_PRESHER_PLATE_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
              null,
              (entity, item) ->
                  (w, pos) ->
                      new WeightPressurePlateMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<WeightPressurePlateMinecartEntity, MinecartWithBlockItem>
      GOLDEN_PRESHER_PLATE_MINECART =
          registerItemOnly(
              "minecart_golden_presher_plate",
              WEIGHT_PRESHER_PLATE_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE),
              null,
              (entity, item) ->
                  (w, pos) ->
                      new WeightPressurePlateMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<SpongeMinecartEntity, MinecartWithBlockItem> SPONGE_MINECART =
      register(
          "sponge",
          SpongeMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SPONGE),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new SpongeMinecartEntity(
                      entity.get(),
                      w,
                      pos.x,
                      pos.y,
                      pos.z,
                      SpongeMinecartEntity.ABSORB_RADIUS,
                      SpongeMinecartEntity.ABSORB_LIMIT,
                      item.get()));

  public static final MinecartEntry<SpongeMinecartEntity, MinecartWithBlockItem>
      WET_SPONGE_MINECART =
          registerItemOnly(
              "wet_sponge",
              SPONGE_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WET_SPONGE),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new SpongeMinecartEntity(
                          entity.get(),
                          w,
                          pos.x,
                          pos.y,
                          pos.z,
                          SpongeMinecartEntity.ABSORB_RADIUS,
                          SpongeMinecartEntity.ABSORB_LIMIT,
                          item.get()));

  public static final MinecartEntry<BarrelMinecartEntity, MinecartWithBlockItem> BARREL_MINECART =
      register(
          "barrel",
          BarrelMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BARREL),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new BarrelMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z));

  public static final MinecartEntry<TrappedChestMinecartEntity, MinecartWithBlockItem>
      TRAPPED_CHEST_MINECART =
          register(
              "trapped_chest",
              TrappedChestMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.TRAPPED_CHEST),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) -> (w, pos) -> new TrappedChestMinecartEntity(w, pos.x, pos.y, pos.z));

  public static final MinecartEntry<CopperChestMinecartEntity, MinecartWithBlockItem>
      COPPER_CHEST_MINECART =
          register(
              "copper_chest",
              CopperChestMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.COPPER_CHEST),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) -> (w, pos) -> new CopperChestMinecartEntity(w, pos.x, pos.y, pos.z));

  public static final MinecartEntry<MinecartFurnace, MinecartWithBlockItem> BLAST_FURNACE_MINECART =
      registerItemOnly(
          "blast_furnace",
          DeferredHolder.create(
              Registries.ENTITY_TYPE,
              Identifier.fromNamespaceAndPath("minecraft", "furnace_minecart")),
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BLAST_FURNACE),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> {
                MinecartFurnace m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
                m.setInitialPos(pos.x, pos.y, pos.z);
                m.setCustomDisplayBlockState(Optional.of(Blocks.BLAST_FURNACE.defaultBlockState()));
                return m;
              });

  public static final MinecartEntry<MinecartFurnace, MinecartWithBlockItem> SMOKER_MINECART =
      registerItemOnly(
          "smoker",
          DeferredHolder.create(
              Registries.ENTITY_TYPE,
              Identifier.fromNamespaceAndPath("minecraft", "furnace_minecart")),
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SMOKER),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> {
                MinecartFurnace m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
                m.setInitialPos(pos.x, pos.y, pos.z);
                m.setCustomDisplayBlockState(Optional.of(Blocks.SMOKER.defaultBlockState()));
                return m;
              });

  public static final MinecartEntry<JukeboxMinecartEntity, MinecartWithBlockItem> JUKEBOX_MINECART =
      register(
          "jukebox",
          JukeboxMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.JUKEBOX),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new JukeboxMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<ShulkerMinecartEntity, ShulkerMinecartItem> SHULKER_MINECART =
      register(
          "shulker",
          ShulkerMinecartEntity::new,
          p -> new ShulkerMinecartItem(p.stacksTo(1), Blocks.SHULKER_BOX),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new ShulkerMinecartEntity(
                      entity.get(),
                      w,
                      pos.x,
                      pos.y,
                      pos.z,
                      (net.minecraft.world.level.block.ShulkerBoxBlock) Blocks.SHULKER_BOX));

  public static final MinecartEntry<DragonEggMinecart, MinecartWithBlockItem> DRAGON_EGG_MINECART =
      register(
          "dragon_egg",
          DragonEggMinecart::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DRAGON_EGG),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new DragonEggMinecart(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<CobwebMinecartEntity, MinecartWithBlockItem> COBWEB_MINECART =
      register(
          "cobweb",
          CobwebMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.COBWEB),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new CobwebMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem>
      SMITHING_TABLE_MINECART =
          register(
              "smithing_table",
              NonInventoryWorkingBlockMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SMITHING_TABLE),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NonInventoryWorkingBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem>
      CRAFTING_TABLE_MINECART =
          register(
              "crafting_table",
              NonInventoryWorkingBlockMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CRAFTING_TABLE),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NonInventoryWorkingBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem>
      STONECUTTER_MINECART =
          register(
              "stonecutter",
              NonInventoryWorkingBlockMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.STONECUTTER),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NonInventoryWorkingBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem>
      LOOM_MINECART =
          register(
              "loom",
              NonInventoryWorkingBlockMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LOOM),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NonInventoryWorkingBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem>
      CARTOGRAPHY_TABLE_MINECART =
          register(
              "cartography_table",
              NonInventoryWorkingBlockMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CARTOGRAPHY_TABLE),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NonInventoryWorkingBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem>
      GRINDSTONE_MINECART =
          register(
              "grindstone",
              NonInventoryWorkingBlockMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.GRINDSTONE),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NonInventoryWorkingBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<AnvilMinecart, MinecartWithBlockItem> ANVIL_MINECART =
      register(
          "anvil",
          AnvilMinecart::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ANVIL),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new AnvilMinecart(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<NonInventoryWorkingBlockMinecartEntity, MinecartWithBlockItem>
      ENCHANTING_TABLE_MINECART =
          register(
              "enchanting_table",
              NonInventoryWorkingBlockMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ENCHANTING_TABLE),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NonInventoryWorkingBlockMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<EnderChestMinecartEntity, MinecartWithBlockItem>
      ENDER_CHEST_MINECART =
          register(
              "ender_chest",
              EnderChestMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ENDER_CHEST),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) -> new EnderChestMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z));

  public static final MinecartEntry<BeaconMinecartEntity, MinecartWithBlockItem> BEACON_MINECART =
      register(
          "beacon",
          BeaconMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BEACON),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new BeaconMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DispenserMinecartEntity, MinecartWithBlockItem>
      DISPENSER_MINECART =
          register(
              "dispenser",
              DispenserMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DISPENSER),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) -> new DispenserMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z));

  public static final MinecartEntry<FluidMinecartEntity, MinecartWithBlockItem> WATER_MINECART =
      register(
          "water",
          (type, world) -> new FluidMinecartEntity(type, world, Items.WATER_BUCKET),
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WATER),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new FluidMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<FluidMinecartEntity, MinecartWithBlockItem> LAVA_MINECART =
      register(
          "lava",
          (type, world) -> new FluidMinecartEntity(type, world, Items.LAVA_BUCKET),
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LAVA),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new FluidMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<MagnetMinecartEntity, MinecartWithBlockItem> MAGNET_MINECART =
      register(
          "magnet",
          MagnetMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.LODESTONE),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new MagnetMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<MobHeadMinecartEntity, MinecartWithBlockItem>
      MOB_HEAD_MINECART =
          register(
              "mob_head",
              MobHeadMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DRAGON_HEAD),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new MobHeadMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<SofaMinecartEntity, MinecartWithBlockItem> SOFA_MINECART =
      register(
          "sofa",
          SofaMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WHITE_CARPET),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new SofaMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<WoolMinecartEntity, MinecartWithBlockItem> WOOL_MINECART =
      register(
          "wool",
          WoolMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WHITE_WOOL),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new WoolMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<NetherPortalMinecartEntity, MinecartWithBlockItem>
      PORTAL_MINECART =
          register(
              "nether_portal",
              NetherPortalMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.NETHER_PORTAL),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new NetherPortalMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<EnderPortalMinecartEntity, MinecartWithBlockItem>
      ENDER_PORTAL_MINECART =
          register(
              "end_portal",
              EnderPortalMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.END_PORTAL),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new EnderPortalMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<ObsidianMinecartEntity, MinecartWithBlockItem>
      OBSIDIAN_MINECART =
          register(
              "obsidian",
              ObsidianMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.OBSIDIAN),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new ObsidianMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<AmethystMinecartEntity, MinecartWithBlockItem>
      AMETHYST_MINECART =
          register(
              "amethyst",
              AmethystMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.AMETHYST_BLOCK),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new AmethystMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<AntMinecartEntity, MinecartWithBlockItem> OBSERVER_MINECART =
      register(
          "ant",
          AntMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.OBSERVER),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new AntMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<HoneyMinecartEntity, MinecartWithBlockItem> HONEY_MINECART =
      register(
          "honey",
          HoneyMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.HONEY_BLOCK),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new HoneyMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<PistonMinecartEntity, MinecartWithBlockItem> PISTON_MINECART =
      register(
          "piston",
          PistonMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.PISTON),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) ->
                  new PistonMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<RailMinecartEntity, MinecartWithBlockItem>
      NORMAL_RAIL_MINECART =
          registerItemOnly(
              "normal_rail",
              RAIL_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.RAIL),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new RailMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<RailMinecartEntity, MinecartWithBlockItem>
      DETECTOR_RAIL_MINECART =
          registerItemOnly(
              "decector_rail",
              RAIL_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DETECTOR_RAIL),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new RailMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<RailMinecartEntity, MinecartWithBlockItem>
      ACTIVATOR_RAIL_MINECART =
          registerItemOnly(
              "acticator_rail",
              RAIL_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ACTIVATOR_RAIL),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new RailMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<RailMinecartEntity, MinecartWithBlockItem>
      POWERED_RAIL_MINECART =
          registerItemOnly(
              "powered_rail",
              RAIL_MINECART,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.POWERED_RAIL),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new RailMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<ScaffoldMinecartEntity, MinecartWithBlockItem>
      SCAFFOLD_MINECART =
          register(
              "scaffold",
              ScaffoldMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SCAFFOLDING),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new ScaffoldMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
  public static final MinecartEntry<EndPortalFraneMinecartEntity, MinecartWithBlockItem>
      END_PORTAL_FRAME_MINECART =
          register(
              "end_frame",
              EndPortalFraneMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.END_PORTAL_FRAME),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new EndPortalFraneMinecartEntity(
                          entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<PickerMinecartEntity, PickerMinecartItem> PICKER_MINECART =
      register(
          "picker",
          (type, world) ->
              new PickerMinecartEntity(
                  (EntityType<? extends net.minecraft.world.entity.vehicle.minecart.Minecart>) type,
                  world),
          PickerMinecartItem::new,
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) -> (w, pos) -> new PickerMinecartEntity(w, pos.x, pos.y, pos.z));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem> IRON_DOOR_MINECART =
      register(
          "door_iron",
          DoorMinecartEntity::new,
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.IRON_DOOR),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem> OAK_DOOR_MINECART =
      registerItemOnly(
          "oak_door",
          IRON_DOOR_MINECART.entity(),
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.OAK_DOOR),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

    public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem> PALE_DOOR_MINECART =
            registerItemOnly(
                    "pale_oak_door",
                    IRON_DOOR_MINECART.entity(),
                    p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.PALE_OAK_DOOR),
                    MinecartWithBlockItem.DISPENSER_BEHAVIOR,
                    (entity, item) ->
                            (w, pos) -> new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem> SPRUCE_DOOR_MINECART =
          registerItemOnly(
              "spruce_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SPRUCE_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) -> new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem> BIRCH_DOOR_MINECART =
      registerItemOnly(
          "birch_door",
          IRON_DOOR_MINECART.entity(),
          p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BIRCH_DOOR),
          MinecartWithBlockItem.DISPENSER_BEHAVIOR,
          (entity, item) ->
              (w, pos) -> new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      JUNGLE_DOOR_MINECART =
          registerItemOnly(
              "jungle_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.JUNGLE_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      ACACIA_DOOR_MINECART =
          registerItemOnly(
              "acacia_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ACACIA_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      DARK_OAK_DOOR_MINECART =
          registerItemOnly(
              "dark_oak_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DARK_OAK_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      MANGROVE_DOOR_MINECART =
          registerItemOnly(
              "mangrove_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.MANGROVE_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      CHERRY_DOOR_MINECART =
          registerItemOnly(
              "cherry_door",
               IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CHERRY_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      BAMBOO_DOOR_MINECART =
          registerItemOnly(
              "bamboo_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BAMBOO_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      CRIMSON_DOOR_MINECART =
          registerItemOnly(
              "crimson_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CRIMSON_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      WARPED_DOOR_MINECART =
          registerItemOnly(
              "warped_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WARPED_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      COPPER_DOOR_MINECART =
          registerItemOnly(
              "copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      EXPOSED_COPPER_DOOR_MINECART =
          registerItemOnly(
              "exposed_copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.EXPOSED_COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      WEATHERED_COPPER_DOOR_MINECART =
          registerItemOnly(
              "weathered_copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WEATHERED_COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      OXIDIZED_COPPER_DOOR_MINECART =
          registerItemOnly(
              "oxidized_copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.OXIDIZED_COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      WAXED_COPPER_DOOR_MINECART =
          registerItemOnly(
              "waxed_copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      WAXED_EXPOSED_COPPER_DOOR_MINECART =
          registerItemOnly(
              "waxed_exposed_copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_EXPOSED_COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      WAXED_WEATHERED_COPPER_DOOR_MINECART =
          registerItemOnly(
              "waxed_weathered_copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_WEATHERED_COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<DoorMinecartEntity, MinecartWithBlockItem>
      WAXED_OXIDIZED_COPPER_DOOR_MINECART =
          registerItemOnly(
              "waxed_oxidized_copper_door",
              IRON_DOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_OXIDIZED_COPPER_DOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new DoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      IRON_TRAPDOOR_MINECART =
          register(
              "trapdoor_iron",
              TrapdoorMinecartEntity::new,
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.IRON_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      OAK_TRAPDOOR_MINECART =
          registerItemOnly(
              "oak_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.OAK_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      SPRUCE_TRAPDOOR_MINECART =
          registerItemOnly(
              "spruce_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.SPRUCE_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      BIRCH_TRAPDOOR_MINECART =
          registerItemOnly(
              "birch_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BIRCH_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      JUNGLE_TRAPDOOR_MINECART =
          registerItemOnly(
              "jungle_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.JUNGLE_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      ACACIA_TRAPDOOR_MINECART =
          registerItemOnly(
              "acacia_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.ACACIA_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      DARK_OAK_TRAPDOOR_MINECART =
          registerItemOnly(
              "dark_oak_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.DARK_OAK_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      MANGROVE_TRAPDOOR_MINECART =
          registerItemOnly(
              "mangrove_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.MANGROVE_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      CHERRY_TRAPDOOR_MINECART =
          registerItemOnly(
              "cherry_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CHERRY_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      BAMBOO_TRAPDOOR_MINECART =
          registerItemOnly(
              "bamboo_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.BAMBOO_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      CRIMSON_TRAPDOOR_MINECART =
          registerItemOnly(
              "crimson_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.CRIMSON_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      WARPED_TRAPDOOR_MINECART =
          registerItemOnly(
              "warped_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WARPED_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      EXPOSED_COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "exposed_copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.EXPOSED_COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      WEATHERED_COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "weathered_copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WEATHERED_COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      OXIDIZED_COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "oxidized_copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.OXIDIZED_COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      WAXED_COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "waxed_copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      WAXED_EXPOSED_COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "waxed_exposed_copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      WAXED_WEATHERED_COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "waxed_weathered_copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  public static final MinecartEntry<TrapdoorMinecartEntity, MinecartWithBlockItem>
      WAXED_OXIDIZED_COPPER_TRAPDOOR_MINECART =
          registerItemOnly(
              "waxed_oxidized_copper_trapdoor",
              IRON_TRAPDOOR_MINECART.entity(),
              p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR),
              MinecartWithBlockItem.DISPENSER_BEHAVIOR,
              (entity, item) ->
                  (w, pos) ->
                      new TrapdoorMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

  @SuppressWarnings("unchecked")
  public static final DeferredHolder<EntityType<?>, EntityType<ChainEntity>> CHAIN_ENTITY =
      (DeferredHolder<EntityType<?>, EntityType<ChainEntity>>)
          (Object)
              ENTITIES.register(
                  "chain",
                  () ->
                      EntityType.Builder.of(ChainEntity::new, MobCategory.MISC)
                          .sized(0.5F, 0.5F)
                          .clientTrackingRange(64)
                          .updateInterval(1)
                          .build(
                              ResourceKey.create(
                                  Registries.ENTITY_TYPE,
                                  Identifier.fromNamespaceAndPath(MODID, "chain"))));
}
