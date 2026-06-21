package ml.mypals.minecartrevolution.entity.others;

import java.util.List;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class FearMonsterMinecartGoal extends PanicGoal {
  private final PathfinderMob mob;

  private MobHeadMinecartEntity scaryMinecart;

  public FearMonsterMinecartGoal(PathfinderMob mob, double speedModifier) {
    super(mob, speedModifier);

    this.mob = mob;
  }

  @Override
  protected boolean shouldPanic() {

    List<MobHeadMinecartEntity> minecarts =
        mob.level()
            .getEntitiesOfClass(MobHeadMinecartEntity.class, mob.getBoundingBox().inflate(8));

    if (minecarts.isEmpty()) {
      scaryMinecart = null;
      return false;
    }

    for (MobHeadMinecartEntity minecart : minecarts) {
      if (minecart.scaresVillagers()) {
        scaryMinecart = minecart;

        return true;
      }
    }

    return false;
  }

  @Override
  protected boolean findRandomPosition() {

    if (scaryMinecart == null) {
      return super.findRandomPosition();
    }

    Vec3 pos = DefaultRandomPos.getPosAway(this.mob, 20, 6, scaryMinecart.position());

    if (pos == null) {
      return false;
    }

    this.posX = pos.x;
    this.posY = pos.y;
    this.posZ = pos.z;

    return true;
  }

  @Override
  public void tick() {
    if (isRunning()) {
      mob.level().broadcastEntityEvent(this.mob, EntityEvent.VILLAGER_SWEAT);
    }
  }
}
