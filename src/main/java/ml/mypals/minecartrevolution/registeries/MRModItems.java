package ml.mypals.minecartrevolution.registeries;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

import ml.mypals.minecartrevolution.item.WrenchItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MRModItems {

  public static final DeferredItem<WrenchItem> WRENCH =
      MRMinecarts.ITEMS.registerItem("wrench", props -> new WrenchItem(props.stacksTo(1)));

  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

  public static final ResourceKey<CreativeModeTab> MINECART_REVOLUTION_ITEM_GROUP_KEY =
      ResourceKey.create(
          BuiltInRegistries.CREATIVE_MODE_TAB.key(),
          Identifier.fromNamespaceAndPath(MODID, "item_group"));

  public static final DeferredHolder<CreativeModeTab, CreativeModeTab>
      MINECART_REVOLUTION_ITEM_GROUP =
          CREATIVE_MODE_TABS.register(
              "minecart_revolution",
              () ->
                  CreativeModeTab.builder()
                      .title(Component.translatable("itemGroup.minecartrevolution"))
                      .withTabsBefore(CreativeModeTabs.COMBAT)
                      .icon(() -> new ItemStack(Items.MINECART))
                      .displayItems(
                          (parameters, itemGroup) -> {
                            itemGroup.accept(WRENCH.get());
                            for (MRMinecarts.MinecartEntry<?, ?> entry : MRMinecarts.MINECARTS) {
                              itemGroup.accept(entry.item().get());
                            }
                            itemGroup.accept(Items.MINECART);
                            itemGroup.accept(Items.FURNACE_MINECART);
                            itemGroup.accept(Items.HOPPER_MINECART);
                            itemGroup.accept(Items.CHEST_MINECART);
                            itemGroup.accept(Items.TNT_MINECART);
                          })
                      .build());

  public static void registerDispenserBehaviors() {
    for (MRMinecarts.MinecartEntry<?, ?> entry : MRMinecarts.MINECARTS) {
      if (entry.dispenseBehavior() != null) {
        DispenserBlock.registerBehavior(entry.item().get(), entry.dispenseBehavior());
      }
    }
  }
}
