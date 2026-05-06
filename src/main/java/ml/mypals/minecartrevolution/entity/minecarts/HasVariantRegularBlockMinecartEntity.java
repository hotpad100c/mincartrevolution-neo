package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.client.light.DynamicLightsSpread;
import ml.mypals.minecartrevolution.client.light.DynamicLightsStorage;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static ml.mypals.minecartrevolution.client.light.DynamicLightsSpread.clearFromCenter;

public class HasVariantRegularBlockMinecartEntity extends AbstractMinecart {
    public boolean activated = false;
    public HasVariantRegularBlockMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public HasVariantRegularBlockMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Block blockInside) {
        super(minecart, world, x, y, z);
        this.setCustomDisplayBlockState(Optional.of(blockInside.defaultBlockState()));
    }

    /*
    @Override
    public Type getMinecartType() {
        return Type.RIDEABLE;
    }*/

    @Override
    public void destroy(@NonNull ServerLevel serverLevel, DamageSource source) {
        boolean sourceIsPlayer = false;
        Player playerEntity = null;
        if (source.getEntity() instanceof Player player) {
            sourceIsPlayer = true;
            playerEntity = player;
        }
        boolean shouldDrop = serverLevel.getGameRules().get(GameRules.ENTITY_DROPS) ||
                (sourceIsPlayer && !((Player) source.getEntity()).isCreative());
        if (shouldDrop) {
            if (playerEntity != null) {
                if (playerEntity.isSecondaryUseActive()) {
                    ItemStack stack = getPickResult();
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
    public void remove(Entity.@NonNull RemovalReason reason) {
        removeDynamicLight(this.getOnPos().asLong(), true);
        this.setRemoved(reason);
    }

    private void clear() {
        setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
        Minecart minecartEntity = new Minecart(EntityType.MINECART, level());

        minecartEntity.restoreFrom(this);
        minecartEntity.copyPosition(this);
        minecartEntity.setDeltaMovement(this.getDeltaMovement());
        ;
        this.remove(RemovalReason.DISCARDED);
        this.level().addFreshEntity(minecartEntity);
        minecartEntity.setHurtDir(-minecartEntity.getHurtDir());
        minecartEntity.setHurtTime(10);
        minecartEntity.setDamage(50.0F);

    }

    public ItemStack addDataToStack(ItemStack stack) {
        return stack;
    }

    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        if (player.isSecondaryUseActive()) {
            if (this.hasCustomDisplay()) {
                BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

                if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    Block block = blockState.getBlock();
                    playSound(block.defaultBlockState().getSoundType().getBreakSound(), 1, 1);
                    player.swing(hand);
                    if (!this.level().isClientSide()) {
                        clear();
                        ItemStack stack = block.asItem().getDefaultInstance();
                        player.setItemInHand(hand, addDataToStack(stack));
                    }
                }
                return InteractionResult.SUCCESS;
            } else if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem blockItem) {
                setCustomDisplayBlockState(Optional.of(blockItem.getBlock().defaultBlockState()));
                player.swing(hand);
                playSound(blockItem.getBlock().defaultBlockState().getSoundType().getPlaceSound(), 1, 1);
                if (!this.level().isClientSide()) {
                    MinecartTransformManager.checkForTransform(level(), this.position(), blockItem.getBlock(), this, player.getItemInHand(InteractionHand.MAIN_HAND));
                    player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        } else {
            player.swing(hand);
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
    }

    @Override
    public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {
        super.activateMinecart(level, x, y, z, powered);
        handleActive( level, x, y, z, powered);
    }

    @Override
    public @NonNull Vec3 getPassengerRidingPosition(@NonNull Entity passenger) {
        if (this.hasCustomDisplay()) {
            BlockState myBlock = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
            double y = myBlock.getCollisionShape(level(), this.blockPosition()).isEmpty() ?
                    0 : myBlock.getCollisionShape(level(), this.blockPosition()).bounds().getMaxPosition().y * 0.9;
            return super.getPassengerRidingPosition(passenger).add(0, y + ((double) this.getDisplayOffset() / 40), 0);
        } else {
            return super.getPassengerRidingPosition(passenger);
        }
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.BLOCK_MINECART_ITEM.item().get();
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        MinecartWithBlockItem item = ((MinecartWithBlockItem)getDropItem());
        ItemStack stack = getDropItem().getDefaultInstance();
        CompoundTag nbt = new CompoundTag();
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        nbt.putInt("block_in_minecart", Block.getId(blockState));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        return stack;
    }

    public boolean hasCustomDisplay() {
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        return !(blockState.getBlock() instanceof AirBlock);
    }

    public void handleActive( ServerLevel level, int x, int y, int z, boolean powered) {
        BlockState blockState = this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        this.activated = powered;
        if (blockState.hasProperty(BlockStateProperties.POWERED)) {
            blockState = blockState.setValue(BlockStateProperties.POWERED, powered);
        }
        if (blockState.hasProperty(RedstoneLampBlock.LIT)) {
            blockState = blockState.setValue(RedstoneLampBlock.LIT, powered);
            if (!powered) this.removeDynamicLight(this.blockPosition().asLong(), true);

        }
        this.setCustomDisplayBlockState(Optional.of(blockState));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            tickDynamicLight((ClientLevel) this.level());
        }
        if(activated && level() instanceof ServerLevel serverLevel &&
                !(this.level().getBlockState(BlockPos.containing(this.position())).getBlock()
                        instanceof PoweredRailBlock poweredRailBlock
                        && poweredRailBlock.isActivatorRail())
        ){
            activateMinecart(serverLevel,this.blockPosition().getX(),this.blockPosition().getY(),this.blockPosition().getZ(),false);
        }
    }

    private void tickDynamicLight(ClientLevel world) {

        BlockState displayBlock = this.entityData
                .get(DATA_ID_CUSTOM_DISPLAY_BLOCK)
                .orElse(Blocks.AIR.defaultBlockState());

        int lightLevel = displayBlock.getLightEmission();

        BlockPos blockPos = this.blockPosition();
        long posLong = blockPos.asLong();
        Vec3 entityPos = this.position();

        BlockPos oldPos = BlockPos.containing(this.oldPosition());
        boolean moved = !oldPos.equals(blockPos);

        if (moved) {
            removeDynamicLight(oldPos.asLong(), true);

            if (lightLevel > 0) {
                DynamicLightsStorage.BP_TO_LIGHT_LEVEL.put(posLong, (double) lightLevel);

                DynamicLightsSpread.computeDynamicLights(
                        posLong,
                        entityPos.x, entityPos.y, entityPos.z,
                        lightLevel,
                        DynamicLightsStorage.BP_TO_LIGHT_LEVEL::containsKey,
                        pos -> {
                            BlockPos bp = BlockPos.of(pos);
                            setBlockDirty(bp);
                        }
                );
                updateDynamicLight(world, blockPos);
            }
        } else if (lightLevel > 0 && DynamicLightsStorage.getLightLevel(blockPos) == 0.0) {
            DynamicLightsStorage.BP_TO_LIGHT_LEVEL.put(posLong, (double) lightLevel);

            DynamicLightsSpread.computeDynamicLights(
                    posLong,
                    entityPos.x, entityPos.y, entityPos.z,
                    lightLevel,
                    DynamicLightsStorage.BP_TO_LIGHT_LEVEL::containsKey,
                    pos -> {
                        BlockPos bp = BlockPos.of(pos);
                        setBlockDirty(bp);
                    }
            );

            updateDynamicLight(world, blockPos);
        }
    }

    private void removeDynamicLight(long posLong, boolean update) {

        DynamicLightsStorage.BP_TO_LIGHT_LEVEL.remove(posLong);
        DynamicLightsSpread.computeLightsOff(
                posLong,
                DynamicLightsStorage.BP_TO_LIGHT_LEVEL::containsKey,
                bp -> {
                    if (update) {
                        BlockPos blockPos = BlockPos.of(bp);
                        setBlockDirty(blockPos);
                    }
                }
        );
    }

    private void setBlockDirty(BlockPos pos) {
        for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
            for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
                for (int y = pos.getY() - 1; y <= pos.getY() + 1; y++) {
                    Minecraft.getInstance().levelRenderer.setSectionDirty(
                            SectionPos.blockToSectionCoord(x),
                            SectionPos.blockToSectionCoord(y),
                            SectionPos.blockToSectionCoord(z)
                    );
                }
            }
        }
    }

    private void updateDynamicLight(ClientLevel world, BlockPos pos) {


        int vanillaLight = world.getBlockState(pos).getLightEmission(world, pos);
        double dynamicLight = DynamicLightsStorage.getLightLevel(pos);

        if (dynamicLight >= vanillaLight) {
            world.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }
}
