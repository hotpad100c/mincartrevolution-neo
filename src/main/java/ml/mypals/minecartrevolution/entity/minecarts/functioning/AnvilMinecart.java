package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class AnvilMinecart extends NonInventoryWorkingBlockMinecartEntity {
  public AnvilMinecart(EntityType<? extends AbstractMinecart> entityType, Level world) {
    super(entityType, world);
    this.mass = 7D;
  }

  @Override
  public @NonNull BlockState getDefaultDisplayBlockState() {
    return Blocks.ANVIL.defaultBlockState();
  }

  public AnvilMinecart(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      Item item) {
    super(minecart, world, x, y, z, item);
  }

  @Override
  public float getMass() {
    return 5f;
  }
}
