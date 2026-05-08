package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.entity.minecarts.workingcarts.BeaconMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.workingcarts.NonInventoryWorkingBlockMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class NonInventoryWorkingBlockEntityMapper {
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> NON_INVENTORY_WORKING = new HashMap<>();

    static {
        NON_INVENTORY_WORKING.put(
                Blocks.CRAFTING_TABLE,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.CRAFTING_TABLE
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.STONECUTTER,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.STONECUTTER
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.LOOM,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.LOOM
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.CARTOGRAPHY_TABLE,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.CARTOGRAPHY_TABLE
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.GRINDSTONE,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.GRINDSTONE
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.SMITHING_TABLE,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.SMITHING_TABLE
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.ANVIL,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.ANVIL
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.CHIPPED_ANVIL,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.CHIPPED_ANVIL
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.DAMAGED_ANVIL,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.DAMAGED_ANVIL
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.ENCHANTING_TABLE,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.ENCHANTING_TABLE
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.ENDER_CHEST,
                (world, pos) -> new NonInventoryWorkingBlockMinecartEntity(
                        MRMinecarts.NON_INVENTORY_WORKING_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.ENDER_CHEST
                )
        );
        NON_INVENTORY_WORKING.put(
                Blocks.BEACON,
                (world, pos) -> new BeaconMinecartEntity(
                        MRMinecarts.BEACON_MINECART.entity().get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.BEACON
                )
        );
    }
}
