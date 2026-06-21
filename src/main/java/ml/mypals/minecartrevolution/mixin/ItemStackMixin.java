package ml.mypals.minecartrevolution.mixin;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder, ItemInstance {
  @Shadow
  public abstract DataComponentMap getComponents();

  /*@WrapMethod(
          method = "getItemName"
  )
  public Component getDisplayName(Operation<Component> original) {
      if(this.getComponents().has(DataComponents.CUSTOM_DATA))
      {
          var customData = this.getComponents().get(DataComponents.CUSTOM_DATA);
          if(customData != null){
              CompoundTag compoundTag = customData.copyTag();
              if(compoundTag.contains("block_in_minecart")){
                  String blockName = Block.stateById(compoundTag.getIntOr("block_in_minecart", 1)).getBlock().getName().toString();
                  String cartName = Items.MINECART.getName(Items.MINECART.getDefaultInstance()).getString();
                  return Component.nullToEmpty(String.format(original.call().getString(), blockName, cartName));
              }
          }

      }
      return original.call();
  }*/

}
