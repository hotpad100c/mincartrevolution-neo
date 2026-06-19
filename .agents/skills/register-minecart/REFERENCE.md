# Register New Minecart — Reference

## Project conventions

- **Mod ID**: `minecartrevolution`
- **Base package**: `ml.mypals.minecartrevolution`
- **Java version**: 25
- **NeoForge**: 26.1.2
- **Naming**: Entity `{base}_minecart`, Item `minecart_{base}`, constant `{BASE}_MINECART`

## File structure

| File/Directory | Purpose |
|---|---|
| `registeries/MRMinecarts.java` | Central registry for all minecart entities + items |
| `registeries/MRModEntityRenderers.java` | Entity renderer registration |
| `registeries/MRModItems.java` | Creative tab, dispenser behaviors |
| `entity/minecarts/` | Custom minecart entity classes |
| `entity/minecarts/container/` | Container minecarts (barrel, chest, dispenser...) |
| `entity/minecarts/functioning/` | Functional minecarts (anvil, beacon, jukebox...) |
| `entity/minecarts/redstone/` | Redstone minecarts (redstone block, pressure plate, piston...) |
| `entity/minecarts/fluidcarts/` | Fluid/portal minecarts (water, lava, nether portal...) |
| `entity/minecarts/maps/` | `@MinecartMapper` block→entity dispatch |
| `item/` | Custom item classes (`MinecartWithBlockItem` subclasses) |
| `behaviours/MinecartTransformManager.java` | Block/item→minecart entity factory map |
| `behaviours/MinecartTransformConfig.java` | Minecart factory functional interface |
| `client/renderer/` | Custom minecart renderers |
| `client/light/` | Moving light system (`DynamicLightsStorage`) |
| `assets/minecartrevolution/lang/` | Language files (`en_us.json`, `zh_cn.json`) |
| `datagen/MRRecipeProvider.java` | Recipe data generation |

## No registration needed (any block)

The mod's `MinecartTransformManager` already converts any block/item into `CompatFriendlyBlockMinecartEntity` when placed on rails. If you only want a block to appear in a minecart with no special behavior, there is **nothing to register** — it works out of the box.

## Scenario A: Custom entity minecart

### Files to create/edit

#### 1. Create entity class — `entity/minecarts/FooMinecartEntity.java`

Base template extending `SingleBlockMinecartEntity`:
```java
package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class FooMinecartEntity extends SingleBlockMinecartEntity {

    public FooMinecartEntity(EntityType<FooMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.FOO.defaultBlockState();
    }

    public FooMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world,
                             double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        setCustomDisplayBlockState(Optional.of(Blocks.FOO.defaultBlockState()));
    }

    // Override tick() here for custom behavior

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.FOO_MINECART.item().get().getDefaultInstance();
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.FOO_MINECART.item().get();
    }
}
```

**Custom display block state**: `getDefaultDisplayBlockState()` can return a block state with properties — not just `.defaultBlockState()`. Examples from the codebase:
```java
// Directional block with facing
return Blocks.OBSERVER.defaultBlockState().setValue(ObserverBlock.FACING, Direction.NORTH);
// Block with size/distance properties
return Blocks.SCAFFOLDING.defaultBlockState().setValue(ScaffoldingBlock.BOTTOM, true).setValue(ScaffoldingBlock.DISTANCE, 1);
```
Also call `setCustomDisplayBlockState(...)` in the constructor to match.

**Moving light source**: Automatic for any entity extending `VariantBlockMinecartEntity`. The `getLightLevel()` method reads from the display block's light emission, and `tickDynamicLight()` in `VariantBlockMinecartEntity.tick()` handles registration, movement tracking, and cleanup via `DynamicLightsStorage`. No extra code needed — just ensure the display block has a non-zero `getLightEmission()` (e.g. glowstone, beacon, furnace).

**Common overrides** (copy from existing entities):

| Override | Purpose | Example file |
|---|---|---|
| `tick()` | Per-tick logic | `ObsidianMinecartEntity.java:34` |
| `applyNaturalSlowdown()` | Modify movement drag | `HoneyMinecartEntity.java:44` |
| `move()` | Custom movement | `HoneyMinecartEntity.java:63` |
| `onCollision()` | Collision response | `HoneyMinecartEntity.java:73` |
| `addAdditionalSaveData()` / `readAdditionalSaveData()` | Persist custom fields | `HoneyMinecartEntity.java:94-106` |
| `fireImmune()` | Lava/ fire immunity | `ObsidianMinecartEntity.java:50` |

