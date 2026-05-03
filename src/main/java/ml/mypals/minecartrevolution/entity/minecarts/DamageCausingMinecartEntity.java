package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DamageCausingMinecartEntity extends ItemBoundBlockMinecartEntity {

    private float damageAmount;
    private DamageSource damageSource;
    private ResourceKey<DamageType> damageType;
    public DamageCausingMinecartEntity(EntityType<? extends ItemBoundBlockMinecartEntity> entityType, Level world) {
        super(entityType, world);
        this.damageAmount = 0;
        this.damageSource = this.damageSources().source(DamageTypes.GENERIC);
        this.damageType = DamageTypes.GENERIC;
    }
    public void setDamageAmount(float damageAmount) {
        this.damageAmount = damageAmount;
    }

    public DamageCausingMinecartEntity(EntityType<? extends ItemBoundBlockMinecartEntity> minecart, Level world, double x, double y, double z, float damageAmount, MinecartWithBlockItem correspondingItem, ResourceKey<DamageType> damageType) {
        super(minecart,world, x, y, z, correspondingItem);
        this.damageAmount = damageAmount;
        this.damageSource = this.damageSources().source(damageType);
        this.damageType = damageType;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        entity.hurt(this.getDamageSource(), this.damageAmount);
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    @Override
    public void load(ValueInput nbt) {
        super.load(nbt);
        this.damageAmount = nbt.getFloatOr("damageAmount",1.0f);
        try {
            String damageTypeString = nbt.getStringOr("damageType", "generic");
            this.damageType = getDamageSource(damageTypeString);
            this.damageSource = createDamageSource(damageType);
        }catch(Exception ignored) {
        }

    }
    @Override
    public void saveWithoutId(ValueOutput nbt) {
        nbt.putString("damageType", getDamageType(this.damageType));
        nbt.putFloat("damageAmount", this.damageAmount);
        super.saveWithoutId(nbt);
    }
    public ResourceKey<DamageType> getDamageSource(String damageType) {
        switch (damageType) {
            case "cactus":
                return DamageTypes.CACTUS;
            case "hot_floor":
                return DamageTypes.HOT_FLOOR;
            case "campfire":
                return DamageTypes.CAMPFIRE;
            default:
                return DamageTypes.GENERIC;
        }
    }
    public DamageSource createDamageSource(ResourceKey<DamageType> damageType) {
        return this.damageSources().source(damageType);
    }
    public String getDamageType(ResourceKey<DamageType> damageType) {
        return switch (damageType.identifier().getPath()) {
            case "cactus" -> DamageTypes.CACTUS.identifier().getPath();
            case "hot_floor" -> DamageTypes.HOT_FLOOR.identifier().getPath();
            case "campfire" -> DamageTypes.CAMPFIRE.identifier().getPath();
            default -> DamageTypes.GENERIC.identifier().getPath();
        };

    }
}