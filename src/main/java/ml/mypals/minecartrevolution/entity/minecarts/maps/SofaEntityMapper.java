package ml.mypals.minecartrevolution.entity.minecarts.maps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.entity.minecarts.WoolMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class SofaEntityMapper {
  @MinecartMapper
  public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> SOFA_ENTITY_MAP =
      new HashMap<>();

  static {
    for (DyeColor color : DyeColor.values()) {
      Block wool = byColor(color);
      SOFA_ENTITY_MAP.put(
          wool,
          (world, pos) ->
              new WoolMinecartEntity(
                  MRMinecarts.SOFA_MINECART.entity().get(),
                  world,
                  pos.x(),
                  pos.y(),
                  pos.z(),
                  wool.asItem()));
    }
  }

  public static Block byColor(@Nullable DyeColor dyeColor) {
    if (dyeColor == null) {
      return Blocks.WHITE_CARPET;
    } else {
      return switch (dyeColor) {
        case WHITE -> Blocks.WHITE_CARPET;
        case ORANGE -> Blocks.ORANGE_CARPET;
        case MAGENTA -> Blocks.MAGENTA_CARPET;
        case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CARPET;
        case YELLOW -> Blocks.YELLOW_CARPET;
        case LIME -> Blocks.LIME_CARPET;
        case PINK -> Blocks.PINK_CARPET;
        case GRAY -> Blocks.GRAY_CARPET;
        case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CARPET;
        case CYAN -> Blocks.CYAN_CARPET;
        case BLUE -> Blocks.BLUE_CARPET;
        case BROWN -> Blocks.BROWN_CARPET;
        case GREEN -> Blocks.GREEN_CARPET;
        case RED -> Blocks.RED_CARPET;
        case BLACK -> Blocks.BLACK_CARPET;
        case PURPLE -> Blocks.PURPLE_CARPET;
      };
    }
  }
}
