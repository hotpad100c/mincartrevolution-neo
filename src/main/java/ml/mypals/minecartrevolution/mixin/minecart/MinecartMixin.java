package ml.mypals.minecartrevolution.mixin.minecart;

import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
}