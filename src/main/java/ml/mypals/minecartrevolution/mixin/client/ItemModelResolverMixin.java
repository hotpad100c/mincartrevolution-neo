package ml.mypals.minecartrevolution.mixin.client;

import ml.mypals.minecartrevolution.interfaces.IMinecartWithBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ItemOwner;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Final;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {

    @Shadow
    @Final
    private ModelManager modelManager;

    @Shadow
    private ClientItem.Properties getItemProperties(Identifier modelId) {
        throw new AssertionError();
    }

    @Shadow
    private ItemModel getItemModel(Identifier modelId) {
        throw new AssertionError();
    }

    @Inject(
            method = "appendItemLayers",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAppendItemLayers(
            ItemStackRenderState output,
            ItemStack item,
            ItemDisplayContext displayContext,
            @Nullable Level level,
            @Nullable ItemOwner owner,
            int seed,
            CallbackInfo ci
    ) {
        if (this.mincartrevolution$isMinecartWithBlock(item)) {
            ci.cancel();
            BlockState blockInside = this.mincartrevolution$getBlockFromMinecart(item);

            // Append block state as model identity element so the GUI renderer
            // distinguishes between stacks with different blocks inside
            output.appendModelIdentityElement(blockInside);

            if (!blockInside.isAir()) {
                this.mincartrevolution$renderMinecartWithBlock(output, item, displayContext, level, owner, seed, blockInside);
            } else {
                this.mincartrevolution$renderDefault(output, item, displayContext, level, owner, seed);
            }
        }
    }

    @Unique
    private boolean mincartrevolution$isMinecartWithBlock(ItemStack item) {
        return item.getItem() instanceof IMinecartWithBlockItem;
    }

    @Unique
    private BlockState mincartrevolution$getBlockFromMinecart(ItemStack item) {
        if (item.getItem() instanceof IMinecartWithBlockItem provider) {
            return provider.getBlockInside(item);
        }
        return null;
    }

    @Unique
    private void mincartrevolution$renderMinecartWithBlock(
            ItemStackRenderState output,
            ItemStack item,
            ItemDisplayContext displayContext,
            @Nullable Level level,
            @Nullable ItemOwner owner,
            int seed,
            BlockState blockInside
    ) {
        ClientLevel clientLevel = level instanceof ClientLevel cl ? cl : null;

        ItemStack minecartStack = new ItemStack(Items.MINECART);
        Identifier minecartModelId = minecartStack.get(DataComponents.ITEM_MODEL);
        output.setOversizedInGui(this.getItemProperties(minecartModelId).oversizedInGui());
        this.getItemModel(minecartModelId).update(
                output,
                item,
                (ItemModelResolver)(Object)this,
                displayContext,
                clientLevel,
                owner,
                seed
        );


        ItemStackRenderState tempBlockState = new ItemStackRenderState();
        tempBlockState.displayContext = displayContext;

        if (blockInside.is(BlockTags.SHULKER_BOXES) && blockInside.hasProperty(net.minecraft.world.level.block.ShulkerBoxBlock.FACING)) {
            blockInside = blockInside.setValue(net.minecraft.world.level.block.ShulkerBoxBlock.FACING, Direction.DOWN);
        }
        ItemStack blockStack = new ItemStack(blockInside.getBlock());
        Identifier blockModelId = blockStack.get(DataComponents.ITEM_MODEL);

        if (blockModelId != null) {
            this.getItemModel(blockModelId).update(
                    tempBlockState,
                    blockStack,
                    (ItemModelResolver)(Object)this,
                    displayContext,
                    clientLevel,
                    owner,
                    0
            );

            this.mincartrevolution$copyLayersWithTransform(output, tempBlockState, blockInside);
        }
    }

    @Unique
    private void mincartrevolution$renderDefault(
            ItemStackRenderState output,
            ItemStack item,
            ItemDisplayContext displayContext,
            @Nullable Level level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        Identifier modelId = item.get(DataComponents.ITEM_MODEL);
        if (modelId != null) {
            output.setOversizedInGui(this.getItemProperties(modelId).oversizedInGui());
            this.getItemModel(modelId).update(
                    output,
                    item,
                    (ItemModelResolver)(Object)this,
                    displayContext,
                    level instanceof ClientLevel clientLevel ? clientLevel : null,
                    owner,
                    seed
            );
        }
    }@Unique
    private void mincartrevolution$copyLayersWithTransform(
            ItemStackRenderState output,
            ItemStackRenderState source,
            BlockState blockInside
    ) {
        BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(
                blockModelRenderState,
                blockInside,
                BlockDisplayContext.create()
        );

        ItemStackRenderState.LayerRenderState reference = output.layers[0];
        ItemTransform referenceTransform = reference.itemTransform;

        List<BlockStateModelPart> bakedQuads = blockModelRenderState.modelParts;
        List<BakedQuad> quads = bakedQuads.stream()
                .flatMap(bakedQuad -> {
                    List<BakedQuad> qds = new ArrayList<>();
                    for (Direction direction : Direction.values()) {
                        qds.addAll(bakedQuad.getQuads(direction));
                    }
                    qds.addAll(bakedQuad.getQuads(null));
                    return qds.stream();
                })
                .toList();
        for (ItemStackRenderState.LayerRenderState sourceLayer : source.layers) {
            ItemStackRenderState.LayerRenderState targetLayer = output.newLayer();

            targetLayer.setFoilType(sourceLayer.foilType);
            targetLayer.setLocalTransform(reference.localTransform);

            if (sourceLayer.specialRenderer != null) {
                // Special renderers (e.g. ShulkerBoxSpecialRenderer) draw in block
                // [0,1] coordinate space and apply their own internal transform
                // (see ShulkerBoxRenderer.createModelTransform which does
                //  translate(0.5,0.5,0.5) + scale(1,-1,-1) + translate(0,-1,0)).
                // We must NOT redirect their translation — just scale them to fit
                // inside the cart and keep the reference rotation.
                Vector3f scale = new Vector3f();
                referenceTransform.scale().mul(0.5f, scale);

                // The reference translation already positions the layer correctly
                // relative to the minecart body; specialRenderer will handle its
                // own internal centering.
                targetLayer.setItemTransform(new ItemTransform(
                        referenceTransform.rotation(),
                        referenceTransform.translation(),
                        scale
                ));
                targetLayer.setupSpecialModel(sourceLayer.specialRenderer, sourceLayer.argumentForSpecialRendering);
            } else {
                Vector3f pos = new Vector3f();
                sourceLayer.itemTransform.translation().add(0f, 0.04f, 0f, pos);

                Vector3f scale = new Vector3f();
                referenceTransform.scale().mul(0.8f, scale);

                targetLayer.setItemTransform(new ItemTransform(
                        referenceTransform.rotation(),
                        pos,
                        scale
                ));
                targetLayer.tintLayers().addAll(sourceLayer.tintLayers());
                targetLayer.prepareQuadList().addAll(quads.isEmpty() ? sourceLayer.prepareQuadList() : quads);
                targetLayer.setExtents(sourceLayer.extents);
            }
        }

        if (source.isAnimated()) {
            output.setAnimated();
        }
    }
}
