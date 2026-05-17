package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.entity.minecarts.WoolMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class WoolEntityMapper {
    @MinecartMapper
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> WOOL_ENTITY_MAP = new HashMap<>();
    static {
        for(DyeColor color : DyeColor.values()){
            Block wool = byColor(color);
            WOOL_ENTITY_MAP.put(
                    wool,
                    (world, pos) -> new WoolMinecartEntity(
                            MRMinecarts.WOOL_MINECART.entity().get(),
                            world,
                            pos.x(),
                            pos.y(),
                            pos.z(),
                            wool.asItem()
                    )
            );
        }
    }
    public static Block byColor(@Nullable DyeColor dyeColor){
        if (dyeColor == null) {
            return Blocks.WHITE_WOOL;
        } else {
            return switch (dyeColor) {
                case WHITE -> Blocks.WHITE_WOOL;
                case ORANGE -> Blocks.ORANGE_WOOL;
                case MAGENTA -> Blocks.MAGENTA_WOOL;
                case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
                case YELLOW -> Blocks.YELLOW_WOOL;
                case LIME -> Blocks.LIME_WOOL;
                case PINK -> Blocks.PINK_WOOL;
                case GRAY -> Blocks.GRAY_WOOL;
                case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
                case CYAN -> Blocks.CYAN_WOOL;
                case BLUE -> Blocks.BLUE_WOOL;
                case BROWN -> Blocks.BROWN_WOOL;
                case GREEN -> Blocks.GREEN_WOOL;
                case RED -> Blocks.RED_WOOL;
                case BLACK -> Blocks.BLACK_WOOL;
                case PURPLE -> Blocks.PURPLE_WOOL;
            };
        }
    }
}
