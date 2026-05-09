package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class DragonEggMinecart extends SingleBlockMinecartEntity {
    public DragonEggMinecart(EntityType<DragonEggMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public DragonEggMinecart(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
    }

    @Override
    public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {
        super.activateMinecart(level, x, y, z, powered);
        if (!powered) {
            this.setDisplayOffset(0);
            return;
        }
        if (this.level().isClientSide()) {
            return;
        }
        ;
        ServerChunkCache chunkManager = ((ServerLevel) this.level()).getChunkSource();

        chunkManager.addTicketAndLoadWithRadius(TicketType.DRAGON, ChunkPos.containing(this.blockPosition()), 2);

        if (this.getHurtTime() <= 0) {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.setDamage(20.0F);
        }
        this.setDisplayOffset(this.getHurtTime());
        spawnParticles((ServerLevel) this.level(), this.blockPosition());
    }

    private static void spawnParticles(ServerLevel world, BlockPos pos) {
        RandomSource random = world.getRandom();
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (random.nextBoolean()) continue;
                int baseX = (chunkX + dx) << 4;
                int baseZ = (chunkZ + dz) << 4;
                if (Math.abs(dx) == 1) {
                    double x = (dx == -1) ? baseX : baseX + 16;
                    for (int i = 0; i < 16; i += 2) {
                        double z = baseZ + i + random.nextDouble();
                        world.sendParticles(
                                PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 0F),
                                x, pos.getY() + 0.2, z, 1, 0.0, 0.02 + random.nextDouble() * 0.18, 0.0, 0.002
                        );
                    }
                }

                if (Math.abs(dz) == 1) {
                    double z = (dz == -1) ? baseZ : baseZ + 16;
                    for (int i = 0; i < 16; i += 2) {
                        double x = baseX + i + random.nextDouble();
                        world.sendParticles(
                                PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 0F),
                                x, pos.getY() + 0.2, z, 1, 0.0, 0.02 + random.nextDouble() * 0.18, 0.0, 0.002
                        );
                    }
                }
            }
        }
    }

    @Override
    public void move(@NonNull MoverType moverType, @NonNull Vec3 delta) {
        Vec3 target = this.position().add(delta);
        double td = target.horizontalDistance();
        super.move(moverType, delta);
        Vec3 actual = this.position();
        double ad = actual.horizontalDistance();
        if (horizontalCollision && td - ad > 0.05) {
            runAway();
        }
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        boolean bl = super.hurtServer(level, source, damage);
        if (this.isAlive()) runAway();
        return bl;
    }

    private void runAway() {
        Level level = this.level();
        WorldBorder worldBorder = level.getWorldBorder();
        RandomSource random = level.getRandom();
        BlockPos pos = this.blockPosition();

        for (int i = 0; i < 1000; i++) {
            BlockPos testPos = pos.offset(
                    random.nextInt(16) - random.nextInt(16),
                    random.nextInt(8) - random.nextInt(8),
                    random.nextInt(16) - random.nextInt(16)
            );

            boolean railWanted = i < 500 && level.getBlockState(testPos.below()).getBlock() instanceof RailBlock;

            if (level.getBlockState(testPos).isAir()
                    && railWanted
                    && !level.getBlockState(testPos.below()).isAir()
                    && worldBorder.isWithinBounds(testPos)
                    && level.isInsideBuildHeight(testPos)) {

                if (level instanceof ServerLevel serverLevel) {
                    for (int j = 0; j < 128; j++) {
                        double d = random.nextDouble();
                        float xa = (random.nextFloat() - 0.5F) * 0.2F;
                        float ya = (random.nextFloat() - 0.5F) * 0.2F;
                        float za = (random.nextFloat() - 0.5F) * 0.2F;
                        double x = Mth.lerp(d, testPos.getX(), pos.getX()) + (random.nextDouble() - 0.5) + 0.5;
                        double y = Mth.lerp(d, testPos.getY(), pos.getY()) + random.nextDouble() - 0.5;
                        double z = Mth.lerp(d, testPos.getZ(), pos.getZ()) + (random.nextDouble() - 0.5) + 0.5;
                        serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, xa, ya, za, 0.0D);
                    }
                    serverLevel.levelEvent(2003, testPos, 0);
                    this.teleportTo(testPos.getX() + 0.5, testPos.getY(), testPos.getZ() + 0.5);
                    this.setDeltaMovement(0, 0, 0);
                }
                return;
            }
        }
    }
}
