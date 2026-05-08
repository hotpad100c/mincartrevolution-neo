package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PresherPlateMinecartEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class PressurePlateEntityMapper {
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> PRESSURE_PLATE_ENTITY_MAP = new HashMap<>();

    static {
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.STONE_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.STONE_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.OAK_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.OAK_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.SPRUCE_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.SPRUCE_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.BIRCH_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.BIRCH_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.JUNGLE_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.JUNGLE_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.ACACIA_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.ACACIA_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.CHERRY_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.CHERRY_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.DARK_OAK_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.DARK_OAK_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.MANGROVE_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.MANGROVE_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.BAMBOO_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.BAMBOO_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.POLISHED_BLACKSTONE_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.WARPED_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.WARPED_PRESSURE_PLATE
                )
        );
        PRESSURE_PLATE_ENTITY_MAP.put(
                Blocks.CRIMSON_PRESSURE_PLATE,
                (world, pos) -> new PresherPlateMinecartEntity(
                        MRMinecarts.PRESHER_PLATE_MINECART.get(),
                        world,
                        pos.x(),
                        pos.y(),
                        pos.z(),
                        Items.CRIMSON_PRESSURE_PLATE
                )
        );
    }
}
