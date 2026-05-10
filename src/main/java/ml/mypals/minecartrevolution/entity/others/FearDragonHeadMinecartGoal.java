package ml.mypals.minecartrevolution.entity.others;

import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FearDragonHeadMinecartGoal extends PanicGoal {
    private final PathfinderMob mob;

    private MobHeadMinecartEntity scaryMinecart;

    public FearDragonHeadMinecartGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier);

        this.mob = mob;
    }

    @Override
    protected boolean shouldPanic() {

        List<MobHeadMinecartEntity> minecarts =
                mob.level().getEntitiesOfClass(
                        MobHeadMinecartEntity.class,
                        mob.getBoundingBox().inflate(8)
                );

        if (minecarts.isEmpty()) {
            scaryMinecart = null;
            return false;
        }

        for (MobHeadMinecartEntity minecart : minecarts){
            if(minecart.scaresMobs()){
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

        Vec3 pos = DefaultRandomPos.getPosAway(
                this.mob,
                30,
                30,
                scaryMinecart.position()
        );

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
        if(isRunning()){
            addParticlesAroundSelf();
        }
    }
    protected void addParticlesAroundSelf() {
        ServerLevel level = getServerLevel(mob);
        for (int i = 0; i < 5; i++) {
            double xa = level.getRandom().nextGaussian() * 0.02;
            double ya = level.getRandom().nextGaussian() * 0.02;
            double za = level.getRandom().nextGaussian() * 0.02;
            level.sendParticles(ParticleTypes.SPLASH, mob.getRandomX(1.0), mob.getRandomY() + 1.0, mob.getRandomZ(1.0),1, xa, ya, za,0.);
        }
    }
}