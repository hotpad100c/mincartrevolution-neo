package ml.mypals.minecartrevolution.item;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.entity.minecarts.fluidcarts.FluidMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.IMinecartWithBlockItem;
import ml.mypals.minecartrevolution.registeries.MRModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class MinecartWithBlockItem extends Item implements IMinecartWithBlockItem {
    final Item corrospondingItem;
    Block blockInside;
    public static final DispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

        @Override
        public @NonNull ItemStack execute(BlockSource pointer, @NonNull ItemStack stack) {
            Direction direction = pointer.state().getValue(DispenserBlock.FACING);
            ServerLevel serverWorld = pointer.level();
            Vec3 vec3d = pointer.center();
            double d = vec3d.x() + direction.getStepX() * 1.125;
            double e = Math.floor(vec3d.y()) + direction.getStepY();
            double f = vec3d.z() + direction.getStepZ() * 1.125;
            BlockPos blockPos = pointer.pos().relative(direction);
            BlockState blockState = serverWorld.getBlockState(blockPos);
            RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
                    ? blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty())
                    : RailShape.NORTH_SOUTH;
            double g;
            if (blockState.is(BlockTags.RAILS)) {
                if (railShape.isSlope()) {
                    g = 0.6;
                } else {
                    g = 0.1;
                }
            } else {
                if (!blockState.isAir() || !serverWorld.getBlockState(blockPos.below()).is(BlockTags.RAILS)) {
                    return this.defaultBehavior.dispense(pointer, stack);
                }

                BlockState blockState2 = serverWorld.getBlockState(blockPos.below());
                RailShape railShape2 = blockState2.getBlock() instanceof BaseRailBlock
                        ? blockState2.getValue(((BaseRailBlock)blockState2.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railShape2.isSlope()) {
                    g = -0.4;
                } else {
                    g = -0.9;
                }
            }

            MinecartWithBlockItem minecartWithBlockItem = ((MinecartWithBlockItem) stack.getItem());

            AbstractMinecart abstractMinecartEntity = minecartWithBlockItem.getCart(serverWorld, d, e + g, f,
                    minecartWithBlockItem.type, minecartWithBlockItem, stack);
            serverWorld.addFreshEntity(abstractMinecartEntity);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(BlockSource pointer) {
            pointer.level().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, pointer.pos(), 0);
        }
    };

    public AbstractMinecart getCart(ServerLevel serverWorld, double x, double y, double z, AdvancedMinecartEntityTypes.Type type, MinecartWithBlockItem corrospondingItem, ItemStack stack) {
        AbstractMinecart minecart = MinecartTransformManager.getTransform(
                serverWorld, corrospondingItem, Item.byBlock(corrospondingItem.blockInside), new Vec3(x, y, z),
                type
        );
        BlockState blockState = corrospondingItem.getBlockInside(stack);
        if(blockState != null){
            minecart.setCustomDisplayBlockState(Optional.of(blockState));
        }
        return minecart;
    }

    final AdvancedMinecartEntityTypes.Type type;

    public MinecartWithBlockItem(AdvancedMinecartEntityTypes.Type type, Properties settings, Block blockInside) {
        super(settings);
        this.type = type;
        this.corrospondingItem = this;
        this.blockInside = blockInside;
    }

    public void setBlockInside(Block blockInside) {
        this.blockInside = blockInside;
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack itemStack) {

        String blockName = this.blockInside.getName().getString();
        if(itemStack.getComponents().has(DataComponents.CUSTOM_DATA))
        {
            var customData = itemStack.getComponents().get(DataComponents.CUSTOM_DATA);
            if(customData != null){
                CompoundTag compoundTag = customData.copyTag();

                if(compoundTag.contains("block_in_minecart")){
                    String key = Block.stateById(compoundTag.getIntOr("block_in_minecart", 1))
                            .getBlock().getDescriptionId();
                    blockName = Component.translatable(key).getString();
                }
            }
        }
        String cartName = Items.MINECART.getName(Items.MINECART.getDefaultInstance()).getString();

        String prompt = Component.translatable("item.minecartrevolution.minecart_with_block").getString();
        return Component.nullToEmpty(String.format(prompt, blockName, cartName));
    }

    public Block getBlockInside() {
        return this.blockInside;
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack itemStack = context.getItemInHand();
            if (world instanceof ServerLevel serverWorld) {
                RailShape railShape = blockState.getBlock() instanceof BaseRailBlock
                        ? blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                double d = 0.0;
                if (railShape.isSlope()) {
                    d = 0.5;
                }

                AbstractMinecart abstractMinecartEntity = getCart(
                        serverWorld, blockPos.getX() + 0.5, blockPos.getY() + 0.0625 + d, blockPos.getZ() + 0.5, this.type, this, itemStack
                );
                if (abstractMinecartEntity instanceof FluidMinecartEntity entity) {
                    if (this.blockInside.equals(Blocks.WATER)) {
                        entity.setCustomDisplayBlockState(Optional.of(Blocks.WATER.defaultBlockState()));
                    } else if (this.blockInside.equals(Blocks.LAVA)) {
                        entity.setCustomDisplayBlockState(Optional.of(Blocks.LAVA.defaultBlockState()));
                    }
                }
                abstractMinecartEntity.setYRot(railShape == RailShape.EAST_WEST ? 0 : 90.0F);
                serverWorld.addFreshEntity(abstractMinecartEntity);
                serverWorld.gameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Context.of(context.getPlayer(), serverWorld.getBlockState(blockPos.below())));
            }

            itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public BlockState getBlockInside(ItemStack stack) {
        return getSyncedBlockState(stack, this.blockInside);
    }

}
