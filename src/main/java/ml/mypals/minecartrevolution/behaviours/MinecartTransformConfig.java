package ml.mypals.minecartrevolution.behaviours;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import ml.mypals.minecartrevolution.entity.minecarts.DamageCausingMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.FluidMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MagnetMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.functioning.MobHeadMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PressurePlateMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.WeightPressurePlateMinecartEntity;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface MinecartTransformConfig {

  /**
   * Creates the appropriate minecart entity based on this config.
   *
   * @param world The level the minecart is in
   * @param pos The position vector of the minecart
   * @return The newly spawned minecart entity
   */
  AbstractMinecart createMinecart(Level world, Vec3 pos);

  /** Convenience method to act as a BiFunction. */
  default BiFunction<Level, Vec3, AbstractMinecart> asBiFunction() {
    return this::createMinecart;
  }

  /** Creates a config from an existing BiFunction. */
  static MinecartTransformConfig of(BiFunction<Level, Vec3, AbstractMinecart> factory) {
    return factory::apply;
  }

  static MinecartTransformConfig damageCausing(
      Supplier<EntityType<DamageCausingMinecartEntity>> entityType,
      float damageAmount,
      MinecartWithBlockItem correspondingItem,
      ResourceKey<DamageType> damageType) {
    return (w, pos) ->
        new DamageCausingMinecartEntity(
            entityType.get(), w, pos.x, pos.y, pos.z, damageAmount, correspondingItem, damageType);
  }

  static MinecartTransformConfig shulker(
      Supplier<EntityType<ShulkerMinecartEntity>> entityType, Block shulkerBoxBlock) {
    return (w, pos) ->
        new ShulkerMinecartEntity(
            entityType.get(),
            w,
            pos.x,
            pos.y,
            pos.z,
            (net.minecraft.world.level.block.ShulkerBoxBlock) shulkerBoxBlock);
  }

  static MinecartTransformConfig fluid(
      Supplier<EntityType<FluidMinecartEntity>> entityType, Item fluidBucketItem) {
    return (w, pos) ->
        new FluidMinecartEntity(entityType.get(), w, pos.x, pos.y, pos.z, fluidBucketItem);
  }

  static MinecartTransformConfig variant(
      Supplier<EntityType<VariantBlockMinecartEntity>> entityType, Item item) {
    return (w, pos) ->
        new VariantBlockMinecartEntity(entityType.get(), w, pos.x, pos.y, pos.z, item);
  }

  static MinecartTransformConfig magnet(
      Supplier<EntityType<MagnetMinecartEntity>> entityType, MinecartWithBlockItem item) {
    return (w, pos) -> new MagnetMinecartEntity(entityType.get(), w, pos.x, pos.y, pos.z, item);
  }

  static MinecartTransformConfig mobHead(
      Supplier<EntityType<MobHeadMinecartEntity>> entityType, MinecartWithBlockItem item) {
    return (w, pos) -> new MobHeadMinecartEntity(entityType.get(), w, pos.x, pos.y, pos.z, item);
  }

  static MinecartTransformConfig pressurePlate(
      Supplier<EntityType<PressurePlateMinecartEntity>> entityType, Item item) {
    return (w, pos) ->
        new PressurePlateMinecartEntity(entityType.get(), w, pos.x, pos.y, pos.z, item);
  }

  static MinecartTransformConfig weightPressurePlate(
      Supplier<EntityType<WeightPressurePlateMinecartEntity>> entityType,
      MinecartWithBlockItem item) {
    return (w, pos) ->
        new WeightPressurePlateMinecartEntity(entityType.get(), w, pos.x, pos.y, pos.z, item);
  }
}
