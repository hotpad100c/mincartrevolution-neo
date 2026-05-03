package ml.mypals.minecartrevolution.mixin;

import ml.mypals.minecartrevolution.advancements.criterion.MRModCriteria;
import ml.mypals.minecartrevolution.entity.minecarts.MinecartTransformManager;
import ml.mypals.minecartrevolution.item.MRModItems;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Minecart.class)
public abstract class MinecartMixin extends AbstractMinecart  {



	protected MinecartMixin(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	@Unique
	private void clear(){
		setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
	}

	@Unique
	public Item mincartrevolution_neo$asBlockMinecartItem() {
		MinecartWithBlockItem item = (MinecartWithBlockItem) MRModItems.BLOCK_MINECART.getDefaultInstance().getItem();
		return item;
	}

	@Unique
	private boolean mincartrevolution_neo$hasBlock(){
		return !this.getDisplayBlockState().isEmpty() &&
				this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).isPresent() &&
				!(this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).get().getBlock() instanceof AirBlock);
	}
	@Override
	public @NotNull ItemStack getPickResult(){
		ItemStack stack = mincartrevolution_neo$hasBlock()  ? MRModItems.BLOCK_MINECART.getDefaultInstance() : Items.MINECART.getDefaultInstance();
		CompoundTag nbt = new CompoundTag();
		int stateId = Block.getId(this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElseGet(Blocks.AIR::defaultBlockState));
		nbt.putInt("block_in_minecart", stateId);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
		return stack;
	}
	

	@Inject(at = @At("HEAD"),
			method="Lnet/minecraft/world/entity/vehicle/minecart/Minecart;" +
					"interact(Lnet/minecraft/world/entity/player/Player;" +
					"Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;" +
					")Lnet/minecraft/world/InteractionResult;", cancellable = true)
	public void interact(Player player, @NotNull InteractionHand hand, @NotNull Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
		if (player.isSecondaryUseActive()){
			if (!mincartrevolution_neo$hasBlock()){
				if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
					Block block = this.entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).get().getBlock();
					playSound(block.defaultBlockState().getSoundType(this.level(),getOnPos(),player).getBreakSound(), 1, 1);
					player.swing(hand);
					if(!this.level().isClientSide()){
						clear();
						player.setItemInHand(hand, block.asItem().getDefaultInstance());
					}
				}
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
			}
			else if(!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem blockItem) {
				setCustomDisplayBlockState(Optional.of(blockItem.getBlock().defaultBlockState()));
				player.swing(hand);
				playSound(blockItem.getBlock().defaultBlockState().getSoundType(this.level(),getOnPos(),player).getPlaceSound(), 1, 1);
				AbstractMinecart abstractMinecartEntity = MinecartTransformManager.checkForTransform(level(), this.position(), blockItem.getBlock(), this,player.getItemInHand(InteractionHand.MAIN_HAND));
				if(!this.level().isClientSide()) {
					player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
				}
				if(player instanceof ServerPlayer serverPlayerEntity)
					MRModCriteria.BLOCK_CART_CRAFTED.trigger(serverPlayerEntity, abstractMinecartEntity);
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
			}else {
				cir.setReturnValue(InteractionResult.PASS);
				cir.cancel();
			}
		}
		if(mincartrevolution_neo$hasBlock()){
			cir.setReturnValue(InteractionResult.PASS);
			cir.cancel();
		}
	}
}