#### 2. Register — `registeries/MRMinecarts.java`

Add import and entry:
```java
import ml.mypals.minecartrevolution.entity.minecarts.FooMinecartEntity;

// ...

public static final MinecartEntry<FooMinecartEntity, MinecartWithBlockItem> FOO_MINECART = register(
    "foo", FooMinecartEntity::new,
    p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.FOO),
    MinecartWithBlockItem.DISPENSER_BEHAVIOR,
    (entity, item) -> (w, pos) -> new FooMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
```

#### 3. Lang — `assets/minecartrevolution/lang/en_us.json` and `zh_cn.json`

```json
"entity.minecartrevolution.foo_minecart": "Foo Minecart",
```
```json
"entity.minecartrevolution.foo_minecart": "Foo矿车",
```

Item names are auto-generated from the block name. No item lang key needed for `MinecartWithBlockItem`.

#### 4. (Optional) Custom renderer — `registeries/MRModEntityRenderers.java`

If the minecart needs a non-default renderer, add:
```java
registerRenderers.registerEntityRenderer(
    MRMinecarts.FOO_MINECART.entity().get(),
    FooMinecartRenderer::new
);
```

Standard block-display minecarts are auto-covered by the generic `MinecartRenderer` loop at the top of `MRModEntityRenderers.init()`.

#### 5. (Optional) Custom item class — `item/FooMinecartItem.java`

If the minecart item needs special logic (custom dispenser behavior, NBT handling, tooltip, etc.), create a class extending `MinecartWithBlockItem` and use it as the item factory in `register()`. See `DamageCausingMinecartItem` or `PickerMinecartItem` for examples.

```java
public class FooMinecartItem extends MinecartWithBlockItem {
    public FooMinecartItem(Properties settings) {
        super(settings, Blocks.FOO);
    }
    // Override getCart(), useOn(), getName(), etc.
}
```

Then register with the custom factory:
```java
public static final MinecartEntry<FooMinecartEntity, FooMinecartItem> FOO_MINECART = register(
    "foo", FooMinecartEntity::new,
    FooMinecartItem::new,
    FooMinecartItem.DISPENSER_BEHAVIOR,
    (entity, item) -> (w, pos) -> new FooMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));
```

Custom item classes need an explicit lang key:
```json
"item.minecartrevolution.minecart_foo": "Foo Minecart",
```

#### 6. (Optional) Interactable minecart (open GUI / use block)

Two approaches:

**A) Generic block interaction** — extend `CompatFriendlyBlockMinecartEntity` instead of `SingleBlockMinecartEntity`. Its `interact()` delegates to the block's `useWithoutItem`/`useItemOn` via a simulated level. Use the `safe_to_interact` block tag (`data/minecartrevolution/tags/block/safe_to_interact.json`) to whitelist which blocks expose interaction. Works for any block with a right-click action.

**B) Vanilla menu opening** — extend `NonInventoryWorkingBlockMinecartEntity` and override `getMenuProvider(BlockState)` to return the appropriate `MenuProvider` for your block. See `NonInventoryWorkingBlockMinecartEntity.java:151` for the full switch-case pattern (crafting table, stonecutter, anvil, enchantment table, etc.).

#### 7. (Optional) Redstone signal emitter

Implement `PowerEmitterMinecartEntity` interface. Required methods and logic:

```java
public class FooMinecartEntity extends SingleBlockMinecartEntity implements PowerEmitterMinecartEntity {

    @Override
    public int getPowerStrength(Direction direction, BlockPos pos) {
        if (!this.isAlive()) return 0;
        BlockState state = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        return Math.max(state.getSignal(level(), pos, direction), state.getDirectSignal(level(), pos, direction));
    }

    @Override
    public void tick() {
        super.tick();
        BlockState state = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        // Update redstone neighbors on position change
        if (this.getPreviousBlockPos() == null || !this.getPreviousBlockPos().equals(this.blockPosition())) {
            if (this.getPreviousBlockPos() == null) this.setPreviousBlockPos(this.blockPosition());
            updateNeighbors(level(), previousBlockPos, state.getBlock());
            this.setPreviousBlockPos(this.blockPosition());
            updateNeighbors(level(), this.blockPosition(), state.getBlock());
        }
    }

    @Override
    public void destroy(ServerLevel level, DamageSource source) {
        super.destroy(level, source);
        BlockState state = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        updateNeighbors(level, getPreviousBlockPos(), state.getBlock());
        updateNeighbors(level, this.blockPosition(), state.getBlock());
    }
}
```

