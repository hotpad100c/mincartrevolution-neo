---
name: register-minecart
description: Step-by-step workflow for adding a new minecart type to the MinecartRevolution Neo mod (NeoForge 26.1.2, Java 25). Use when the user wants to register, add, or create a new minecart entity, minecart item variant, or minecart with custom behavior.
---

# Register New Minecart (注册新矿车)

## Quick start

**If the minecart has no special behavior** (just displays a block), you need **zero code changes**. The mod's `MinecartTransformManager` already converts any block into a `CompatFriendlyBlockMinecartEntity` when placed on rails. No registration required.

Registration is only needed for: a dedicated creative-tab item, a crafting recipe, custom tick/collision/render logic, or variant dispatch. See [REFERENCE.md](REFERENCE.md) for the complete file-by-file checklists.

## Workflows

Pick the scenario that matches your need:

### Scenario A: Custom entity minecart

Use when the minecart needs custom `tick()`, movement, collision, data serialization, or a custom item class.

1. Create entity class extending `SingleBlockMinecartEntity` under `entity/minecarts/`
2. (If needed) Create custom item class extending `MinecartWithBlockItem` under `item/`
3. Register in `registeries/MRMinecarts.java`
4. Add lang entries
5. (Optional) Register custom renderer in `registeries/MRModEntityRenderers.java`
6. (Optional) Add recipe in `datagen/MRRecipeProvider.java`

### Scenario B: Variant minecart (one entity, multiple block variants)

Use when one entity type serves multiple block variants (e.g. RailMinecartEntity handles all rail types). Two parallel approaches, often combined:

**B1: `registerItemOnly()`** — each variant gets a dedicated creative-tab item with its own recipe. Best when players should craft distinct items for each variant.

1. Register shared entity with `registerEntityOnly()` in `MRMinecarts.java`
2. Register each variant item with `registerItemOnly()` referencing the shared entity
3. Add lang entries for each variant

**B2: `@MinecartMapper`** — automatic block→entity dispatch when a block is placed on rails. No per-variant items needed. Best when there are many variants (e.g. 16 wool colors, 16 shulker box colors).

1. Create a mapper class in `entity/minecarts/maps/` with a `@MinecartMapper`-annotated static `Map<Block, BiFunction<Level, Vec3, AbstractMinecart>>`
2. Populate the map in a `static {}` block mapping each block variant to the entity factory

Note: B1 and B2 are often used together — items for the creative tab (B1), mapper for right-click-on-rail conversion (B2). See e.g. `RailMinecartEntityMapper` + 4× `registerItemOnly()`.

## Registration API

All registration happens in `registeries/MRMinecarts.java`. Three methods:

| Method | Purpose |
|--------|---------|
| `register(baseName, entityFactory, itemFactory)` | Registers entity type + item |
| `registerItemOnly(itemName, existingEntity, itemFactory)` | Registers item pointing to existing entity |
| `registerEntityOnly(entityName, entityFactory)` | Registers entity type only |

All entries are auto-added to `MINECARTS` list and auto-populate the creative tab, dispenser behaviors, and `MinecartTransformManager` factory map.

## Reference

See [REFERENCE.md](REFERENCE.md) for complete file-by-file checklists, code templates, conventions, and examples for each scenario.
