package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import ml.mypals.minecartrevolution.interfaces.ILevelChunkRedstoneExt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.List;

public final class RedstoneMinecartManager {

    private final ServerLevel level;

    public RedstoneMinecartManager(ServerLevel level) {
        this.level = level;
    }

    private LevelChunk getChunkIfLoaded(BlockPos pos) {
        net.minecraft.world.level.chunk.ChunkAccess chunk =
                level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        return chunk instanceof LevelChunk ? (LevelChunk) chunk : null;
    }

    public void add(PowerEmitterMinecartEntity cart) {
        BlockPos pos = ((Entity) cart).blockPosition();
        LevelChunk chunk = getChunkIfLoaded(pos);
        if (chunk != null) {
            ((ILevelChunkRedstoneExt) chunk).mincartrevolution_neo$addRedstoneMinecart(cart, pos);
        }
    }

    public void remove(PowerEmitterMinecartEntity cart) {
        BlockPos pos = ((Entity) cart).blockPosition();
        LevelChunk chunk = getChunkIfLoaded(pos);
        if (chunk != null) {
            ((ILevelChunkRedstoneExt) chunk).mincartrevolution_neo$removeRedstoneMinecart(cart, pos);
        }
    }

    public void onCartMoved(PowerEmitterMinecartEntity cart, BlockPos oldPos, BlockPos newPos) {
        LevelChunk oldChunk = getChunkIfLoaded(oldPos);
        if (oldChunk != null) {
            ((ILevelChunkRedstoneExt) oldChunk).mincartrevolution_neo$removeRedstoneMinecart(cart, oldPos);
        }
        LevelChunk newChunk = getChunkIfLoaded(newPos);
        if (newChunk != null) {
            ((ILevelChunkRedstoneExt) newChunk).mincartrevolution_neo$addRedstoneMinecart(cart, newPos);
        }
    }

    public List<PowerEmitterMinecartEntity> queryAt(BlockPos pos) {
        LevelChunk chunk = getChunkIfLoaded(pos);
        if (chunk != null) {
            return ((ILevelChunkRedstoneExt) chunk).mincartrevolution_neo$queryRedstoneMinecarts(pos);
        }
        return List.of();
    }
}
