package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ml.mypals.minecartrevolution.entity.minecarts.SingleBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.world.level.block.Block.dropResources;

public class PistonMinecartEntity extends SingleBlockMinecartEntity {


    public PistonMinecartEntity(EntityType<PistonMinecartEntity> entityType, Level world) {
        super(entityType, world);
    }

    public PistonMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        setCustomDisplayBlockState(Optional.of(Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.NORTH)));
    }


    @Override
    public boolean onCollision(Vec3 position, Vec3 target, Vec3 actual, Vec3 delta){

        Direction direction = Direction.getApproximateNearest(delta.horizontal());
        moveBlocks(level(), BlockPos.containing(position), direction);
        return super.onCollision(position, target, actual, delta);
    }
    private boolean moveBlocks(Level level, BlockPos pistonPos, Direction direction) {
        BlockPos armPos = pistonPos.relative(direction);

        PistonStructureResolver resolver = new PistonStructureResolver(level, pistonPos, direction, true);
        if (!resolver.resolve()) {
            return false;
        } else {
            Map<BlockPos, BlockState> deleteAfterMove = Maps.newHashMap();
            List<BlockPos> toPush = resolver.getToPush();
            List<BlockState> toPushShapes = Lists.newArrayList();

            for (BlockPos pos : toPush) {
                BlockState state = level.getBlockState(pos);
                toPushShapes.add(state);
                deleteAfterMove.put(pos, state);
            }

            List<BlockPos> toDestroy = resolver.getToDestroy();
            BlockState[] toUpdate = new BlockState[toPush.size() + toDestroy.size()];
            Direction pushDirection = true ? direction : direction.getOpposite();
            int updateIndex = 0;

            for (int i = toDestroy.size() - 1; i >= 0; i--) {
                BlockPos pos = toDestroy.get(i);
                BlockState state = level.getBlockState(pos);
                BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                dropResources(state, level, pos, blockEntity);
                if (!state.is(BlockTags.FIRE) && level.isClientSide()) {
                    level.levelEvent(2001, pos, Block.getId(state));
                }

                state.onDestroyedByPushReaction(level, pos, direction, level.getFluidState(pos));
                toUpdate[updateIndex++] = state;
            }

            for (int i = toPush.size() - 1; i >= 0; i--) {
                BlockPos pos = toPush.get(i);
                BlockState blockState = level.getBlockState(pos);
                pos = pos.relative(pushDirection);
                deleteAfterMove.remove(pos);
                BlockState actualState = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction);
                level.setBlock(pos, actualState, 324);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(pos, actualState, toPushShapes.get(i), direction, true, false));
                toUpdate[updateIndex++] = blockState;
            }

  /*          if (extending) {
                PistonType type = PistonType.DEFAULT;
                BlockState state = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction).setValue(PistonHeadBlock.TYPE, type);
                BlockState blockState = Blocks.MOVING_PISTON
                        .defaultBlockState()
                        .setValue(MovingPistonBlock.FACING, direction)
                        .setValue(MovingPistonBlock.TYPE,  PistonType.DEFAULT);
                deleteAfterMove.remove(armPos);
                level.setBlock(armPos, blockState, 324);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(armPos, blockState, state, direction, true, true));
            }
*/
            BlockState air = Blocks.AIR.defaultBlockState();

            for (BlockPos pos : deleteAfterMove.keySet()) {
                level.setBlock(pos, air, 82);
            }

            for (Map.Entry<BlockPos, BlockState> entry : deleteAfterMove.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState oldState = entry.getValue();
                oldState.updateIndirectNeighbourShapes(level, pos, 2);
                air.updateNeighbourShapes(level, pos, 2);
                air.updateIndirectNeighbourShapes(level, pos, 2);
            }

            Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, resolver.getPushDirection(), null);
            updateIndex = 0;

            for (int i = toDestroy.size() - 1; i >= 0; i--) {
                BlockState state = toUpdate[updateIndex++];
                BlockPos pos = toDestroy.get(i);
                if (level instanceof ServerLevel serverLevel) {
                    state.affectNeighborsAfterRemoval(serverLevel, pos, false);
                }

                state.updateIndirectNeighbourShapes(level, pos, 2);
                level.updateNeighborsAt(pos, state.getBlock(), orientation);
            }

            for (int i = toPush.size() - 1; i >= 0; i--) {
                level.updateNeighborsAt(toPush.get(i), toUpdate[updateIndex++].getBlock(), orientation);
            }

            if (true) {
                level.updateNeighborsAt(armPos, Blocks.PISTON_HEAD, orientation);
            }

            return true;
        }
    }
    @Override
    public float getCollisionSensitive() {
        return 0.1f;
    }
}