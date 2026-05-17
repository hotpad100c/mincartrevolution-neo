package ml.mypals.minecartrevolution.entity.minecarts.maps;

import ml.mypals.minecartrevolution.annotations.MinecartMapper;
import ml.mypals.minecartrevolution.entity.minecarts.container.CopperChestMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class MobHeadEntityMapper {
    @MinecartMapper
    public static final Map<Block, BiFunction<Level, Vec3, AbstractMinecart>> HEAD_MINECARTS = new HashMap<>();

    static {
        HEAD_MINECARTS.put(Blocks.CREEPER_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.CREEPER_WALL_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.DRAGON_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.DRAGON_WALL_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));

        HEAD_MINECARTS.put(Blocks.PLAYER_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.PLAYER_WALL_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.PIGLIN_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.PIGLIN_WALL_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));

        HEAD_MINECARTS.put(Blocks.ZOMBIE_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.ZOMBIE_WALL_HEAD, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.WITHER_SKELETON_SKULL, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.WITHER_SKELETON_WALL_SKULL, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));

        HEAD_MINECARTS.put(Blocks.SKELETON_SKULL, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
        HEAD_MINECARTS.put(Blocks.SKELETON_WALL_SKULL, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));

        HEAD_MINECARTS.put(Blocks.CARVED_PUMPKIN, (w, pos) -> new MobHeadMinecartEntity(MRMinecarts.MOB_HEAD_MINECART.entity().get(), w, pos.x, pos.y, pos.z, MRMinecarts.MOB_HEAD_MINECART.item().get()));
    }
}
