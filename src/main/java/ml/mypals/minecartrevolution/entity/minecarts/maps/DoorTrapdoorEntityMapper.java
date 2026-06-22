package ml.mypals.minecartrevolution.entity.minecarts.maps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.DoorMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.TrapdoorMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DoorTrapdoorEntityMapper {
    @MinecartMapper
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> DOOR_TRAPDOOR_MINECARTS =
            new HashMap<>();

    static {
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.IRON_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.IRON_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.OAK_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.OAK_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.SPRUCE_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.SPRUCE_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.BIRCH_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.BIRCH_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.JUNGLE_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.JUNGLE_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.ACACIA_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.ACACIA_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.DARK_OAK_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.DARK_OAK_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.MANGROVE_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.MANGROVE_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.CHERRY_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.CHERRY_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.BAMBOO_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.BAMBOO_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.CRIMSON_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.CRIMSON_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WARPED_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WARPED_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.COPPER_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.EXPOSED_COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.EXPOSED_COPPER_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WEATHERED_COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WEATHERED_COPPER_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.OXIDIZED_COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.OXIDIZED_COPPER_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_COPPER_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_EXPOSED_COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_EXPOSED_COPPER_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_WEATHERED_COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_WEATHERED_COPPER_DOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_OXIDIZED_COPPER_DOOR,
                (world, pos) -> new DoorMinecartEntity(MRMinecarts.IRON_DOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_OXIDIZED_COPPER_DOOR_MINECART.item().get()));

        DOOR_TRAPDOOR_MINECARTS.put(Blocks.IRON_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.IRON_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.OAK_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.OAK_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.SPRUCE_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.SPRUCE_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.BIRCH_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.BIRCH_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.JUNGLE_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.JUNGLE_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.ACACIA_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.ACACIA_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.DARK_OAK_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.DARK_OAK_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.MANGROVE_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.MANGROVE_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.CHERRY_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.CHERRY_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.BAMBOO_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.BAMBOO_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.CRIMSON_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.CRIMSON_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WARPED_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WARPED_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.COPPER_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.EXPOSED_COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.EXPOSED_COPPER_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WEATHERED_COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WEATHERED_COPPER_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.OXIDIZED_COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.OXIDIZED_COPPER_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_COPPER_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_EXPOSED_COPPER_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_WEATHERED_COPPER_TRAPDOOR_MINECART.item().get()));
        DOOR_TRAPDOOR_MINECARTS.put(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR,
                (world, pos) -> new TrapdoorMinecartEntity(MRMinecarts.IRON_TRAPDOOR_MINECART.entity().get(),
                        world, pos.x(), pos.y(), pos.z(), MRMinecarts.WAXED_OXIDIZED_COPPER_TRAPDOOR_MINECART.item().get()));
    }
}