The `RedstoneViewMixin` automatically queries all `PowerEmitterMinecartEntity` instances on the rail for signal strength. Existing examples: `RedstoneBlockMinecartEntity`, `PressurePlateMinecartEntity`, `TrappedChestMinecartEntity`.

#### 8. (Optional) Block→entity auto-mapping — `entity/minecarts/maps/`

If the block should spawn this minecart when right-clicked on rails (beyond what `MinecartTransformManager` auto-registers from `MRMinecarts.MINECARTS`), create a mapper class:
```java
@MinecartMapper
public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> FOO_MAP = new HashMap<>();
static {
    FOO_MAP.put(Blocks.FOO, (world, pos) -> new FooMinecartEntity(
        MRMinecarts.FOO_MINECART.entity().get(), world, pos.x, pos.y, pos.z,
        MRMinecarts.FOO_MINECART.item().get()));
}
```

Note: Most minecarts do NOT need a `@MinecartMapper` — the auto-population from `MRMinecarts.MINECARTS` in `MinecartTransformManager` static block handles standard cases.

---

## Scenario B: Variant minecart (one entity, many block variants)

Two parallel strategies — choose one or both:

### B1: `registerItemOnly()` → dedicated creative-tab items

Use when each variant should have its own recipe and creative-tab entry. Example: `CACTUS_MINECART`, `MAGMA_BLOCK_MINECART`, `CAMPFIRE_MINECART`, `SOUL_CAMPFIRE_MINECART` all reuse `DAMAGE_CAUSING_MINECART`.

#### 1. Register shared entity — `MRMinecarts.java`
```java
public static final DeferredHolder<EntityType<?>, EntityType<FooMinecartEntity>> FOO_MINECART_ENTITY =
    registerEntityOnly("foo_minecart", FooMinecartEntity::new);
```

#### 2. Register each variant item
```java
public static final MinecartEntry<FooMinecartEntity, MinecartWithBlockItem> FOO_VARIANT_A_MINECART = registerItemOnly(
    "minecart_foo_a", FOO_MINECART_ENTITY,
    p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.FOO_A),
    MinecartWithBlockItem.DISPENSER_BEHAVIOR,
    (entity, item) -> (w, pos) -> new FooMinecartEntity(entity.get(), w, pos.x, pos.y, pos.z, item.get()));

public static final MinecartEntry<FooMinecartEntity, MinecartWithBlockItem> FOO_VARIANT_B_MINECART = registerItemOnly(
    "minecart_foo_b", FOO_MINECART_ENTITY,
    p -> new MinecartWithBlockItem(p.stacksTo(1), Blocks.FOO_B),
    // ... same pattern
```

#### 3. Lang entries for each variant item

### B2: `@MinecartMapper` → automatic block→entity on rail placement

Use when there are many variants (16 colors, etc.) and you don't need per-variant creative-tab items. The mapper handles right-click-on-rail conversion automatically. Example: `WoolEntityMapper` maps all 16 wool colors to `WoolMinecartEntity`.

#### 1. Create mapper — `entity/minecarts/maps/FooEntityMapper.java`
```java
package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class FooEntityMapper {
    @MinecartMapper
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> FOO_MAP = new HashMap<>();

    static {
        FOO_MAP.put(Blocks.FOO_A, (world, pos) -> new FooMinecartEntity(
            MRMinecarts.FOO_MINECART_ENTITY.get(), world, pos.x, pos.y, pos.z, Blocks.FOO_A.asItem()));
        FOO_MAP.put(Blocks.FOO_B, (world, pos) -> new FooMinecartEntity(
            MRMinecarts.FOO_MINECART_ENTITY.get(), world, pos.x, pos.y, pos.z, Blocks.FOO_B.asItem()));
    }
}
```

The `AnnotationManager` in `MinecartTransformManager` automatically discovers all `@MinecartMapper`-annotated fields at mod load time and registers them in the factory map.

### B1 + B2 combined (common pattern)

Most variant minecarts in the mod use both patterns together:
- B1 provides creative-tab items with recipes
- B2 handles the right-click-on-rail conversion for all block variants

