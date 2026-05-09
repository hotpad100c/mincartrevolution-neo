package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.entity.minecarts.container.CopperChestMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.CopperChestMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.BeaconMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.NonInventoryWorkingBlockMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ChestEntityMapper {
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> CHEST_MINECARTS = new HashMap<>();

    static {
        CHEST_MINECARTS.put(
                Blocks.CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new MinecartChest(EntityType.CHEST_MINECART, world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    return abstractMinecart;
                }

        );

        CHEST_MINECARTS.put(
                Blocks.COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );
        CHEST_MINECARTS.put(
                Blocks.EXPOSED_COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.EXPOSED_COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );
        CHEST_MINECARTS.put(
                Blocks.WEATHERED_COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.WEATHERED_COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );
        CHEST_MINECARTS.put(
                Blocks.OXIDIZED_COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.OXIDIZED_COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );


        CHEST_MINECARTS.put(
                Blocks.WAXED_COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.WAXED_COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );
        CHEST_MINECARTS.put(
                Blocks.WAXED_EXPOSED_COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.WAXED_EXPOSED_COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );
        CHEST_MINECARTS.put(
                Blocks.WAXED_WEATHERED_COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.WAXED_WEATHERED_COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );
        CHEST_MINECARTS.put(
                Blocks.WAXED_OXIDIZED_COPPER_CHEST,
                (world, pos) -> {
                    AbstractMinecart abstractMinecart = new CopperChestMinecartEntity(MRMinecarts.COPPER_CHEST_MINECART.entity().get(), world);
                    abstractMinecart.setInitialPos(pos.x, pos.y, pos.z);
                    abstractMinecart.setCustomDisplayBlockState(Optional.of(Blocks.WAXED_OXIDIZED_COPPER_CHEST.defaultBlockState()));
                    return abstractMinecart;
                }

        );

    }
}
