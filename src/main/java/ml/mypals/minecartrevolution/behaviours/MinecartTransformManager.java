package ml.mypals.minecartrevolution.behaviours;

import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.entity.minecarts.*;
import ml.mypals.minecartrevolution.entity.minecarts.container.*;
import ml.mypals.minecartrevolution.manager.AnnotationManager;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.util.*;
import java.util.function.BiFunction;

import static ml.mypals.minecartrevolution.MinecartRevolution.LOGGER;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.ChestEntityMapper.CHEST_MINECARTS;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.MobHeadEntityMapper.HEAD_MINECARTS;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.NonInventoryWorkingBlockEntityMapper.NON_INVENTORY_WORKING;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.PressurePlateEntityMapper.PRESSURE_PLATE_ENTITY_MAP;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.ShulkerBoxEntityMapper.SHULKER_ENTITY_MAP;

public class MinecartTransformManager {
    public static final Map<Item, BiFunction<Level, Vec3, AbstractMinecart>> factoryMap = new HashMap<>();

    static {
        // ── Vanilla conversions (not in MRMinecarts) ─────────────────────
        register(Blocks.TRAPPED_CHEST,   (w, pos) -> new TrappedChestMinecartEntity(w, pos.x, pos.y, pos.z));
        register(Blocks.FURNACE,         (w, pos) -> {
            AbstractMinecart m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            return m;
        });
        register(Blocks.BLAST_FURNACE,   (w, pos) -> {
            AbstractMinecart m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            m.setCustomDisplayBlockState(Optional.of(Blocks.BLAST_FURNACE.defaultBlockState()));
            return m;
        });
        register(Blocks.SMOKER,          (w, pos) -> {
            AbstractMinecart m = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            m.setCustomDisplayBlockState(Optional.of(Blocks.SMOKER.defaultBlockState()));
            return m;
        });
        register(Blocks.TNT,             (w, pos) -> {
            AbstractMinecart m = new MinecartTNT(EntityType.TNT_MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            return m;
        });
        register(Blocks.SPAWNER,         (w, pos) -> {
            AbstractMinecart m = new MinecartSpawner(EntityType.SPAWNER_MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            return m;
        });
        register(Blocks.HOPPER,          (w, pos) -> {
            AbstractMinecart m = new MinecartHopper(EntityType.HOPPER_MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            return m;
        });
        register(Blocks.COMMAND_BLOCK,   (w, pos) -> {
            AbstractMinecart m = new MinecartCommandBlock(EntityType.COMMAND_BLOCK_MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            return m;
        });
        register(Blocks.AIR,             (w, pos) -> {
            AbstractMinecart m = new Minecart(EntityType.MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            return m;
        });
        register(Items.AIR,              (w, pos) -> {
            AbstractMinecart m = new Minecart(EntityType.MINECART, w);
            m.setInitialPos(pos.x, pos.y, pos.z);
            return m;
        });

        // ── Automatic Mapper Loading ──
        AnnotationManager manager = new AnnotationManager(MinecartMapper.class, ElementType.FIELD);
        List<ModFileScanData.AnnotationData> annotationData = manager.find();
        for (ModFileScanData.AnnotationData data : annotationData) {
            try {
                Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> map = (Map<Block, BiFunction<Level, Vec3, AbstractMinecart>>) Class.forName(data.clazz().getClassName()).getDeclaredField(data.memberName()).get(null);
                map.forEach((block, f) -> factoryMap.put(block.asItem(), f));
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
                throw new IllegalArgumentException("MinecartMapper annotation must be on a static field", e);
            }
        }


        // ── Auto-register from MRMinecarts.MINECARTS ─────────────────────
        for (MRMinecarts.MinecartEntry<?, ?> entry : MRMinecarts.MINECARTS) {
            if (entry.spawnFactory() != null) {
                factoryMap.put(entry.item().get(), entry.spawnFactory());
                factoryMap.put(entry.item().get().getBlockInside().asItem(), entry.spawnFactory());
            }
        }
    }

    private static void register(Block block, BiFunction<Level, Vec3, AbstractMinecart> factory) {
        factoryMap.put(block.asItem(), factory);
    }

    private static void register(Item item, BiFunction<Level, Vec3, AbstractMinecart> factory) {
        factoryMap.put(item, factory);
    }

    public static AbstractMinecart spawnFromItem(Level world, Item item, Vec3 pos, @Nullable ItemStack handStack) {
        BiFunction<Level, Vec3, AbstractMinecart> factory =
            factoryMap.getOrDefault(item,
                (w, p) -> new VariantBlockMinecartEntity(MRMinecarts.BLOCK_MINECART.get(), w, p.x, p.y, p.z, item));
        AbstractMinecart minecart = factory.apply(world, pos);
        return doExtraCheck(minecart, handStack);
    }

    public static AbstractMinecart checkForTransform(Level world, Vec3 pos, Item item, AbstractMinecart original, ItemStack handStack) {
        AbstractMinecart minecart = spawnFromItem(world, item, pos, handStack);
        if (minecart != null) {
            ProblemReporter.PathElement PATH_ELEMENT = () -> "Minecart Transforming";
            ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(PATH_ELEMENT, LOGGER);

            TagValueOutput valueOutput = TagValueOutput.createWithContext(reporter, world.registryAccess());
            TagValueOutput valueOutput1 = TagValueOutput.createWithContext(reporter, world.registryAccess());
            minecart.saveWithoutId(valueOutput);
            original.saveWithoutId(valueOutput1);

            CompoundTag nbtCompound = valueOutput.buildResult();
            CompoundTag nbtCompound1 = valueOutput1.buildResult();
            nbtCompound1.remove("Dimension");

            ValueInput valueInput = TagValueInput.create(reporter, world.registryAccess(), nbtCompound);
            ValueInput valueInput1 = TagValueInput.create(reporter, world.registryAccess(), nbtCompound1);

            minecart.load(valueInput);
            minecart.load(valueInput1);
            minecart.setPortalCooldown(original.getPortalCooldown());
            minecart.portalProcess = original.portalProcess;

            minecart.setPosRaw(original.position().x, original.position().y, original.position().z);
            minecart.setDeltaMovement(original.getDeltaMovement());
            minecart.absSnapRotationTo(original.getYRot(), original.getXRot());
            original.remove(Entity.RemovalReason.DISCARDED);
            world.addFreshEntity(minecart);

            minecart.setHurtDir(-minecart.getHurtDir());
            minecart.setHurtTime(10);
            minecart.setDamage(50.0F);
        }
        return minecart;
    }

    public static AbstractMinecart doExtraCheck(AbstractMinecart abstractMinecartEntity, @Nullable ItemStack handStack) {
        if (handStack != null && abstractMinecartEntity instanceof ShulkerMinecartEntity shulkerMinecartEntity) {
            if (handStack.get(DataComponents.CONTAINER) != null) {
                Objects.requireNonNull(handStack.get(DataComponents.CONTAINER)).copyInto(shulkerMinecartEntity.getItemStacks());
            }
        }
        return abstractMinecartEntity;
    }
}
