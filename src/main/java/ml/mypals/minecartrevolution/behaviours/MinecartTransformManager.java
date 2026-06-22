package ml.mypals.minecartrevolution.behaviours;

import static ml.mypals.minecartrevolution.MinecartRevolution.LOGGER;

import java.lang.annotation.ElementType;
import java.util.*;
import java.util.function.BiFunction;
import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.entity.minecarts.CompatFriendlyBlockMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.DamageCausingMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.*;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.FluidMinecartEntity;
import ml.mypals.minecartrevolution.manager.AnnotationManager;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.*;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;

public class MinecartTransformManager {
  public static final Map<Object, MinecartTransformConfig> factoryMap = new HashMap<>();

  static {
    // ── Specific Custom Transforms (Fluid Buckets, etc.) ─────────
    register(
        Items.WATER_BUCKET,
        MinecartTransformConfig.fluid(
            MRMinecarts.WATER_MINECART.entity()::get, Items.WATER_BUCKET));
    register(
        Items.LAVA_BUCKET,
        MinecartTransformConfig.fluid(MRMinecarts.LAVA_MINECART.entity()::get, Items.LAVA_BUCKET));

    // ── Vanilla conversions (not in MRMinecarts) ─────────────────────
    register(
        Blocks.TRAPPED_CHEST, (w, pos) -> new TrappedChestMinecartEntity(w, pos.x, pos.y, pos.z));
    register(
        Blocks.FURNACE,
        (w, pos) -> {
          AbstractMinecart m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          return m;
        });
    register(
        Blocks.BLAST_FURNACE,
        (w, pos) -> {
          AbstractMinecart m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          m.setCustomDisplayBlockState(Optional.of(Blocks.BLAST_FURNACE.defaultBlockState()));
          return m;
        });
    register(
        Blocks.SMOKER,
        (w, pos) -> {
          AbstractMinecart m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          m.setCustomDisplayBlockState(Optional.of(Blocks.SMOKER.defaultBlockState()));
          return m;
        });
    register(
        Blocks.TNT,
        (w, pos) -> {
          AbstractMinecart m = new MinecartTNT(EntityType.TNT_MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          return m;
        });
    register(
        Blocks.SPAWNER,
        (w, pos) -> {
          AbstractMinecart m = new MinecartSpawner(EntityType.SPAWNER_MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          return m;
        });
    register(
        Blocks.HOPPER,
        (w, pos) -> {
          AbstractMinecart m = new MinecartHopper(EntityType.HOPPER_MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          return m;
        });
    register(
        Blocks.COMMAND_BLOCK,
        (w, pos) -> {
          AbstractMinecart m = new MinecartCommandBlock(EntityType.COMMAND_BLOCK_MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          return m;
        });
    register(
        Blocks.AIR,
        (w, pos) -> {
          AbstractMinecart m = new Minecart(EntityType.MINECART, w);
          m.setInitialPos(pos.x, pos.y, pos.z);
          return m;
        });

    AnnotationManager manager = new AnnotationManager(MinecartMapper.class, ElementType.FIELD);
    List<ModFileScanData.AnnotationData> annotationData = manager.find();
    for (ModFileScanData.AnnotationData data : annotationData) {
      try {
        Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> map =
            (Map<Block, BiFunction<Level, Vec3, AbstractMinecart>>)
                Class.forName(data.clazz().getClassName())
                    .getDeclaredField(data.memberName())
                    .get(null);
        map.forEach(
            (block, f) -> {
              factoryMap.put(block, f::apply);
              factoryMap.put(block.asItem(), f::apply);
            });
      } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
        throw new IllegalArgumentException(
            "MinecartMapper annotation must be on a static field", e);
      }
    }
  }

  public static void init() {
    for (MRMinecarts.MinecartEntry<?, ?> entry : MRMinecarts.MINECARTS) {
      Item corItem = entry.item().get();
      if (entry.spawnFactory() != null) {
        Block blockInside = entry.item().get().getBlockInside();
        Item blockInsideItem = blockInside.asItem();
        if (corItem != Items.AIR) {
          factoryMap.put(blockInside, entry.spawnFactory()::apply);
          factoryMap.put(blockInsideItem, entry.spawnFactory()::apply);
        }
      }
    }
  }

  private static void register(Block block, MinecartTransformConfig factory) {
    factoryMap.put(block.asItem(), factory);
  }

  private static void register(Item item, MinecartTransformConfig factory) {
    factoryMap.put(item, factory);
  }

  public static AbstractMinecart spawnFromItem(
      Level world, Item item, Vec3 pos, ItemStack handStack) {
    MinecartTransformConfig factory = factoryMap.getOrDefault(item, null);
    if (factory == null) {
      if (item instanceof BucketItem bucketItem && bucketItem.getContent() != Fluids.EMPTY) {
        factory =
            (w, p) ->
                new FluidMinecartEntity(
                    MRMinecarts.WATER_MINECART.entity().get(), w, p.x, p.y, p.z, item);
      } else {
        factory =
            (w, p) ->
                new CompatFriendlyBlockMinecartEntity(
                    MRMinecarts.BLOCK_MINECART.get(), w, p.x, p.y, p.z, item);
      }
    }
    AbstractMinecart minecart = factory.createMinecart(world, pos);
    if (minecart.getDisplayBlockState().isAir()
        && item instanceof net.minecraft.world.item.BlockItem blockItem) {
      minecart.setCustomDisplayBlockState(Optional.of(blockItem.getBlock().defaultBlockState()));
    }
    Component name = handStack.getCustomName();
    minecart.setCustomName(name);
    return doExtraCheck(minecart, handStack);
  }

  public static AbstractMinecart spawnFromItemNullable(
      Level world, Item item, Vec3 pos, ItemStack handStack) {
    MinecartTransformConfig factory = factoryMap.getOrDefault(item, (w, p) -> null);
    AbstractMinecart minecart = factory.createMinecart(world, pos);
    if (minecart == null) return null;
    if (minecart.getDisplayBlockState().isAir()
        && item instanceof net.minecraft.world.item.BlockItem blockItem) {
      minecart.setCustomDisplayBlockState(Optional.of(blockItem.getBlock().defaultBlockState()));
    }
    Component name = handStack.getCustomName();
    minecart.setCustomName(name);
    return doExtraCheck(minecart, handStack);
  }

  public static AbstractMinecart spawnFromBlock(
      Level world, Block block, Vec3 pos, ItemStack handStack) {
    MinecartTransformConfig factory =
        factoryMap.getOrDefault(
            block,
            (w, p) ->
                new CompatFriendlyBlockMinecartEntity(
                    MRMinecarts.BLOCK_MINECART.get(), w, p.x, p.y, p.z, block));
    AbstractMinecart minecart = factory.createMinecart(world, pos);
    if (minecart.getDisplayBlockState().isAir()) {
      minecart.setCustomDisplayBlockState(Optional.of(block.defaultBlockState()));
    }
    Component name = handStack.getCustomName();
    minecart.setCustomName(name);
    return doExtraCheck(minecart, handStack);
  }

  public static AbstractMinecart checkForTransform(
      Level level, Vec3 pos, Item item, AbstractMinecart original, ItemStack handStack) {
    AbstractMinecart newMinecart = spawnFromItem(level, item, pos, handStack);
    return configMinecartData(level, newMinecart, original);
  }

  public static AbstractMinecart checkForTransform(
      Level level, Vec3 pos, Block block, AbstractMinecart original, ItemStack handStack) {
    AbstractMinecart minecart = spawnFromBlock(level, block, pos, handStack);
    return configMinecartData(level, minecart, original);
  }

  public static AbstractMinecart configMinecartData(
      Level level, AbstractMinecart minecart, AbstractMinecart original) {
    if (minecart != null) {
      ProblemReporter.PathElement PATH_ELEMENT = () -> "Minecart Transforming";
      ProblemReporter.ScopedCollector reporter =
          new ProblemReporter.ScopedCollector(PATH_ELEMENT, LOGGER);

      TagValueOutput valueOutput =
          TagValueOutput.createWithContext(reporter, level.registryAccess());
      TagValueOutput valueOutput1 =
          TagValueOutput.createWithContext(reporter, level.registryAccess());
      minecart.saveWithoutId(valueOutput);
      original.saveWithoutId(valueOutput1);

      CompoundTag nbtCompound = valueOutput.buildResult();
      CompoundTag nbtCompound1 = valueOutput1.buildResult();
      nbtCompound1.remove("Dimension");
      nbtCompound1.remove("DisplayState");
      nbtCompound1.remove("DisplayOffset");
      nbtCompound1.remove("CustomDisplayTile");

      ValueInput valueInput = TagValueInput.create(reporter, level.registryAccess(), nbtCompound);
      ValueInput valueInput1 = TagValueInput.create(reporter, level.registryAccess(), nbtCompound1);

      BlockState correctState = minecart.getDisplayBlockState();
      minecart.load(valueInput);
      minecart.load(valueInput1);
      if (!correctState.isAir() && minecart.getDisplayBlockState().isAir()) {
        minecart.setCustomDisplayBlockState(Optional.of(correctState));
      }
      minecart.setPortalCooldown(original.getPortalCooldown());
      minecart.portalProcess = original.portalProcess;

      minecart.setPosRaw(original.position().x, original.position().y, original.position().z);
      minecart.setDeltaMovement(original.getDeltaMovement());
      minecart.absSnapRotationTo(original.getYRot(), original.getXRot());

      if (minecart instanceof DamageCausingMinecartEntity DAMAGE_CAUSING_MINECART) {
          Block displayBlock = DAMAGE_CAUSING_MINECART.getDisplayBlockState().getBlock();

          if (displayBlock == Blocks.MAGMA_BLOCK) {
              DAMAGE_CAUSING_MINECART.damageType = DamageTypes.HOT_FLOOR;
              DAMAGE_CAUSING_MINECART.damageAmount = 1.0f;
              DAMAGE_CAUSING_MINECART.damageSource = DAMAGE_CAUSING_MINECART.damageSources().source(DAMAGE_CAUSING_MINECART.damageType);
          } else if (displayBlock == Blocks.CACTUS) {
              DAMAGE_CAUSING_MINECART.damageType = DamageTypes.CACTUS;
              DAMAGE_CAUSING_MINECART.damageAmount = 1.0f;
              DAMAGE_CAUSING_MINECART.damageSource = DAMAGE_CAUSING_MINECART.damageSources().source(DAMAGE_CAUSING_MINECART.damageType);
          } else if (displayBlock == Blocks.CAMPFIRE || displayBlock == Blocks.SOUL_CAMPFIRE) {
              DAMAGE_CAUSING_MINECART.damageType = DamageTypes.CAMPFIRE;
              DAMAGE_CAUSING_MINECART.damageAmount = 2.0f;
              DAMAGE_CAUSING_MINECART.damageSource = DAMAGE_CAUSING_MINECART.damageSources().source(DAMAGE_CAUSING_MINECART.damageType);
          }
      }

      original.remove(Entity.RemovalReason.DISCARDED);
      level.addFreshEntity(minecart);

      minecart.setHurtDir(-minecart.getHurtDir());
      minecart.setHurtTime(10);
      minecart.setDamage(50.0F);
    }
    return minecart;
  }

  public static AbstractMinecart doExtraCheck(
      AbstractMinecart abstractMinecartEntity, @Nullable ItemStack handStack) {
    if (handStack != null
        && abstractMinecartEntity instanceof ShulkerMinecartEntity shulkerMinecartEntity) {
      if (handStack.get(DataComponents.CONTAINER) != null) {
        Objects.requireNonNull(handStack.get(DataComponents.CONTAINER))
            .copyInto(shulkerMinecartEntity.getItemStacks());
      }
    }
    if (handStack != null
        && abstractMinecartEntity instanceof CompatFriendlyBlockMinecartEntity simMinecart) {
      var beData = handStack.get(DataComponents.BLOCK_ENTITY_DATA);
      if (beData != null) {
        simMinecart.setBlockEntityTag(beData.copyTagWithoutId());
      }
    }
    return abstractMinecartEntity;
  }
}
