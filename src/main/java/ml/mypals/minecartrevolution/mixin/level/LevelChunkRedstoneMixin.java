package ml.mypals.minecartrevolution.mixin.level;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.ILevelChunkRedstoneExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import ml.mypals.minecartrevolution.interfaces.IRedstoneListIndex;

@Mixin(LevelChunk.class)
public abstract class LevelChunkRedstoneMixin implements ILevelChunkRedstoneExt {

    @Unique
    private final Int2ReferenceOpenHashMap<List<WeakReference<PowerEmitterMinecartEntity>>>
            mincartrevolution_neo$redstoneMinecarts = new Int2ReferenceOpenHashMap<>();

    @Unique
    private int[] mincartrevolution_neo$sectionRedstoneMinecartCounts;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void mincartrevolution_neo$onInit(CallbackInfo ci) {
        LevelChunk self = (LevelChunk) (Object) this;
        mincartrevolution_neo$sectionRedstoneMinecartCounts = new int[self.getSections().length];
    }

    @Unique
    private int mincartrevolution_neo$getLocalCoord(BlockPos pos) {
        return (pos.getX() & 15) << 24 | (pos.getZ() & 15) << 20 | (pos.getY() & 0xFFFFF);
    }

    @Override
    public void mincartrevolution_neo$addRedstoneMinecart(PowerEmitterMinecartEntity cart, BlockPos pos) {
        LevelChunk self = (LevelChunk) (Object) this;
        int sectionIndex = self.getSectionIndex(pos.getY());
        if (sectionIndex >= 0 && sectionIndex < mincartrevolution_neo$sectionRedstoneMinecartCounts.length) {
            mincartrevolution_neo$sectionRedstoneMinecartCounts[sectionIndex]++;
        }
        
        int key = mincartrevolution_neo$getLocalCoord(pos);
        List<WeakReference<PowerEmitterMinecartEntity>> list = mincartrevolution_neo$redstoneMinecarts
            .computeIfAbsent(key, k -> new ArrayList<>());
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            WeakReference<PowerEmitterMinecartEntity> ref = list.get(i);
            if (ref == null || ref.get() == null) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            index = list.size();
            list.add(new WeakReference<>(cart));
        } else {
            list.set(index, new WeakReference<>(cart));
        }
        if (cart instanceof IRedstoneListIndex listIndexCart) {
            listIndexCart.mincartrevolution_neo$setRedstoneListIndex(index);
        }
    }

    @Override
    public void mincartrevolution_neo$removeRedstoneMinecart(PowerEmitterMinecartEntity cart, BlockPos pos) {
        LevelChunk self = (LevelChunk) (Object) this;
        int sectionIndex = self.getSectionIndex(pos.getY());
        if (sectionIndex >= 0 && sectionIndex < mincartrevolution_neo$sectionRedstoneMinecartCounts.length) {
            mincartrevolution_neo$sectionRedstoneMinecartCounts[sectionIndex]--;
        }
        
        int key = mincartrevolution_neo$getLocalCoord(pos);
        List<WeakReference<PowerEmitterMinecartEntity>> list = mincartrevolution_neo$redstoneMinecarts.get(key);
        if (list != null) {
            if (cart instanceof IRedstoneListIndex listIndexCart) {
                int index = listIndexCart.mincartrevolution_neo$getRedstoneListIndex();
                if (index >= 0 && index < list.size()) {
                    WeakReference<PowerEmitterMinecartEntity> ref = list.get(index);
                    if (ref != null && ref.get() == cart) {
                        list.set(index, null);
                        listIndexCart.mincartrevolution_neo$setRedstoneListIndex(-1);
                    }
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    WeakReference<PowerEmitterMinecartEntity> ref = list.get(i);
                    if (ref != null && ref.get() == cart) {
                        list.set(i, null);
                    }
                }
            }
        }
    }

    @Override
    public List<PowerEmitterMinecartEntity> mincartrevolution_neo$queryRedstoneMinecarts(BlockPos pos) {
        LevelChunk self = (LevelChunk) (Object) this;
        int sectionIndex = self.getSectionIndex(pos.getY());
        if (sectionIndex < 0 || sectionIndex >= mincartrevolution_neo$sectionRedstoneMinecartCounts.length || mincartrevolution_neo$sectionRedstoneMinecartCounts[sectionIndex] <= 0) {
            return List.of();
        }

        int key = mincartrevolution_neo$getLocalCoord(pos);
        List<WeakReference<PowerEmitterMinecartEntity>> list = mincartrevolution_neo$redstoneMinecarts.get(key);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        
        List<PowerEmitterMinecartEntity> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            WeakReference<PowerEmitterMinecartEntity> ref = list.get(i);
            if (ref != null) {
                PowerEmitterMinecartEntity entity = ref.get();
                if (entity != null) {
                    result.add(entity);
                } else {
                    // Optional: cleanup dead reference by setting to null to free up the slot
                    list.set(i, null);
                }
            }
        }
        return result;
    }
}
