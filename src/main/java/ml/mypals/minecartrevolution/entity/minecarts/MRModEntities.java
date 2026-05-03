package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.MinecartRevolution;
import ml.mypals.minecartrevolution.entity.minecarts.container.BarrelMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.TrappedChestMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.HorizontalDirectionalRedstoneEmitterPowerMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PresherPlateMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.RedstoneBlockMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.WeightPresherPlateMinecartEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

import static ml.mypals.minecartrevolution.MinecartRevolution.ENTITIES;

public class MRModEntities {

    public static final Supplier<EntityType<DamageCausingMinecartEntity>> DAMAGE_CAUSING_MINECART = ENTITIES.register(
            "harmful_minecart",
            () -> EntityType.Builder.<DamageCausingMinecartEntity>of(DamageCausingMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "harmful_minecart")))
    );

    public static final Supplier<EntityType<RedstoneBlockMinecartEntity>> POWER_PROVIDER_MINECART = ENTITIES.register(
            "power_minecart",
            () -> EntityType.Builder.<RedstoneBlockMinecartEntity>of(RedstoneBlockMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "power_minecart")))
    );

    public static final Supplier<EntityType<RedstoneBlockMinecartEntity>> DIRECTIONAL_POWER_PROVIDER_MINECART = ENTITIES.register(
            "power_minecart_directional",
            () -> EntityType.Builder.<RedstoneBlockMinecartEntity>of(HorizontalDirectionalRedstoneEmitterPowerMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "power_minecart_directional")))
    );

    public static final Supplier<EntityType<HasVariantRegularBlockMinecartEntity>> BLOCK_MINECART = ENTITIES.register(
            "block_minecart",
            () -> EntityType.Builder.<HasVariantRegularBlockMinecartEntity>of(HasVariantRegularBlockMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "block_minecart")))
    );

    public static final Supplier<EntityType<SpongeMinecartEntity>> SPONGE_MINECART = ENTITIES.register(
            "sponge_minecart",
            () -> EntityType.Builder.<SpongeMinecartEntity>of(SpongeMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "sponge_minecart")))
    );

    public static final Supplier<EntityType<BarrelMinecartEntity>> BARREL_MINECART = ENTITIES.register(
            "barrel_minecart",
            () -> EntityType.Builder.<BarrelMinecartEntity>of(BarrelMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "barrel_minecart")))
    );

    public static final Supplier<EntityType<TrappedChestMinecartEntity>> TRAPPED_CHEST_MINECART = ENTITIES.register(
            "trapped_chest_minecart",
            () -> EntityType.Builder.<TrappedChestMinecartEntity>of(TrappedChestMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "trapped_chest_minecart")))
    );

    public static final Supplier<EntityType<PresherPlateMinecartEntity>> PRESHER_PLATE_MINECART = ENTITIES.register(
            "presher_plate_minecart",
            () -> EntityType.Builder.<PresherPlateMinecartEntity>of(PresherPlateMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "presher_plate_minecart")))
    );

    public static final Supplier<EntityType<WeightPresherPlateMinecartEntity>> WEIGHT_PRESHER_PLATE_MINECART = ENTITIES.register(
            "weight_presher_plate_minecart",
            () -> EntityType.Builder.<WeightPresherPlateMinecartEntity>of(WeightPresherPlateMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "weight_presher_plate_minecart")))
    );

    public static final Supplier<EntityType<JukeboxMinecartEntity>> JUKEBOX_MINECART = ENTITIES.register(
            "jukebox_minecart",
            () -> EntityType.Builder.<JukeboxMinecartEntity>of(JukeboxMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "jukebox_minecart")))
    );

    public static final Supplier<EntityType<ShulkerMinecartEntity>> SHULKER_MINECART = ENTITIES.register(
            "shulker_minecart",
            () -> EntityType.Builder.<ShulkerMinecartEntity>of(ShulkerMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "shulker_minecart")))
    );

    public static final Supplier<EntityType<DragonEggMinecart>> DRAGON_EGG_MINECART = ENTITIES.register(
            "dragon_egg_minecart",
            () -> EntityType.Builder.<DragonEggMinecart>of(DragonEggMinecart::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MinecartRevolution.MODID, "dragon_egg_minecart")))
    );

    public static void init() {
        // Method to ensure class loading
    }
}