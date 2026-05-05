package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ShulkerBoxEntityMapper {
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> SHULKER_ENTITY_MAP = new HashMap<>();

    static {
        for(DyeColor color : DyeColor.values()) {
            Block shulkerBox = byColor(color);
            SHULKER_ENTITY_MAP.put(
                    shulkerBox,
                    (world, pos) -> new ShulkerMinecartEntity(
                            MRMinecarts.SHULKER_MINECART.entity().get(),
                            world,
                            pos.x(),
                            pos.y(),
                            pos.z(),
                            (ShulkerBoxBlock) shulkerBox
                    )
            );
        }
    }
    public static Block byColor(@Nullable DyeColor dyeColor) {
        if (dyeColor == null) {
            return Blocks.SHULKER_BOX;
        } else {
            return switch (dyeColor) {
                case WHITE -> Blocks.WHITE_SHULKER_BOX;
                case ORANGE -> Blocks.ORANGE_SHULKER_BOX;
                case MAGENTA -> Blocks.MAGENTA_SHULKER_BOX;
                case LIGHT_BLUE -> Blocks.LIGHT_BLUE_SHULKER_BOX;
                case YELLOW -> Blocks.YELLOW_SHULKER_BOX;
                case LIME -> Blocks.LIME_SHULKER_BOX;
                case PINK -> Blocks.PINK_SHULKER_BOX;
                case GRAY -> Blocks.GRAY_SHULKER_BOX;
                case LIGHT_GRAY -> Blocks.LIGHT_GRAY_SHULKER_BOX;
                case CYAN -> Blocks.CYAN_SHULKER_BOX;
                case BLUE -> Blocks.BLUE_SHULKER_BOX;
                case BROWN -> Blocks.BROWN_SHULKER_BOX;
                case GREEN -> Blocks.GREEN_SHULKER_BOX;
                case RED -> Blocks.RED_SHULKER_BOX;
                case BLACK -> Blocks.BLACK_SHULKER_BOX;
                case PURPLE -> Blocks.PURPLE_SHULKER_BOX;
            };
        }
    }
}
