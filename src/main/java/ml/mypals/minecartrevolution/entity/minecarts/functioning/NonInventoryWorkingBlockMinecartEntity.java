package ml.mypals.minecartrevolution.entity.minecarts.functioning;

import javax.annotation.Nullable;
import ml.mypals.minecartrevolution.entity.minecarts.VariantBlockMinecartEntity;
import ml.mypals.minecartrevolution.interfaces.IMinecartContainer;
import ml.mypals.minecartrevolution.inventory.ContainerEntityAccess;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class NonInventoryWorkingBlockMinecartEntity extends VariantBlockMinecartEntity
    implements IMinecartContainer {
  public int time;
  public float flip;
  public float oFlip;
  public float flipT;
  public float flipA;
  public float bookOpen;
  public float oBookOpen;
  public float bookRotation;
  public float oBookRotation;
  public float tRot;
  private static final RandomSource RANDOM = RandomSource.create();

  public NonInventoryWorkingBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> entityType, Level world) {
    super(entityType, world);
  }

  public NonInventoryWorkingBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      Item item) {
    super(minecart, world, x, y, z, item);
  }

  private Item getItem() {
    BlockState displayBlock = this.getDisplayBlockState();
    Block block = displayBlock.getBlock();
    return switch (block) {
      case SmithingTableBlock ignored -> MRMinecarts.SMITHING_TABLE_MINECART.item().get();
      case CraftingTableBlock ignored -> MRMinecarts.CRAFTING_TABLE_MINECART.item().get();
      case StonecutterBlock ignored -> MRMinecarts.STONECUTTER_MINECART.item().get();
      case LoomBlock ignored -> MRMinecarts.LOOM_MINECART.item().get();
      case CartographyTableBlock ignored -> MRMinecarts.CARTOGRAPHY_TABLE_MINECART.item().get();
      case GrindstoneBlock ignored -> MRMinecarts.GRINDSTONE_MINECART.item().get();
      case AnvilBlock ignored -> MRMinecarts.ANVIL_MINECART.item().get();
      case EnchantingTableBlock ignored -> MRMinecarts.ENCHANTING_TABLE_MINECART.item().get();
      default -> MRMinecarts.CRAFTING_TABLE_MINECART.item().get();
    };
  }

  @Override
  public @NonNull Item getDropItem() {
    return getItem();
  }

  @Override
  public @NonNull ItemStack getPickResult() {
    return getItem().getDefaultInstance();
  }

  @Override
  public void tick() {
    super.tick();

    if (this.level().isClientSide() && this.getDisplayBlockState().is(Blocks.ENCHANTING_TABLE)) {
      this.oBookOpen = this.bookOpen;
      this.oBookRotation = this.bookRotation;
      Player player =
          this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 3.0D, false);
      if (player != null) {
        double dX = player.getX() - this.getX();
        double dZ = player.getZ() - this.getZ();
        float worldAngle = (float) Mth.atan2(dZ, dX);
        float entityYawRadians = this.getYRot() * (float) (Math.PI / 180.0);
        this.tRot = worldAngle - entityYawRadians;
        if (this.level().getBlockState(this.blockPosition()).is(BlockTags.RAILS)) {
          this.tRot += (float) Math.PI;
        }
        this.bookOpen = Mth.clamp(this.bookOpen + 0.1F, 0.0F, 1.0F);
        if (this.bookOpen < 0.5F || RANDOM.nextInt(40) == 0) {
          float oldT = this.flipT;
          do {
            this.flipT += (float) (RANDOM.nextInt(4) - RANDOM.nextInt(4));
          } while (oldT == this.flipT);
        }
      } else {
        this.tRot += 0.02F;
        this.bookOpen = Mth.clamp(this.bookOpen - 0.1F, 0.0F, 1.0F);
      }
      while (this.bookRotation >= (float) Math.PI) this.bookRotation -= ((float) Math.PI * 2F);
      while (this.bookRotation < -(float) Math.PI) this.bookRotation += ((float) Math.PI * 2F);
      while (this.tRot >= (float) Math.PI) this.tRot -= ((float) Math.PI * 2F);
      while (this.tRot < -(float) Math.PI) this.tRot += ((float) Math.PI * 2F);

      float rotDiff = this.tRot - this.bookRotation;
      while (rotDiff >= (float) Math.PI) rotDiff -= ((float) Math.PI * 2F);
      while (rotDiff < -(float) Math.PI) rotDiff += ((float) Math.PI * 2F);

      this.bookRotation += rotDiff * 0.4F;
      this.oFlip = this.flip;
      float flipDiff = Mth.clamp((this.flipT - this.flip) * 0.4F, -0.2F, 0.2F);
      this.flipA += (flipDiff - this.flipA) * 0.9F;
      this.flip += this.flipA;

      this.time++;
    }
  }

  @Override
  public @NonNull InteractionResult interact(
      @NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
    if (!player.isSecondaryUseActive() && !player.isSprinting()) {
      BlockState blockState = this.getDisplayBlockState();
      if (this.level().isClientSide()) {
        return InteractionResult.SUCCESS;
      }

      MenuProvider provider = getMenuProvider(blockState);
      if (provider != null) {
        player.openMenu(provider);
        return InteractionResult.SUCCESS;
      }
    }
    return super.interact(player, hand, pos);
  }

  @Nullable
  private MenuProvider getMenuProvider(BlockState state) {
    if (state.is(Blocks.CRAFTING_TABLE)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new CraftingMenu(id, inv, new ContainerEntityAccess(this)),
          Component.translatable("container.crafting"));
    } else if (state.is(Blocks.STONECUTTER)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new StonecutterMenu(id, inv, new ContainerEntityAccess(this)),
          Component.translatable("container.stonecutter"));
    } else if (state.is(Blocks.LOOM)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new LoomMenu(id, inv, new ContainerEntityAccess(this)),
          Component.translatable("container.loom"));
    } else if (state.is(Blocks.CARTOGRAPHY_TABLE)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new CartographyTableMenu(id, inv, new ContainerEntityAccess(this)),
          Component.translatable("container.cartography_table"));
    } else if (state.is(Blocks.GRINDSTONE)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new GrindstoneMenu(id, inv, new ContainerEntityAccess(this)),
          Component.translatable("container.grindstone_title"));
    } else if (state.is(Blocks.SMITHING_TABLE)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new SmithingMenu(id, inv, new ContainerEntityAccess(this)),
          Component.translatable("container.upgrade"));
    } else if (state.is(BlockTags.ANVIL)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new AnvilMenu(id, inv, ContainerLevelAccess.NULL),
          Component.translatable("container.repair"));
    } else if (state.is(Blocks.ENCHANTING_TABLE)) {
      return new SimpleMenuProvider(
          (id, inv, _) -> new EnchantmentMenu(id, inv, new ContainerEntityAccess(this)),
          Component.translatable("container.enchant"));
    }
    return null;
  }

  @Override
  public void minecartrevolution$OnContainerClosed(Level level, Player player) {}
}