Existing mappers: `RailMinecartEntityMapper` (4 rail types), `PressurePlateEntityMapper` (all wood/stone), `ShulkerBoxEntityMapper` (16 colors), `SofaEntityMapper`/`WoolEntityMapper` (16 wool colors), `MobHeadEntityMapper` (all heads), `NonInventoryWorkingBlockEntityMapper` (9 work blocks), `ChestEntityMapper` (copper chests).

---

## Entity class hierarchy

Choose your base class by the features needed:

```
AbstractMinecart (vanilla)
├── VariantBlockMinecartEntity                     [base: display block, moving light]
│   ├── SingleBlockMinecartEntity                  [+item tracking, drop logic]
│   │   ├── DamageCausingMinecartEntity            [damage on contact]
│   │   ├── RedstoneBlockMinecartEntity            [+PowerEmitterMinecartEntity redstone signal]
│   │   │   ├── HorizontalDirectionalRedstoneEmitterPowerMinecartEntity [directional redstone]
│   │   │   └── EndPortalFraneMinecartEntity
│   │   ├── SpongeMinecartEntity                   [water absorption/drying]
│   │   ├── JukeboxMinecartEntity                  [+PowerEmitterMinecartEntity music playback]
│   │   ├── ObsidianMinecartEntity                 [lava floating]
│   │   ├── AmethystMinecartEntity                 [sound relay]
│   │   ├── AntMinecartEntity                      [Langton's ant / observer]
│   │   ├── HoneyMinecartEntity                    [stick to blocks]
│   │   ├── PistonMinecartEntity                   [push entities]
│   │   └── ScaffoldMinecartEntity                 [scaffolding]
│   ├── CompatFriendlyBlockMinecartEntity          [generic block interaction, BE simulation, Capability]
│   ├── NonInventoryWorkingBlockMinecartEntity     [open vanilla menus (crafting/anvil/enchanting...)]
│   │   └── AnvilMinecart
│   ├── RailMinecartEntity                         [rail placement]
│   ├── PressurePlateMinecartEntity                [+PowerEmitterMinecartEntity pressure plate]
│   ├── BeaconMinecartEntity                       [beacon effects]
│   ├── MobHeadMinecartEntity                      [mob heads]
│   ├── WoolMinecartEntity                         [no gravity]
│   ├── FluidMinecartEntity                        [fluids (water/lava)]
│   └── PortalMinecartEntity                       [portals]
│       ├── NetherPortalMinecartEntity
│       └── EnderPortalMinecartEntity
├── AbstractMinecartContainer (vanilla)            [container minecart base]
│   └── BaseMinecartContainer
│       ├── BarrelMinecartEntity
│       ├── TrappedChestMinecartEntity             [+PowerEmitterMinecartEntity]
│       ├── CopperChestMinecartEntity
│       ├── ShulkerMinecartEntity
│       ├── EnderChestMinecartEntity
│       └── DispenserMinecartEntity
└── Minecart (vanilla)
    └── PickerMinecartEntity                       [sticky minecart]
```

## Existing minecart catalog

### Custom entity minecarts (Scenario A)

