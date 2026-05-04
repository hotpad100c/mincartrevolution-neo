package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class DragonEggMinecart extends ItemBoundBlockMinecartEntity{
    public DragonEggMinecart(EntityType<DragonEggMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public DragonEggMinecart(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
    }

    protected DragonEggMinecart(EntityType<? extends AbstractMinecart> entityType, Level world, MinecartWithBlockItem correspondingItem) {
        super(entityType, world, correspondingItem);
    }


    @Override
    public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {
        super.activateMinecart(level, x, y, z, powered);
        if(!powered){
            this.setDisplayOffset(0);
            return;
        }
        if(this.level().isClientSide()) {
            return;
        };
        ServerChunkCache chunkManager = ((ServerLevel)this.level()).getChunkSource();

        chunkManager.addTicketAndLoadWithRadius(TicketType.DRAGON, ChunkPos.containing(this.blockPosition()), 2);

        if(this.getHurtTime() <= 0){
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.setDamage(20.0F);
        }
        this.setDisplayOffset(this.getHurtTime());
        spawnParticles((ServerLevel)this.level(), this.blockPosition());
    }
    private static void spawnParticles(ServerLevel world, BlockPos pos) {
        RandomSource random = world.getRandom();

        for (Direction direction : Direction.values()) {
            BlockPos blockPos = pos.relative(direction);
            if (!world.getBlockState(blockPos).isSolidRender()) {
                Direction.Axis axis = direction.getAxis();
                double e = axis == Direction.Axis.X ? 0.5 + 0.5625 * direction.getStepX() : random.nextFloat();
                double f = axis == Direction.Axis.Y ? 0.5 + 0.5625 * direction.getStepY() : random.nextFloat();
                double g = axis == Direction.Axis.Z ? 0.5 + 0.5625 * direction.getStepZ() : random.nextFloat();
                world.addParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 0.2F), pos.getX() + e, pos.getY() + f, pos.getZ() + g, 0.0, 0.0, 0.0);
            }
        }
    }
}
