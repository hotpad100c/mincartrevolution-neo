package ml.mypals.minecartrevolution.mixin.minecart;

import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.FluidMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Minecart.class)
public abstract class MinecartMixin extends AbstractMinecart {


    protected MinecartMixin(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    private void minecartrevolution_neo$clear() {
        setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
    }

    @Unique
    public Item minecartrevolution_neo$asBlockMinecartItem() {
        return (MinecartWithBlockItem) MRMinecarts.BLOCK_MINECART_ITEM.item().get().getDefaultInstance().getItem();
    }

    @Unique
    private boolean minecartrevolution_neo$hasBlock() {
        return !this.getDisplayBlockState().isEmpty() &&
                this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).isPresent() &&
                !(this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).get().getBlock() instanceof AirBlock);
    }

    @Inject(at = @At("RETURN"),
            method = "getPickResult", cancellable = true)
    public void getPickResult(CallbackInfoReturnable<ItemStack> cir) {
        if (!minecartrevolution_neo$hasBlock()) return;
        ItemStack stack = MRMinecarts.BLOCK_MINECART_ITEM.item().get().getDefaultInstance();
        CompoundTag nbt = new CompoundTag();
        int stateId = Block.getId(this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElseGet(Blocks.AIR::defaultBlockState));
        nbt.putInt("block_in_minecart", stateId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        cir.setReturnValue(stack);
    }


    @Inject(
            at = @At("HEAD"),
            method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;",
            cancellable = true
    )
    public void interact(Player player, @NotNull InteractionHand hand, @NotNull Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {

        ItemStack stackInHand = player.getItemInHand(hand);

        if (player.isSecondaryUseActive()) {
            if (!stackInHand.isEmpty()) {
                if (stackInHand.getItem() instanceof BlockItem blockItem) {
                    setCustomDisplayBlockState(
                            Optional.of(blockItem.getBlock().defaultBlockState())
                    );

                    player.swing(hand);
                    playSound(blockItem.getBlock().defaultBlockState().getSoundType(this.level(), getOnPos(), player).getPlaceSound(), 1, 1);

                    if (!this.level().isClientSide()) {
                        MinecartTransformManager.checkForTransform(level(), this.position(), blockItem, this, stackInHand);
                        stackInHand.consume(1, player);
                    }

                    if (player instanceof ServerPlayer serverPlayerEntity) {
                        MRModCriteria.BLOCK_CART_CRAFTED.get().trigger(serverPlayerEntity, this);
                    }

                    cir.setReturnValue(InteractionResult.SUCCESS);
                    return;
                } else if (stackInHand.is(Items.WATER_BUCKET) || stackInHand.is(Items.LAVA_BUCKET)) {
                    if (!this.level().isClientSide()) {
                        MinecartTransformManager.checkForTransform(level(), this.position(), stackInHand.getItem(), this, stackInHand);
                        stackInHand.split(1);
                        player.getInventory().add(new ItemStack(Items.BUCKET));
                    }
                    playBucketSound(Blocks.WATER);

                }
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            } else {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
        }
        if (minecartrevolution_neo$hasBlock()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
    @Unique
    private void playBucketSound(Block block) {
        if (block == Blocks.LAVA) {
            level().playSound(null, blockPosition(), SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level().playSound(null, blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}