| Constant | Entity Class | Item Class | Redstone | Interactive | Custom Renderer | @Mapper |
|---|---|---|---|---|---|---|
| `SPONGE_MINECART` | `SpongeMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `BARREL_MINECART` | `BarrelMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `TRAPPED_CHEST_MINECART` | `TrappedChestMinecartEntity` | `MinecartWithBlockItem` | ✓ | | | |
| `COPPER_CHEST_MINECART` | `CopperChestMinecartEntity` | `MinecartWithBlockItem` | | | | ✓ |
| `JUKEBOX_MINECART` | `JukeboxMinecartEntity` | `MinecartWithBlockItem` | ✓ | | | |
| `SHULKER_MINECART` | `ShulkerMinecartEntity` | `ShulkerMinecartItem` | | | ✓ | ✓ |
| `DRAGON_EGG_MINECART` | `DragonEggMinecart` | `MinecartWithBlockItem` | | | | |
| `COBWEB_MINECART` | `CobwebMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `SMITHING_TABLE_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `CRAFTING_TABLE_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `STONECUTTER_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `LOOM_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `CARTOGRAPHY_TABLE_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `GRINDSTONE_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `ANVIL_MINECART` | `AnvilMinecart` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `ENCHANTING_TABLE_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | `MinecartWithBlockItem` | | ✓ | | ✓ |
| `ENDER_CHEST_MINECART` | `EnderChestMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `BEACON_MINECART` | `BeaconMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | ✓ |
| `DISPENSER_MINECART` | `DispenserMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `WATER_MINECART` | `FluidMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | |
| `LAVA_MINECART` | `FluidMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | |
| `MAGNET_MINECART` | `MagnetMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `MOB_HEAD_MINECART` | `MobHeadMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | ✓ |
| `SOFA_MINECART` | `SofaMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | ✓ |
| `WOOL_MINECART` | `WoolMinecartEntity` | `MinecartWithBlockItem` | | | | ✓ |
| `PORTAL_MINECART` | `NetherPortalMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | |
| `ENDER_PORTAL_MINECART` | `EnderPortalMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | |
| `OBSIDIAN_MINECART` | `ObsidianMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `AMETHYST_MINECART` | `AmethystMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `OBSERVER_MINECART` | `AntMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `HONEY_MINECART` | `HoneyMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `PISTON_MINECART` | `PistonMinecartEntity` | `MinecartWithBlockItem` | | | | |
| `SCAFFOLD_MINECART` | `ScaffoldMinecartEntity` | `MinecartWithBlockItem` | | | ✓ | |
| `END_PORTAL_FRAME_MINECART` | `EndPortalFraneMinecartEntity` | `MinecartWithBlockItem` | ✓ | | | |
| `PICKER_MINECART` | `PickerMinecartEntity` | `PickerMinecartItem` | | | ✓ | |

### Variant minecarts (Scenario B) — `registerItemOnly()` reusing shared entity

| Constant | Reuses Entity | Entity Class | Item Class | Redstone | Custom Item | @Mapper |
|---|---|---|---|---|---|---|
| `CACTUS_MINECART` | `DAMAGE_CAUSING_MINECART` | `DamageCausingMinecartEntity` | `DamageCausingMinecartItem` | | ✓ | |
| `MAGMA_BLOCK_MINECART` | `DAMAGE_CAUSING_MINECART` | `DamageCausingMinecartEntity` | `DamageCausingMinecartItem` | | ✓ | |
| `CAMPFIRE_MINECART` | `DAMAGE_CAUSING_MINECART` | `DamageCausingMinecartEntity` | `DamageCausingMinecartItem` | | ✓ | |
| `SOUL_CAMPFIRE_MINECART` | `DAMAGE_CAUSING_MINECART` | `DamageCausingMinecartEntity` | `DamageCausingMinecartItem` | | ✓ | |
| `REDSTONE_MINECART` | `POWER_PROVIDER_MINECART` | `RedstoneBlockMinecartEntity` | `MinecartWithBlockItem` | ✓ | | |
| `REPEATER_MINECART` | `DIRECTIONAL_POWER_PROVIDER_MINECART` | `HorizontalDirectional...` | `MinecartWithBlockItem` | ✓ | | |
| `BLOCK_MINECART_ITEM` | `BLOCK_MINECART` | `CompatFriendlyBlockMinecartEntity` | `MultiVariantMinecartWithBlockItem` | | ✓ | |
| `PRESHER_PLATE_MINECART_ITEM` | `PRESHER_PLATE_MINECART` | `PressurePlateMinecartEntity` | `MultiVariantMinecartWithBlockItem` | ✓ | ✓ | ✓ |
| `IRON_PRESHER_PLATE_MINECART` | `WEIGHT_PRESHER_PLATE_MINECART` | `WeightPressurePlateMinecartEntity` | `MinecartWithBlockItem` | ✓ | | |
| `GOLDEN_PRESHER_PLATE_MINECART` | `WEIGHT_PRESHER_PLATE_MINECART` | `WeightPressurePlateMinecartEntity` | `MinecartWithBlockItem` | ✓ | | |
| `WET_SPONGE_MINECART` | `SPONGE_MINECART` (entity) | `SpongeMinecartEntity` | `MinecartWithBlockItem` | | | |
| `BLAST_FURNACE_MINECART` | vanilla `furnace_minecart` | `MinecartFurnace` (vanilla) | `MinecartWithBlockItem` | | | |
| `SMOKER_MINECART` | vanilla `furnace_minecart` | `MinecartFurnace` (vanilla) | `MinecartWithBlockItem` | | | |
| `NORMAL_RAIL_MINECART` | `RAIL_MINECART` | `RailMinecartEntity` | `MinecartWithBlockItem` | | | ✓ |
| `DETECTOR_RAIL_MINECART` | `RAIL_MINECART` | `RailMinecartEntity` | `MinecartWithBlockItem` | | | ✓ |
| `ACTIVATOR_RAIL_MINECART` | `RAIL_MINECART` | `RailMinecartEntity` | `MinecartWithBlockItem` | | | ✓ |
| `POWERED_RAIL_MINECART` | `RAIL_MINECART` | `RailMinecartEntity` | `MinecartWithBlockItem` | | | ✓ |

