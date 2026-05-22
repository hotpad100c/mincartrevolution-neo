package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.entity.minecarts.RailMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.BeaconMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.NonInventoryWorkingBlockMinecartEntity;
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

public class RailMinecartEntityMapper {
    @MinecartMapper
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> RAIL_MINECARTS = new HashMap<>();

    static {
        RAIL_MINECARTS.put(
                Blocks.RAIL,
                (world, pos) -> new RailMinecartEntity(
                        MRMinecarts.RAIL_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.RAIL
                )
        );
        RAIL_MINECARTS.put(
                Blocks.ACTIVATOR_RAIL,
                (world, pos) -> new RailMinecartEntity(
                        MRMinecarts.RAIL_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.ACTIVATOR_RAIL
                )
        );
        RAIL_MINECARTS.put(
                Blocks.DETECTOR_RAIL,
                (world, pos) -> new RailMinecartEntity(
                        MRMinecarts.RAIL_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.DETECTOR_RAIL
                )
        );
        RAIL_MINECARTS.put(
                Blocks.POWERED_RAIL,
                (world, pos) -> new RailMinecartEntity(
                        MRMinecarts.RAIL_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.POWERED_RAIL
                )
        );
    }
}
