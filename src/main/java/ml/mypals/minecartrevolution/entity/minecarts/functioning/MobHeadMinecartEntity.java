package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class MobHeadMinecartEntity extends VariantBlockMinecartEntity {
    public MobHeadMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public MobHeadMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
    }

    @Override
    public @NonNull BlockState getDefaultDisplayBlockState() {
        return Blocks.DRAGON_HEAD.defaultBlockState();
    }

    @Override
    public void tick() {
        super.tick();

    }

    public boolean scaresVillagers() {
        BlockState blockState = this.getDisplayBlockState();
        Block block = blockState.getBlock();
        return block == Blocks.ZOMBIE_HEAD || block == Blocks.ZOMBIE_WALL_HEAD ||
               block == Blocks.SKELETON_SKULL || block == Blocks.SKELETON_WALL_SKULL ||
               block == Blocks.WITHER_SKELETON_SKULL || block == Blocks.WITHER_SKELETON_WALL_SKULL ||
               block == Blocks.CREEPER_HEAD || block == Blocks.CREEPER_WALL_HEAD ||
               block == Blocks.PIGLIN_HEAD || block == Blocks.PIGLIN_WALL_HEAD;
    }
    public boolean scaresMobs() {
        BlockState blockState = this.getDisplayBlockState();
        Block block = blockState.getBlock();
        return block == Blocks.DRAGON_HEAD ;
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.MOB_HEAD_MINECART.item().get();
    }

}
