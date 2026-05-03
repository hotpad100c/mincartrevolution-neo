package ml.mypals.minecartrevolution.registry;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import ml.mypals.minecartrevolution.entity.minecarts.DamageCausingMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.MRModEntities;
import ml.mypals.minecartrevolution.entity.minecarts.SpongeMinecartEntity;
import ml.mypals.minecartrevolution.item.MRModItems;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.Blocks;
import java.util.List;

public class MinecartRegister {
    /*
    public static final List<MinecartRegistration> MINECARTS = List.of(
            new MinecartRegistration(
                    Blocks.CACTUS,
                    MRModEntities.DAMAGE_CAUSING_MINECART,
                    MRModItems.CACTUS_MINECART,
                    AdvancedMinecartEntityTypes.Type.CAUSING_DAMAGE,
                    (w, pos) -> new DamageCausingMinecartEntity(MRModEntities.DAMAGE_CAUSING_MINECART, w, pos.x, pos.y, pos.z, 1f,
                            (MinecartWithBlockItem)MRModItems.CACTUS_MINECART, DamageTypes.CACTUS),
                    null
            ),
            new MinecartRegistration(
                    Blocks.SPONGE,
                    MRModEntities.SPONGE_MINECART,
                    MRModItems.SPONGE_MINECART,
                    AdvancedMinecartEntityTypes.Type.SPONGE,
                    (w, pos) -> new SpongeMinecartEntity(MRModEntities.SPONGE_MINECART, w, pos.x, pos.y, pos.z,
                            SpongeMinecartEntity.ABSORB_RADIUS, SpongeMinecartEntity.ABSORB_LIMIT,
                            (MinecartWithBlockItem)MRModItems.SPONGE_MINECART),
                    null
            )
    );
    public static void registerAll(boolean isClient) {
        MinecartRegistration.registerAll(MINECARTS, isClient);
    }*/
}
