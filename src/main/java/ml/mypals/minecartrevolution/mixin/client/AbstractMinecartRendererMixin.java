package ml.mypals.minecartrevolution.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.minecartrevolution.entity.minecarts.container.CopperChestMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.ShulkerMinecartEntity;
import ml.mypals.minecartrevolution.entity.minecarts.container.TrappedChestMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.IMinecartChestExtension;
import ml.mypals.minecartrevolution.interfaces.IMinecartRenderStateExtension;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartRenderer.class)
public class AbstractMinecartRendererMixin {

    @Unique
    private MultiblockChestResources<ChestModel> minecartrevolution$chestModels = null;
    @Unique
    private SpriteGetter minecartrevolution$sprites = null;


    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(EntityRendererProvider.Context context, ModelLayerLocation model, CallbackInfo ci) {

        this.minecartrevolution$chestModels = ChestRenderer.LAYERS.map(layer ->
                new ChestModel(context.bakeLayer(layer))
        );

        this.minecartrevolution$sprites = context.getSprites();
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;F)V", at = @At(value = "RETURN"))
    private void extractRenderState(AbstractMinecart entity, MinecartRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity.getDisplayBlockState().is(Blocks.ENCHANTING_TABLE)
                || entity.getDisplayBlockState().getBlock() instanceof AbstractChestBlock<?>) {
            ((BlockModelRenderStateAccessor) state.displayBlockModel).setSpecialRenderer(null);
        }
        if (entity instanceof MinecartChest minecartChest) {
            IMinecartRenderStateExtension stateExt = (IMinecartRenderStateExtension) state;
            stateExt.minecartrevolution$setOpenness(((IMinecartChestExtension) minecartChest)
                    .minecartrevolution$getChestLidController().getOpenness(partialTicks));
            stateExt.minecartrevolution$setDisplayBlock(entity.getDisplayBlockState());
        } else if (entity instanceof TrappedChestMinecartEntity minecartChest) {
            IMinecartRenderStateExtension stateExt = (IMinecartRenderStateExtension) state;
            stateExt.minecartrevolution$setOpenness(minecartChest.getOpenness(partialTicks));
            stateExt.minecartrevolution$setDisplayBlock(entity.getDisplayBlockState());
        } else if (entity instanceof CopperChestMinecartEntity minecartChest) {
            IMinecartRenderStateExtension stateExt = (IMinecartRenderStateExtension) state;
            stateExt.minecartrevolution$setOpenness(minecartChest.getOpenness(partialTicks));
            stateExt.minecartrevolution$setDisplayBlock(entity.getDisplayBlockState());
        } else if (entity instanceof ShulkerMinecartEntity shulker) {
            ((BlockModelRenderStateAccessor) state.displayBlockModel).setSpecialRenderer(null);
        }
    }

    @WrapMethod(method = "submitMinecartContents")
    private void submitMinecartContents(MinecartRenderState minecartRenderState, BlockModelRenderState blockModel, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, Operation<Void> original) {
        IMinecartRenderStateExtension state = (IMinecartRenderStateExtension) minecartRenderState;
        if (state.minecartrevolution$getDisplayBlock().getBlock() instanceof AbstractChestBlock) {
            poseStack.pushPose();
            poseStack.translate(0F, (float) (minecartRenderState.displayOffset - 8) / 16.0F, 1F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            float open = state.minecartrevolution$getOpenness();
            open = 1.0F - open;
            open = 1.0F - open * open * open;
            ChestModel model = this.minecartrevolution$chestModels.select(ChestType.SINGLE);

            SpriteId location = switch (state.minecartrevolution$getDisplayBlock().getBlock()) {
                case TrappedChestBlock ignored -> Sheets.CHEST_TRAPPED.single();
                case WeatheringCopperChestBlock ignored -> {
                    if(state.minecartrevolution$getDisplayBlock().is(Blocks.COPPER_CHEST)
                    || state.minecartrevolution$getDisplayBlock().is(Blocks.WAXED_COPPER_CHEST)){
                        yield Sheets.CHEST_COPPER_UNAFFECTED.single();
                    }else if(state.minecartrevolution$getDisplayBlock().is(Blocks.EXPOSED_COPPER_CHEST)
                    || state.minecartrevolution$getDisplayBlock().is(Blocks.WAXED_EXPOSED_COPPER_CHEST)){
                        yield Sheets.CHEST_COPPER_EXPOSED.single();
                    }else if(state.minecartrevolution$getDisplayBlock().is(Blocks.WEATHERED_COPPER_CHEST)
                    ||state.minecartrevolution$getDisplayBlock().is(Blocks.WAXED_WEATHERED_COPPER_CHEST)){
                        yield Sheets.CHEST_COPPER_WEATHERED.single();
                    }else{
                        yield Sheets.CHEST_COPPER_OXIDIZED.single();
                    }
                }
                case CopperChestBlock ignored -> Sheets.CHEST_COPPER_UNAFFECTED.single();
                case EnderChestBlock ignored -> Sheets.ENDER_CHEST_LOCATION;
                default -> Sheets.CHEST_REGULAR.single();
            };

            submitNodeCollector.submitModel(
                    model,
                    open,
                    poseStack,
                    lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    -1,
                    location,
                    this.minecartrevolution$sprites,
                    0,
                    null
            );
            poseStack.popPose();
        } else {
            original.call(minecartRenderState, blockModel, poseStack, submitNodeCollector, lightCoords);
        }
    }
}