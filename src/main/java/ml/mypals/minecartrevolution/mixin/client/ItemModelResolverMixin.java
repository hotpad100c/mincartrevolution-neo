package ml.mypals.minecartrevolution.mixin.client;

import ml.mypals.minecartrevolution.interfaces.IMinecartWithBlockItem;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.resources.Identifier;
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

            if (blockInside != null && !blockInside.isAir()) {
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

            this.mincartrevolution$copyLayersWithTransform(output, tempBlockState);
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
    }
    @Unique
    private void mincartrevolution$copyLayersWithTransform(
            ItemStackRenderState output,
            ItemStackRenderState source

    ) {
        List<ItemStackRenderState.LayerRenderState> layers = Arrays.stream(source.layers).toList();
        ItemStackRenderState.LayerRenderState reference = output.layers[0];
        ItemTransform referenceTransform = reference.itemTransform;
        for (ItemStackRenderState.LayerRenderState sourceLayer : layers) {
            ItemStackRenderState.LayerRenderState targetLayer = output.newLayer();

            targetLayer.setFoilType(sourceLayer.foilType);
            targetLayer.setExtents(sourceLayer.extents);
            if(sourceLayer.specialRenderer != null){
                targetLayer.setupSpecialModel(sourceLayer.specialRenderer, sourceLayer.argumentForSpecialRendering);
            }
            targetLayer.setLocalTransform(reference.localTransform);

            ItemTransform itemTransform = sourceLayer.itemTransform;
            Vector3f pos = new Vector3f();
            itemTransform.translation().add(0f,0.04f,0f,pos);
            Vector3f scale = new Vector3f();
            referenceTransform.scale().mul(0.8f,scale);
            targetLayer.setItemTransform(new ItemTransform(
                    referenceTransform.rotation(),
                    pos,
                    scale
            ));

            targetLayer.tintLayers().addAll(sourceLayer.tintLayers());

            targetLayer.prepareQuadList().addAll(sourceLayer.prepareQuadList());
        }

        if (source.isAnimated()) {
            output.setAnimated();
        }
    }
}
