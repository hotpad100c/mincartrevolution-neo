package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.item.MRModItems;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
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
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

public class HasVariantRegularBlockMinecartEntity extends AbstractMinecart {
    protected HasVariantRegularBlockMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
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
        if(source.getEntity() instanceof Player player){
            sourceIsPlayer = true;
            playerEntity = player;
        }
        boolean shouldDrop = serverLevel.getGameRules().get(GameRules.ENTITY_DROPS) ||
                (sourceIsPlayer  && !((Player)source.getEntity()).isCreative());
        if(shouldDrop) {
            if (playerEntity != null){
                if(playerEntity.isSecondaryUseActive()){
                    ItemStack stack = getPickResult();
                    spawnAtLocation(serverLevel, stack);
                }else{
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
    private void clear(){
        setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
        Minecart minecartEntity = new Minecart(EntityType.MINECART, level());

        minecartEntity.restoreFrom(this);
        minecartEntity.copyPosition(this);
        minecartEntity.setDeltaMovement(this.getDeltaMovement());;
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
        if (player.isSecondaryUseActive()){
            if (this.hasCustomDisplay()){
                BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

                if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
                    Block block = blockState.getBlock();
                    playSound(block.defaultBlockState().getSoundType().getBreakSound(), 1, 1);
                    player.swing(hand);
                    if(!this.level().isClientSide()){
                        clear();
                        ItemStack stack = block.asItem().getDefaultInstance();
                        player.setItemInHand(hand, addDataToStack(stack));
                    }
                }
                return InteractionResult.SUCCESS;
            }
            else if(!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem blockItem) {
                setCustomDisplayBlockState(Optional.of(blockItem.getBlock().defaultBlockState()));
                player.swing(hand);
                playSound(blockItem.getBlock().defaultBlockState().getSoundType().getPlaceSound(), 1, 1);
                if(!this.level().isClientSide()) {
                    MinecartTransformManager.checkForTransform(level(), this.position(), blockItem.getBlock(), this, player.getItemInHand(InteractionHand.MAIN_HAND));
                    player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
                }
                return InteractionResult.SUCCESS;
            }else {
                return InteractionResult.PASS;
            }
        }else{
            player.swing(hand);
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
    }
    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        if (this.hasCustomDisplay()) {
            BlockState myBlock = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
            double y = myBlock.getCollisionShape(level(), this.blockPosition()).isEmpty() ?
                    0:myBlock.getCollisionShape(level(), this.blockPosition()).bounds().getMaxPosition().y*0.9;
            return super.getPassengerRidingPosition(passenger).add(0,y+((double) this.getDisplayOffset() / 40),0);
        }else {
            return super.getPassengerRidingPosition(passenger);
        }
    }
    @Override
    public Item getDropItem() {
        MinecartWithBlockItem item = (MinecartWithBlockItem)MRModItems.BLOCK_MINECART;
        return item;
    }
    @Override
    public ItemStack getPickResult(){
        ItemStack stack = getDropItem().getDefaultInstance();
        CompoundTag nbt = new CompoundTag();
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        
        nbt.putInt("block_in_minecart", Block.getId(blockState));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        String blockName = blockState.getBlock().getName().getString();

        String cartName = Items.MINECART.getDescriptionId();

        stack.set(DataComponents.ITEM_NAME, Component.nullToEmpty(String.format(
                stack.getHoverName().getString(),blockName,cartName)));
        return stack;
    }
    @Unique
    public boolean hasCustomDisplay() {
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        return blockState.getBlock() instanceof AirBlock;
    }

}