### Entity-only registrations (no item)

| Constant | Entity Class | Redstone | Notes |
|---|---|---|---|
| `DAMAGE_CAUSING_MINECART` | `DamageCausingMinecartEntity` | | Shared entity for damage-dealing minecarts |
| `POWER_PROVIDER_MINECART` | `RedstoneBlockMinecartEntity` | ✓ | Shared entity for redstone minecarts |
| `DIRECTIONAL_POWER_PROVIDER_MINECART` | `HorizontalDirectionalRedstoneEmitterPowerMinecartEntity` | ✓ | Directional redstone (repeater) |
| `PRESHER_PLATE_MINECART` | `PressurePlateMinecartEntity` | ✓ | Shared entity for pressure plate minecarts |
| `WEIGHT_PRESHER_PLATE_MINECART` | `WeightPressurePlateMinecartEntity` | ✓ | Shared entity for weighted pressure plate minecarts |
| `BLOCK_MINECART` | `CompatFriendlyBlockMinecartEntity` | | Generic block interaction minecart (fallback type) |
| `RAIL_MINECART` | `RailMinecartEntity` | | Shared entity for rail minecarts |
| `NON_INVENTORY_WORKING_MINECART` | `NonInventoryWorkingBlockMinecartEntity` | | Shared entity for working block minecarts |

### @MinecartMapper files

| File | Maps | Target Minecart(s) |
|---|---|---|
| `ChestEntityMapper.java` | Chest → `MinecartChest`; copper chest variants → `CopperChestMinecartEntity` | `COPPER_CHEST_MINECART` |
| `MobHeadEntityMapper.java` | All mob heads/wall heads/pumpkin → `MobHeadMinecartEntity` | `MOB_HEAD_MINECART` |
| `NonInventoryWorkingBlockEntityMapper.java` | Crafting table/stonecutter/loom/cartography/grindstone/smithing/anvil/enchanting → `NonInventoryWorkingBlockMinecartEntity`; beacon → `BeaconMinecartEntity` | 9 minecart entries |
| `PressurePlateEntityMapper.java` | All wood/stone pressure plates → `PressurePlateMinecartEntity` | `PRESHER_PLATE_MINECART_ITEM` |
| `RailMinecartEntityMapper.java` | Rail/activator/detector/powered rail → `RailMinecartEntity` | 4 rail minecarts |
| `ShulkerBoxEntityMapper.java` | 16 dyed shulker boxes + undyed → `ShulkerMinecartEntity` | `SHULKER_MINECART` |
| `SofaEntityMapper.java` | 16 wool colors → `WoolMinecartEntity` (using `SOFA_MINECART` entity) | `SOFA_MINECART` |
| `WoolEntityMapper.java` | 16 wool colors → `WoolMinecartEntity` (using `WOOL_MINECART` entity) | `WOOL_MINECART` |

---

## Auto-registration summary

When you add a `MinecartEntry` to `MRMinecarts.MINECARTS`, these systems pick it up automatically:

| System | Location | How |
|---|---|---|
| Creative tab | `MRModItems.java:40` | Iterates `MINECARTS` |
| Dispenser behavior | `MRModItems.java:52` | Iterates `MINECARTS` |
| Block/Item→Entity transform | `MinecartTransformManager.java:113` | Iterates `MINECARTS`, registers `spawnFactory` |
| Generic renderer | `MRModEntityRenderers.java:14` | Iterates `ENTITIES.getEntries()` |

You only need to manually register:
- Custom renderers (override the generic one)
- Custom `@MinecartMapper` for block-dispatch beyond auto-registration
- Custom item models (only if not using `MinecartWithBlockItem`)
- Recipes
