package ml.mypals.minecartrevolution.item;

import ml.mypals.minecartrevolution.entity.minecarts.AdvancedMinecartEntityTypes;
import ml.mypals.minecartrevolution.entity.minecarts.DamageCausingMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class DamageCausingMinecartItem extends MinecartWithBlockItem {
    final float damageAmount;
    final MinecartWithBlockItem correspondingItem;
    final ResourceKey<DamageType> damageType;

    public DamageCausingMinecartItem(AdvancedMinecartEntityTypes.Type type, Properties settings, float damageAmount, Block blockInside, ResourceKey<DamageType> damageType) {
        super(type, settings, blockInside);
        this.damageAmount = damageAmount;
        this.damageType = damageType;
        this.correspondingItem = this;
    }

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
                        ? blockState2.getValue(((BaseRailBlock) blockState2.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railShape2.isSlope()) {
                    g = -0.4;
                } else {
                    g = -0.9;
                }
            }

            AbstractMinecart abstractMinecartEntity = getCart(serverWorld, d, e + g, f,
                    ((DamageCausingMinecartItem) stack.getItem()).blockInside, ((DamageCausingMinecartItem) stack.getItem()).correspondingItem);


            serverWorld.addFreshEntity(abstractMinecartEntity);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(BlockSource pointer) {
            pointer.level().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, pointer.pos(), 0);
        }
    };

    public static AbstractMinecart getCart(Level world, double x, double y, double z, Block blockInside, MinecartWithBlockItem correspondingItem) {

        DamageCausingMinecartEntity damageCausingMinecartEntity = new DamageCausingMinecartEntity(
                ml.mypals.minecartrevolution.registeries.MRMinecarts.DAMAGE_CAUSING_MINECART.get(), world,
                x, y, z,
                ((DamageCausingMinecartItem) correspondingItem).damageAmount,
                correspondingItem,
                ((DamageCausingMinecartItem) correspondingItem).damageType
        );
        damageCausingMinecartEntity.setCustomDisplayBlockState(Optional.of(blockInside.defaultBlockState()));
        return damageCausingMinecartEntity;
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
                        serverWorld,
                        blockPos.getX() + 0.5,
                        blockPos.getY() + 0.0625 + d,
                        blockPos.getZ() + 0.5,
                        this.blockInside,
                        this.correspondingItem
                );
                abstractMinecartEntity.setCustomDisplayBlockState(Optional.of(blockInside.defaultBlockState()));
                serverWorld.addFreshEntity(abstractMinecartEntity);
                serverWorld.gameEvent(GameEvent.ENTITY_PLACE, blockPos, GameEvent.Context.of(context.getPlayer(), serverWorld.getBlockState(blockPos.below())));
            }

            itemStack.shrink(1);
            return InteractionResult.SUCCESS;
        }
    }
}
