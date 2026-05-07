package ml.mypals.minecartrevolution.behaviours;

import ml.mypals.minecartrevolution.entity.minecarts.*;
import ml.mypals.minecartrevolution.entity.minecarts.container.*;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.*;
import ml.mypals.minecartrevolution.entity.minecarts.workingcarts.*;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static ml.mypals.minecartrevolution.MinecartRevolution.LOGGER;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.NonInventoryWorkingBlockEntityMapper.NON_INVENTORY_WORKING;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.PressurePlateEntityMapper.PRESSURE_PLATE_ENTITY_MAP;
import static ml.mypals.minecartrevolution.entity.minecarts.maps.ShulkerBoxEntityMapper.SHULKER_ENTITY_MAP;

public class MinecartTransformManager {
    public static final ProblemReporter.PathElement PATH_ELEMENT = new ProblemReporter.PathElement() {
        @Override
        public @NonNull String get() {
            return "Minecart Transforming";
        }
    };

    public static AbstractMinecart checkForTransform(Level world, Vec3 pos, Block block, AbstractMinecart original, ItemStack handStack) {
        AbstractMinecart minecart = getTransform(world, pos, block, handStack);
        if (minecart != null) {

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

    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> factoryMap = new java.util.HashMap<>();

    static {
        factoryMap.putAll(Map.ofEntries(
                Map.entry(Blocks.TRAPPED_CHEST, (w, pos) -> new TrappedChestMinecartEntity(w, pos.x, pos.y, pos.z)),
                Map.entry(Blocks.CHEST, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartChest(EntityType.CHEST_MINECART, w);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }),

                Map.entry(Blocks.FURNACE, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }),
                Map.entry(Blocks.BLAST_FURNACE, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.BLAST_FURNACE.defaultBlockState()));
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }),
                Map.entry(Blocks.SMOKER, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartFurnace(EntityType.FURNACE_MINECART, w);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.SMOKER.defaultBlockState()));
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }),

                Map.entry(Blocks.TNT, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartTNT(EntityType.TNT_MINECART, w);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }),
                Map.entry(Blocks.SPAWNER, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartSpawner(EntityType.SPAWNER_MINECART, w);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }),
                Map.entry(Blocks.HOPPER, (w, pos) -> {
                    MinecartHopper hopperMinecartEntity = new MinecartHopper(EntityType.HOPPER_MINECART, w);
                    hopperMinecartEntity.setInitialPos(pos.x, pos.y, pos.z);
                    hopperMinecartEntity.setDisplayOffset(1);
                    hopperMinecartEntity.setCustomDisplayBlockState(Optional.of(Blocks.HOPPER.defaultBlockState()));
                    return hopperMinecartEntity;
                }),
                Map.entry(Blocks.DRAGON_EGG, (w, pos) -> new DragonEggMinecart(MRMinecarts.DRAGON_EGG_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.DRAGON_EGG_MINECART.item().get())),
                Map.entry(Blocks.COBWEB, (w, pos) -> new CobwebMinecartEntity(MRMinecarts.COBWEB_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.COBWEB_MINECART.item().get())),
                Map.entry(Blocks.COMMAND_BLOCK, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartCommandBlock(EntityType.COMMAND_BLOCK_MINECART, w);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }),
                Map.entry(Blocks.CACTUS, (w, pos) -> new DamageCausingMinecartEntity(MRMinecarts.DAMAGE_CAUSING_MINECART.get(), w, pos.x, pos.y, pos.z, 1f, MRMinecarts.CACTUS_MINECART.item().get(), DamageTypes.CACTUS)),
                Map.entry(Blocks.MAGMA_BLOCK, (w, pos) -> new DamageCausingMinecartEntity(MRMinecarts.DAMAGE_CAUSING_MINECART.get(), w, pos.x, pos.y, pos.z, 1f, MRMinecarts.MAGMA_BLOCK_MINECART.item().get(), DamageTypes.HOT_FLOOR)),
                Map.entry(Blocks.CAMPFIRE, (w, pos) -> new DamageCausingMinecartEntity(MRMinecarts.DAMAGE_CAUSING_MINECART.get(), w, pos.x, pos.y, pos.z, 1f, MRMinecarts.CAMPFIRE_MINECART.item().get(), DamageTypes.CAMPFIRE)),
                Map.entry(Blocks.SOUL_CAMPFIRE, (w, pos) -> new DamageCausingMinecartEntity(MRMinecarts.DAMAGE_CAUSING_MINECART.get(), w, pos.x, pos.y, pos.z, 2f, MRMinecarts.SOUL_CAMPFIRE_MINECART.item().get(), DamageTypes.CAMPFIRE)),
                Map.entry(Blocks.REDSTONE_BLOCK, (w, pos) -> new RedstoneBlockMinecartEntity(MRMinecarts.POWER_PROVIDER_MINECART.get(), w, pos.x, pos.y, pos.z, MRMinecarts.REDSTONE_MINECART.item().get())),
                Map.entry(Blocks.REPEATER, (w, pos) -> new HorizontalDirectionalRedstoneEmitterPowerMinecartEntity(MRMinecarts.DIRECTIONAL_POWER_PROVIDER_MINECART.get(), w, pos.x, pos.y, pos.z, MRMinecarts.REPEATER_MINECART.item().get())),
                Map.entry(Blocks.SPONGE, (w, pos) -> new SpongeMinecartEntity(MRMinecarts.SPONGE_MINECART.entity().get(), w, pos.x, pos.y, pos.z, SpongeMinecartEntity.ABSORB_RADIUS, SpongeMinecartEntity.ABSORB_LIMIT, MRMinecarts.SPONGE_MINECART.item().get())),
                Map.entry(Blocks.WET_SPONGE, (w, pos) -> new SpongeMinecartEntity(MRMinecarts.SPONGE_MINECART.entity().get(), w, pos.x, pos.y, pos.z, SpongeMinecartEntity.ABSORB_RADIUS, SpongeMinecartEntity.ABSORB_LIMIT, MRMinecarts.WET_SPONGE_MINECART.item().get())),
                Map.entry(Blocks.BARREL, (w, pos) -> new BarrelMinecartEntity(MRMinecarts.BARREL_MINECART.entity().get(), w, pos.x, pos.y, pos.z)),
                Map.entry(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, (w, pos) -> new WeightPresherPlateMinecartEntity(MRMinecarts.WEIGHT_PRESHER_PLATE_MINECART.get(), w, pos.x, pos.y, pos.z, MRMinecarts.GOLDEN_PRESHER_PLATE_MINECART.item().get())),
                Map.entry(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, (w, pos) -> new WeightPresherPlateMinecartEntity(MRMinecarts.WEIGHT_PRESHER_PLATE_MINECART.get(), w, pos.x, pos.y, pos.z, MRMinecarts.IRON_PRESHER_PLATE_MINECART.item().get())),
                Map.entry(Blocks.JUKEBOX, (w, pos) -> new JukeboxMinecartEntity(MRMinecarts.JUKEBOX_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.JUKEBOX_MINECART.item().get())),
                Map.entry(Blocks.DISPENSER, (w, pos) -> {
                    AbstractMinecart abstractMinecart = new DispenserMinecartEntity(EntityType.CHEST_MINECART, w);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                })
        ));
        factoryMap.putAll(PRESSURE_PLATE_ENTITY_MAP);
        factoryMap.putAll(SHULKER_ENTITY_MAP);
        factoryMap.putAll(NON_INVENTORY_WORKING);
    }

    public static AbstractMinecart getTransform(Level world, Vec3 pos, Block block, ItemStack handStack) {
        return doExtraCheck(factoryMap
                .getOrDefault(block, (w, p) -> new HasVariantRegularBlockMinecartEntity(MRMinecarts.BLOCK_MINECART.get(), w, pos.x, pos.y, pos.z, block))
                .apply(world, pos), handStack);
    }

    public static AbstractMinecart doExtraCheck(AbstractMinecart abstractMinecartEntity, ItemStack handStack) {
        if (abstractMinecartEntity instanceof ShulkerMinecartEntity shulkerMinecartEntity) {
            if (handStack.get(DataComponents.CONTAINER) != null) {
                Objects.requireNonNull(handStack.get(DataComponents.CONTAINER)).copyInto(shulkerMinecartEntity.getItemStacks());
            }
        }
        return abstractMinecartEntity;
    }

    public static AbstractMinecart getTransform(Level world, MinecartWithBlockItem corrospondingItem, Block blockInside, Vec3 pos, AdvancedMinecartEntityTypes.Type type) {
        return switch (type) {
            case SHULKER -> blockInside instanceof ShulkerBoxBlock shulkerBoxBlock ?
                    new ShulkerMinecartEntity(MRMinecarts.SHULKER_MINECART.entity().get(), world, pos.x, pos.y, pos.z, shulkerBoxBlock) :
                    new HasVariantRegularBlockMinecartEntity(MRMinecarts.BLOCK_MINECART.get(), world, pos.x, pos.y, pos.z, blockInside);
            case DRAGON_EGG ->
                    new DragonEggMinecart(MRMinecarts.DRAGON_EGG_MINECART.entity().get(), world, pos.x, pos.y, pos.z, corrospondingItem);
            case PRESSER_PLATE ->
                    new PresherPlateMinecartEntity(MRMinecarts.PRESHER_PLATE_MINECART.get(), world, pos.x, pos.y, pos.z, blockInside);
            case WEIGHT_PRESSER_PLATE ->
                    new WeightPresherPlateMinecartEntity(MRMinecarts.WEIGHT_PRESHER_PLATE_MINECART.get(), world, pos.x, pos.y, pos.z, corrospondingItem);
            case SPONGE ->
                    new SpongeMinecartEntity(MRMinecarts.SPONGE_MINECART.entity().get(), world, pos.x, pos.y, pos.z, SpongeMinecartEntity.ABSORB_RADIUS, SpongeMinecartEntity.ABSORB_LIMIT, corrospondingItem);
            case EMITTING_POWER_DIRECTIONAL ->
                    new HorizontalDirectionalRedstoneEmitterPowerMinecartEntity(MRMinecarts.DIRECTIONAL_POWER_PROVIDER_MINECART.get(), world, pos.x, pos.y, pos.z, corrospondingItem);
            case REGULAR ->
                    new HasVariantRegularBlockMinecartEntity(MRMinecarts.BLOCK_MINECART.get(), world, pos.x, pos.y, pos.z, blockInside);
            case EMITTING_POWER ->
                    new RedstoneBlockMinecartEntity(MRMinecarts.POWER_PROVIDER_MINECART.get(), world, pos.x, pos.y, pos.z, corrospondingItem);
            case CAUSING_DAMAGE ->
                    new DamageCausingMinecartEntity(MRMinecarts.DAMAGE_CAUSING_MINECART.get(), world, pos.x, pos.y, pos.z, 0f, corrospondingItem, DamageTypes.GENERIC);
            case BARREL ->
                    new BarrelMinecartEntity(MRMinecarts.BARREL_MINECART.entity().get(), world, pos.x, pos.y, pos.z);
            case JUKEBOX ->
                    new JukeboxMinecartEntity(MRMinecarts.JUKEBOX_MINECART.entity().get(), world, pos.x, pos.y, pos.z, corrospondingItem);
            case TRAPPED_CHEST -> new TrappedChestMinecartEntity(world, pos.x, pos.y, pos.z);
            case FURNACE -> {
                MinecartFurnace minecartFurnace = new MinecartFurnace(EntityType.FURNACE_MINECART, world);
                minecartFurnace.setCustomDisplayBlockState(Optional.of(blockInside.defaultBlockState()));
                minecartFurnace.setInitialPos(pos.x, pos.y, pos.z);
                yield minecartFurnace;
            }
            case DISPENSER -> new DispenserMinecartEntity(MRMinecarts.DISPENSER_MINECART.entity().get(), world, pos.x, pos.y, pos.z);
            case COBWEB ->
                    new CobwebMinecartEntity(MRMinecarts.COBWEB_MINECART.entity().get(), world, pos.x, pos.y, pos.z, corrospondingItem);
            case WORKING_NON_INVENTORY ->
                    new NonInventoryWorkingBlockMinecartEntity(MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(), world, pos.x, pos.y, pos.z, blockInside);
            case BEACON ->
                    new BeaconMinecartEntity(MRMinecarts.BEACON_MINECART.entity().get(), world, pos.x, pos.y, pos.z, blockInside);
            default ->
                    new HasVariantRegularBlockMinecartEntity(MRMinecarts.BLOCK_MINECART.get(), world, pos.x, pos.y, pos.z, blockInside);
        };
    }
}
