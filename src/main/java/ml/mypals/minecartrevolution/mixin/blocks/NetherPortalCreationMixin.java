package ml.mypals.minecartrevolution.mixin.blocks;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.NetherPortalMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PortalShape.class)
public class NetherPortalCreationMixin {
  @Shadow @Final private Direction.Axis axis;

  @Shadow @Final private BlockPos bottomLeft;

  @Shadow @Final private int height;

  @Shadow @Final private Direction rightDir;

  @Shadow @Final private int width;

  @WrapMethod(method = "createPortalBlocks")
  public void createPortalBlocks(LevelAccessor level, Operation<Void> original) {
    Iterable<BlockPos> portals =
        BlockPos.betweenClosed(
            this.bottomLeft,
            this.bottomLeft
                .relative(Direction.UP, this.height - 1)
                .relative(this.rightDir, this.width - 1));
    original.call(level);
    Set<BlockPos> toUpdate = new HashSet<>();
    for (BlockPos pos : portals) {
      List<Minecart> minecarts =
          level.getEntitiesOfClass(Minecart.class, AABB.of(BoundingBox.fromCorners(pos, pos)));
      if (!minecarts.isEmpty()) {
        Minecart minecart = minecarts.getFirst();
        minecart.remove(Entity.RemovalReason.DISCARDED);
        NetherPortalMinecartEntity netherPortalMinecart =
            new NetherPortalMinecartEntity(
                MRMinecarts.PORTAL_MINECART.entity().get(),
                minecart.level(),
                minecart.getX(),
                minecart.getY(),
                minecart.getZ(),
                MRMinecarts.PORTAL_MINECART.item().asItem());
        level.addFreshEntity(netherPortalMinecart);
        if (level instanceof ServerLevel serverLevel) {
          net.minecraft.world.entity.player.Player player =
              serverLevel.getNearestPlayer(minecart, 10.0);
          if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ml.mypals.minecartrevolution.registeries.MRModCriteria.BLOCK_CART_CRAFTED
                .get()
                .trigger(serverPlayer, netherPortalMinecart);
          }
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2, 0);
        toUpdate.add(pos);
      }
    }
    for (BlockPos pos : toUpdate) {
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
  }
}
