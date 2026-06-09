package ml.mypals.minecartrevolution.events;

import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.item.WrenchItem;
import ml.mypals.minecartrevolution.manager.MinecartChainManager;
import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static ml.mypals.minecartrevolution.entity.minecarts.maps.WoolEntityMapper.byColor;
import static net.minecraft.world.item.Items.IRON_CHAIN;

@EventBusSubscriber(modid = "minecartrevolution")
public class MinecartInteractionEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handleInteractionWithMinecart(PlayerInteractEvent.EntityInteract event) {
        Level world = event.getLevel();
        Entity interacted = event.getTarget();
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        if (!(interacted instanceof AbstractMinecart))
            return;
        if (!player.isShiftKeyDown()) {
            if (!held.getItem().equals(IRON_CHAIN)) {
                // regular minecart riding is handled by vanilla
            } else {
                if (!(interacted instanceof AbstractMinecart minecart))
                    return;
                if (!world.isClientSide()) {
                    handleIronChain(player, minecart, held, (ServerLevel) world);
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        } else {
            interact(player, event.getHand(), (AbstractMinecart) interacted, world);
        }
    }

    public static void interact(Player player, @NotNull InteractionHand hand, AbstractMinecart interacted,
            Level world) {

        ItemStack stackInHand = player.getItemInHand(hand);

        if (player.isSecondaryUseActive()) {
            if (!stackInHand.isEmpty()) {
                if (stackInHand.getItem() instanceof BlockItem blockItem && interacted.getDisplayBlockState().isAir()) {
                    interacted.setCustomDisplayBlockState(
                            Optional.of(blockItem.getBlock().defaultBlockState()));

                    player.swing(hand);
                    interacted.playSound(blockItem.getBlock().defaultBlockState()
                            .getSoundType(world, interacted.getOnPos(), player).getPlaceSound(), 1, 1);

                    if (!world.isClientSide()) {
                        MinecartTransformManager.checkForTransform(world, interacted.position(), blockItem, interacted,
                                stackInHand);
                        stackInHand.consume(1, player);
                    }

                    if (player instanceof ServerPlayer serverPlayerEntity) {
                        MRModCriteria.BLOCK_CART_CRAFTED.get().trigger(serverPlayerEntity, interacted);
                        boolean flag = false;
                        for (DyeColor color : DyeColor.values()) {
                            Block block = byColor(color);
                            flag |= blockItem.getBlock().equals(block);
                        }
                        if (flag) {
                            MRModCriteria.NO_GRAVITY.get().trigger(serverPlayerEntity);
                        }
                    }
                    return;
                } else if (stackInHand.getItem() instanceof BucketItem bucketItem
                        && bucketItem.getContent() != Fluids.EMPTY
                        && interacted.getDisplayBlockState().isAir()) {
                    Fluid fluid = bucketItem.getContent();
                    if (!world.isClientSide()) {
                        MinecartTransformManager.checkForTransform(world, interacted.position(), stackInHand.getItem(),
                                interacted, stackInHand);
                        stackInHand.shrink(1);
                        player.getInventory().add(new ItemStack(Items.BUCKET));
                    }
                    playBucketSound(fluid, world, interacted);
                }
            }
        }
    }

    private static void playBucketSound(Fluid fluid, Level world, Entity interacted) {
        if (fluid.defaultFluidState().is(FluidTags.LAVA)) {
            world.playSound(null, interacted.blockPosition(), SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0F,
                    1.0F);
        } else {
            world.playSound(null, interacted.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static void sendOverlayMessage(Player player, Component message) {
        if (player instanceof ServerPlayer sp) {
            sp.sendOverlayMessage(message);
        }
    }

    private static void handleIronChain(Player player, AbstractMinecart minecart, ItemStack held, ServerLevel level) {
        UUID selectedId = MinecartChainManager.getSelectedMinecart(player);
        boolean alreadyChained = MinecartChainManager.isMinecartChained(level, minecart);

        if (selectedId == null) {
            if (alreadyChained) {
                MinecartChainManager.breakChain(level, minecart);
                sendOverlayMessage(player,
                        Component.translatable("message.minecartrevolution.chain_broken"));
                if (!player.isCreative())
                    held.shrink(1);
                level.playSound(null, minecart.blockPosition(),
                        SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                MinecartChainManager.startSelection(player, minecart);
                sendOverlayMessage(player,
                        Component.translatable("message.minecartrevolution.chain_selected"));
                level.playSound(null, minecart.blockPosition(),
                        SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        } else {
            Entity selected = level.getEntity(selectedId);
            if (!(selected instanceof AbstractMinecart selectedCart) || !selectedCart.isAlive()
                    || selectedCart.equals(minecart)) {
                MinecartChainManager.clearSelection(player);
                return;
            }

            boolean selectedChained = MinecartChainManager.isMinecartChained(level, selectedCart);
            if (alreadyChained || selectedChained) {
                sendOverlayMessage(player,
                        Component.translatable("message.minecartrevolution.chain_already_chained"));
                MinecartChainManager.clearSelection(player);
                return;
            }

            if (MinecartChainManager.createChain(level, selectedCart, minecart)) {
                MinecartChainManager.clearSelection(player);
                sendOverlayMessage(player,
                        Component.translatable("message.minecartrevolution.chain_linked"));
                if (!player.isCreative())
                    held.shrink(1);
                level.playSound(null, minecart.blockPosition(),
                        SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                MinecartChainManager.clearSelection(player);
            }
        }
    }
}
