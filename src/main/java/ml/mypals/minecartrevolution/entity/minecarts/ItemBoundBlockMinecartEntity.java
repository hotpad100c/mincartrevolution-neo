package ml.mypals.minecartrevolution.entity.minecarts;

import com.mojang.logging.LogUtils;
import ml.mypals.minecartrevolution.registeries.MRModItems;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.Optional;

public class ItemBoundBlockMinecartEntity extends HasVariantRegularBlockMinecartEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final EntityDataAccessor<String> CORRESPONDING_ITEM =
            SynchedEntityData.defineId(ItemBoundBlockMinecartEntity.class, EntityDataSerializers.STRING);
    private Item correspondingItem;

    public ItemBoundBlockMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
        this.correspondingItem = MRMinecarts.BLOCK_MINECART_ITEM.item().get();
    }

    public ItemBoundBlockMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, Item.byBlock(correspondingItem.getBlockInside()));
        this.getEntityData().set(CORRESPONDING_ITEM, BuiltInRegistries.ITEM.getKey(correspondingItem).toString());
        this.correspondingItem = correspondingItem;
    }

    public ItemBoundBlockMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world, MinecartWithBlockItem correspondingItem) {
        super(entityType, world);
        this.correspondingItem = correspondingItem;
        this.getEntityData().set(CORRESPONDING_ITEM, BuiltInRegistries.ITEM.getKey(correspondingItem).toString());
        this.setCustomDisplayBlockState(Optional.of(correspondingItem.getBlockInside().defaultBlockState()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CORRESPONDING_ITEM, BuiltInRegistries.ITEM.getKey(this.correspondingItem).toString());
    }

    @Override
    public void destroy(@NonNull ServerLevel serverLevel, DamageSource source) {
        boolean sourceIsPlayer = false;
        Player playerEntity = null;
        if (source.getEntity() instanceof Player player) {
            sourceIsPlayer = true;
            playerEntity = player;
        }
        boolean shouldDrop = !serverLevel.getGameRules().get(GameRules.ENTITY_DROPS) ||
                (sourceIsPlayer && !((Player) source.getEntity()).isCreative());
        if (shouldDrop) {
            if (playerEntity != null) {
                if (playerEntity.isSecondaryUseActive()) {
                    ItemStack stack = getDropItem().getDefaultInstance();
                    spawnAtLocation(serverLevel, stack);
                } else {
                    ItemStack stack = Items.MINECART.getDefaultInstance();
                    spawnAtLocation(serverLevel, stack);
                    BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
                    ItemStack stack2 = blockState.getBlock().asItem().getDefaultInstance();
                    Containers.dropItemStack(this.level(), this.getX(), this.getY(), this.getZ(), stack2);
                }
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
        this.kill(serverLevel);
    }

    @Override
    public @NonNull Item getDropItem() {
        if (this.correspondingItem != null && !(this.correspondingItem instanceof AirItem)) {
            return this.correspondingItem;
        } else {
            return this.getCorrospondingItem();
        }
    }

    public Item getCorrospondingItem() {
        ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER);
        TagValueOutput valueOutput = TagValueOutput.createWithContext(reporter, this.registryAccess());

        this.saveWithoutId(valueOutput);
        CompoundTag nbt = valueOutput.buildResult();
        this.correspondingItem = BuiltInRegistries.ITEM.get(
                        Identifier.parse(String.valueOf(nbt.getString("correspondingItem"))))
                .orElse(MRMinecarts.BLOCK_MINECART_ITEM.item().get().builtInRegistryHolder()).value();


        return this.correspondingItem;
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        ItemStack stack =
                this.correspondingItem != null ? correspondingItem.getDefaultInstance() : Items.MINECART.getDefaultInstance();
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
/*
        String blockName = blockState.getBlock().getName().getString();
        String cartName = Items.MINECART.getDefaultInstance().getDisplayName().getString();
        stack.set(DataComponents.ITEM_NAME, Component.nullToEmpty(String.format(stack.getHoverName().getString(),blockName,cartName)));
  */
        return stack;
    }


    public void load(@NonNull ValueInput nbt) {
        super.load(nbt);
        Optional<String> itemIdOpt = nbt.getString("correspondingItem");
        this.correspondingItem = itemIdOpt.map(s -> BuiltInRegistries.ITEM.get(
                        Identifier.parse(s))
                .orElse(Items.AIR.builtInRegistryHolder())
                .value()).orElse(Items.MINECART);
        this.getEntityData().set(CORRESPONDING_ITEM, BuiltInRegistries.ITEM.getKey(correspondingItem).toString());
    }

    @Override
    public void saveWithoutId(ValueOutput nbt) {
        nbt.putString("correspondingItem", BuiltInRegistries.ITEM.getKey(this.correspondingItem).toString());
        super.saveWithoutId(nbt);
    }

    public void setCorrespondingItem(Item correspondingItem) {
        this.correspondingItem = correspondingItem;
        this.entityData.set(CORRESPONDING_ITEM, BuiltInRegistries.ITEM.getKey(correspondingItem).toString());
    }
}
