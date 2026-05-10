package ml.mypals.minecartrevolution.entity.others;

import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
public class AttackMonsterMinecartGoal extends Goal {

    protected final Mob mob;

    protected final Class<MobHeadMinecartEntity> targetType;

    protected final int randomInterval;

    protected @Nullable MobHeadMinecartEntity target;

    public AttackMonsterMinecartGoal(
            Mob mob,
            Class<MobHeadMinecartEntity> targetType,
            int randomInterval
    ) {

        this.mob = mob;
        this.targetType = targetType;
        this.randomInterval = reducedTickDelay(randomInterval);

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {

        if (this.randomInterval > 0
                && this.mob.getRandom().nextInt(this.randomInterval) != 0) {

            return false;
        }

        this.findTarget();

        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {

        return this.target != null
                && this.target.isAlive()
                && this.mob.distanceToSqr(this.target) < 32 * 32;
    }

    protected void findTarget() {

        double range = 16.0;

        AABB searchBox = this.mob.getBoundingBox()
                .inflate(range);

        List<MobHeadMinecartEntity> minecarts =
                this.mob.level().getEntitiesOfClass(
                        this.targetType,
                        searchBox,
                        MobHeadMinecartEntity::scaresVillagers
                );

        double nearestDistance = Double.MAX_VALUE;

        MobHeadMinecartEntity nearest = null;

        for (MobHeadMinecartEntity minecart : minecarts) {

            double distance = this.mob.distanceToSqr(minecart);

            if (distance < nearestDistance) {

                nearestDistance = distance;
                nearest = minecart;
            }
        }

        this.target = nearest;
    }

    @Override
    public void tick() {

        if (this.target == null) {
            return;
        }

        this.mob.getLookControl().setLookAt(this.target);

        this.mob.getNavigation().moveTo(
                this.target.getX(),
                this.target.getY(),
                this.target.getZ(),
                1.0
        );
    }

    @Override
    public void stop() {
        this.target = null;
    }
}