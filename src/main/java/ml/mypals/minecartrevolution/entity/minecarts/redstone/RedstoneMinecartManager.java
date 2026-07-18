package ml.mypals.minecartrevolution.entity.minecarts.redstone;

import com.hexagram2021.tetrachordlib.core.container.KDTree;
import com.hexagram2021.tetrachordlib.core.container.impl.IntPosition;
import com.hexagram2021.tetrachordlib.vanilla.MDUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public final class RedstoneMinecartManager {

  private final KDTree<PowerEmitterMinecartEntity, Integer> tree = KDTree.newLinkedKDTree(3);

  private final Map<UUID, KDTree.KDNode<PowerEmitterMinecartEntity, Integer>> nodeMap =
      new HashMap<>();

  public void add(PowerEmitterMinecartEntity cart) {
    UUID uuid = ((Entity) cart).getUUID();
    if (nodeMap.containsKey(uuid)) {
      return;
    }
    BlockPos pos = ((Entity) cart).blockPosition();
    KDTree.KDNode<PowerEmitterMinecartEntity, Integer> node =
        tree.insert(new KDTree.BuildNode<>(MDUtils.vec3i(pos), cart));
    nodeMap.put(uuid, node);
  }

  public void remove(PowerEmitterMinecartEntity cart) {
    UUID uuid = ((Entity) cart).getUUID();
    KDTree.KDNode<PowerEmitterMinecartEntity, Integer> node = nodeMap.remove(uuid);
    if (node != null) {
      tree.remove(node);
    }
  }

  public void onCartMoved(PowerEmitterMinecartEntity cart, BlockPos newPos) {
    UUID uuid = ((Entity) cart).getUUID();
    KDTree.KDNode<PowerEmitterMinecartEntity, Integer> oldNode = nodeMap.remove(uuid);
    if (oldNode != null) {
      tree.remove(oldNode);
    }
    KDTree.KDNode<PowerEmitterMinecartEntity, Integer> newNode =
        tree.insert(new KDTree.BuildNode<>(MDUtils.vec3i(newPos), cart));
    nodeMap.put(uuid, newNode);
  }

  public List<PowerEmitterMinecartEntity> queryAt(BlockPos pos) {
    if (tree.isEmpty()) {
      return List.of();
    }

    List<PowerEmitterMinecartEntity> result = new ArrayList<>();
    IntPosition target = MDUtils.vec3i(pos);

    tree.preDfs(
        (cart, position) -> {
          if (position.equals(target)) {
            result.add(cart);
          }
        });

    return result;
  }
}
