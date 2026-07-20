package ml.mypals.minecartrevolution.entity.minecarts;

import java.util.Optional;
import java.util.function.Consumer;

import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;
import ml.mypals.minecartrevolution.client.light.DynamicLightsSpread;
import ml.mypals.minecartrevolution.client.light.DynamicLightsStorage;
import ml.mypals.minecartrevolution.entity.chain.ChainEntity;
import ml.mypals.minecartrevolution.item.WrenchItem;
import ml.mypals.minecartrevolution.packets.MinecartCollisionPacket;
import ml.mypals.minecartrevolution.registeries.MRDataComponents;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static net.neoforged.neoforge.common.CommonHooks.isEntityInvulnerableTo;

public class VariantBlockMinecartEntity extends AbstractMinecart {
  public boolean activated = false;
  public boolean keepUpdatingLight = false;
  private boolean firstTickUpdateLight = true;
  private int oldLight = 0;
  public double mass = 0.1D;
  private boolean movingEntities = false;
  private Vec3 lastTickPos;
  private float lastTickRot = Float.NaN;

  public VariantBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> entityType, Level world) {
    super(entityType, world);
  }

  public VariantBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> entityType, Level world, Item item) {
    super(entityType, world);
    Block block = Block.byItem(item);
    this.setCustomDisplayBlockState(Optional.of(block.defaultBlockState()));
  }

  public VariantBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      Item item) {
    super(minecart, world, x, y, z);
    Block block = Block.byItem(item);
    this.setCustomDisplayBlockState(Optional.of(block.defaultBlockState()));
  }

  public VariantBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart,
      Level world,
      double x,
      double y,
      double z,
      Block block) {
    super(minecart, world, x, y, z);
    this.setCustomDisplayBlockState(Optional.of(block.defaultBlockState()));
  }

  public VariantBlockMinecartEntity(
      EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z) {
    super(minecart, world, x, y, z);
  }

  @Override
  public @NonNull BlockState getDisplayBlockState() {
    return this.getEntityData()
        .get(DATA_ID_CUSTOM_DISPLAY_BLOCK)
        .orElse(getDefaultDisplayBlockState());
  }

  @Override
  public void destroy(@NonNull ServerLevel serverLevel, DamageSource source) {
    boolean sourceIsPlayer = false;
    Player playerEntity = null;
    if (source.getEntity() instanceof Player player) {
      sourceIsPlayer = true;
      playerEntity = player;
    }
    boolean shouldDrop =
        serverLevel.getGameRules().get(GameRules.ENTITY_DROPS) || (sourceIsPlayer && !((Player) source.getEntity()).isCreative());
    if (shouldDrop) {
      if (playerEntity != null && playerEntity.isSecondaryUseActive()) {
        ItemStack stack = Items.MINECART.getDefaultInstance();
        spawnAtLocation(serverLevel, stack);
        BlockState blockState =
                entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

        ItemStack stack2 = blockState.getBlock().asItem().getDefaultInstance();
        Containers.dropItemStack(this.level(), this.getX(), this.getY(), this.getZ(), stack2);
      } else {
        ItemStack stack = getPickResult();
        spawnAtLocation(serverLevel, stack);
      }
      this.remove(Entity.RemovalReason.KILLED);

    }

    this.kill(serverLevel);
  }

  @Override
  public void onClientRemoval() {
    removeDynamicLight(this.blockPosition(), true);
    super.onClientRemoval();
  }

  private void clear() {
    setCustomDisplayBlockState(Optional.of(Blocks.AIR.defaultBlockState()));
    Minecart minecartEntity = new Minecart(EntityType.MINECART, level());

    minecartEntity.restoreFrom(this);
    minecartEntity.copyPosition(this);
    minecartEntity.setDeltaMovement(this.getDeltaMovement());
    this.remove(RemovalReason.DISCARDED);
    this.level().addFreshEntity(minecartEntity);
    minecartEntity.setHurtDir(-minecartEntity.getHurtDir());
    minecartEntity.setHurtTime(10);
    minecartEntity.setDamage(50.0F);
  }

  @Override
  public @NonNull Component getName() {
    if (getCustomName() != null) return getCustomName();
    String blockName = getDisplayBlockState().getBlock().getName().getString();
    String cartName = Items.MINECART.getName(Items.MINECART.getDefaultInstance()).getString();
    return Component.translatable(
        "item.minecartrevolution.minecart_with_block", blockName, cartName);
  }

  public void addDataToStack(ItemStack stack) {}

  @Override
  public @NonNull InteractionResult interact(
      @NonNull Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {

    PlayerInteractEvent.EntityInteract evt = new PlayerInteractEvent.EntityInteract(player, hand, this);
    NeoForge.EVENT_BUS.post(evt);
    InteractionResult interactionResult = super.interact(player, hand, pos);

    if(evt.isCanceled()) return evt.getCancellationResult();
    if (interactionResult.consumesAction()) return interactionResult;

    ItemStack stackInHand = player.getItemInHand(InteractionHand.MAIN_HAND);

    if (stackInHand.getItem() instanceof WrenchItem wrench) {
      return wrench.useOnMinecart(player, this, hand);
    }

    if (player.isSecondaryUseActive()) {
      if (this.hasCustomDisplay()) {
        BlockState blockState = getDisplayBlockState();

        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
          Block block = blockState.getBlock();
          playSound(block.defaultBlockState().getSoundType().getBreakSound(), 1, 1);
          player.swing(hand);
          if (!this.level().isClientSide()) {
            ItemStack stack = getCloneItemStack(blockState);
            player.setItemInHand(hand, stack);
            clear();
          } else {
            removeDynamicLight(this.blockPosition(), true);
          }
        }
        return InteractionResult.SUCCESS;
      } else if (!stackInHand.isEmpty()) {
        if (stackInHand.getItem() instanceof BlockItem blockItem) {
          setCustomDisplayBlockState(Optional.of(blockItem.getBlock().defaultBlockState()));
          player.swing(hand);
          playSound(blockItem.getBlock().defaultBlockState().getSoundType().getPlaceSound(), 1, 1);
          if (!this.level().isClientSide()) {
            MinecartTransformManager.checkForTransform(
                level(), position(), blockItem, this, stackInHand);
            stackInHand.consume(1, player);
          }
        } else if (stackInHand.is(Items.WATER_BUCKET) || stackInHand.is(Items.LAVA_BUCKET)) {
          if (!level().isClientSide()) {
            stackInHand.consume(1, player);
            player.getInventory().add(new ItemStack(Items.BUCKET));
            transformTo(stackInHand.getItem());
          }
          playBucketSound(Blocks.WATER);
          return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
      } else {
        return InteractionResult.PASS;
      }
    } else {
      player.swing(hand);
      return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }
  }

  public ItemStack getCloneItemStack(BlockState blockState) {
    ItemStack stack = blockState.getBlock().asItem().getDefaultInstance();
    addDataToStack(stack);
    return stack;
  }

  public void transformTo(Item item) {
    MinecartTransformManager.checkForTransform(level(), position(), item, this, ItemStack.EMPTY);
  }

  public void transformTo(Block block) {
    MinecartTransformManager.checkForTransform(level(), position(), block, this, ItemStack.EMPTY);
  }

  protected void playBucketSound(Block block) {
    if (block == Blocks.LAVA) {
      level()
          .playSound(
              null, blockPosition(), SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
    } else {
      level()
          .playSound(
              null, blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
  }

  @Override
  public void activateMinecart(@NonNull ServerLevel level, int x, int y, int z, boolean powered) {
    if (powered) {
      this.ejectPassengers();
    }
    handleActive(level, x, y, z, powered);
  }

  @Override
  public @NonNull Vec3 getPassengerRidingPosition(@NonNull Entity passenger) {
    if (this.hasCustomDisplay()) {
      BlockState myBlock =
          entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
      double y =
          myBlock.getCollisionShape(level(), this.blockPosition()).isEmpty()
              ? 0
              : myBlock.getCollisionShape(level(), this.blockPosition()).bounds().getMaxPosition().y
                  - 0.2;
      return super.getPassengerRidingPosition(passenger).add(0, y, 0);
    } else {
      return super.getPassengerRidingPosition(passenger);
    }
  }

  @Override
  public @NonNull Item getDropItem() {
    return MRMinecarts.BLOCK_MINECART_ITEM.item().get();
  }

  @Override
  public @NonNull ItemStack getPickResult() {
    ItemStack stack = getDropItem().getDefaultInstance();
    BlockState blockState = getDisplayBlockState();
    stack.set(MRDataComponents.BLOCK_STATE.get(), blockState);
    CompoundTag nbt = new CompoundTag();
    nbt.putInt("block_in_minecart", Block.getId(blockState));
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

    stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
    return stack;
  }

  public boolean hasCustomDisplay() {
    return !(getDisplayBlockState().getBlock() instanceof AirBlock);
  }

  public void handleActive(ServerLevel level, BlockPos blockPos, boolean powered) {
    handleActive(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), powered);
  }

  public void handleActive(ServerLevel level, int x, int y, int z, boolean powered) {
    this.activated = powered;
  }

  @Override
  public void tick() {
    super.tick();
    moveEntitiesAbove((_)->{});
    if (this.level().isClientSide()) {
      tickDynamicLight((ClientLevel) this.level());
    }
    if (activated
        && level() instanceof ServerLevel serverLevel
        && !(this.level().getBlockState(BlockPos.containing(this.position())).getBlock()
                instanceof PoweredRailBlock poweredRailBlock
            && poweredRailBlock.isActivatorRail())) {
      activateMinecart(
          serverLevel,
          this.blockPosition().getX(),
          this.blockPosition().getY(),
          this.blockPosition().getZ(),
          false);
    }

    collideWithEntities();
    if (activated && !level().isClientSide()) {
      BlockState rail = level().getBlockState(getCurrentBlockPosOrRailBelow());
      if (!(rail.is(Blocks.ACTIVATOR_RAIL) && rail.getValue(PoweredRailBlock.POWERED))) {
        handleActive((ServerLevel) level(), blockPosition(), false);
      }
    }
  }

  @Override
  protected void moveAlongTrack(@NonNull ServerLevel level) {
    super.moveAlongTrack(level);
    moveEntitiesAbove((_)->{});
  }

  public int getLightLevel() {

    BlockState displayBlock = getDisplayBlockState();

    return displayBlock.getLightEmission();
  }

  protected void tickDynamicLight(ClientLevel world) {
    int lightLevel = getLightLevel();

    BlockPos blockPos = this.blockPosition();
    BlockPos oldPos = BlockPos.containing(this.oldPosition());
    boolean moved = !oldPos.equals(blockPos);

    if (lightLevel > 0 || lightLevel != oldLight) {
      DynamicLightsStorage.LIGHT_SOURCES.put(this, lightLevel);
      if (moved || keepUpdatingLight || firstTickUpdateLight) {
        DynamicLightsSpread.markAreaDirty(oldPos, DynamicLightsSpread.RADIUS);
        DynamicLightsSpread.markAreaDirty(blockPos, DynamicLightsSpread.RADIUS);
        updateDynamicLight(world, blockPos);
        firstTickUpdateLight = false;
      }
      oldLight = lightLevel;
    } else {
      if (DynamicLightsStorage.LIGHT_SOURCES.remove(this) != null) {
        DynamicLightsSpread.markAreaDirty(oldPos, DynamicLightsSpread.RADIUS);
        DynamicLightsSpread.markAreaDirty(blockPos, DynamicLightsSpread.RADIUS);
        updateDynamicLight(world, blockPos);
      }
    }
  }

  private void removeDynamicLight(BlockPos pos, boolean update) {
    if (DynamicLightsStorage.LIGHT_SOURCES.remove(this) != null) {
      if (update) {
        BlockPos blockPos = this.blockPosition();
        BlockPos oldPos = BlockPos.containing(this.oldPosition());
        DynamicLightsSpread.markAreaDirty(oldPos, DynamicLightsSpread.RADIUS);
        DynamicLightsSpread.markAreaDirty(blockPos, DynamicLightsSpread.RADIUS);
      }
    }
  }

  protected void updateDynamicLight(ClientLevel world, BlockPos pos) {

    int vanillaLight = world.getBlockState(pos).getLightEmission(world, pos);
    double dynamicLight = DynamicLightsStorage.getLightLevel(pos);

    if (dynamicLight >= vanillaLight) {
      world.getChunkSource().getLightEngine().checkBlock(pos);
    }
  }

  @Override
  public boolean canCollideWith(@NonNull Entity entity) {
    return canVehicleCollide(this, entity);
  }

  public static boolean canVehicleCollide(Entity vehicle, Entity entity) {
    return (entity.canBeCollidedWith(vehicle) || entity.isPushable())
        && !vehicle.isPassengerOfSameVehicle(entity);
  }

  @Override
  public boolean canBeCollidedWith(@Nullable Entity other) {
    if (movingEntities) return false;
    return true;
  }

  public void moveEntitiesAbove(Consumer<Entity> consumer) {
    if (this.lastTickPos == null) {
      this.lastTickPos = new Vec3(this.xOld, this.yOld, this.zOld);
    }
    if (Float.isNaN(this.lastTickRot)) {
      this.lastTickRot = this.yRotO;
    }
    Vec3 movement = this.position().subtract(this.lastTickPos);
    float deltaRot = this.getYRot() - this.lastTickRot;

    if (movement.lengthSqr() < 1.0E-7 && Math.abs(deltaRot) < 1.0E-5) {
      this.lastTickPos = this.position();
      this.lastTickRot = this.getYRot();
      return;
    }

    double topY = this.getBoundingBox().maxY;
    double oldTopY = topY - movement.y;

    AABB aabb =
        this.getBoundingBox()
            .minmax(this.getBoundingBox().move(-movement.x, -movement.y, -movement.z))
            .inflate(0.1, 0.25, 0.1);

    if (getDisplayBlockState().is(Blocks.HONEY_BLOCK)) {
      aabb = aabb.inflate(0.3, 0.0, 0.3);
    }

    this.movingEntities = true;
    try {
      for (Entity entity : level().getEntitiesOfClass(Entity.class, aabb)) {
        consumer.accept(entity);
        if (entity == this
            || entity instanceof ChainEntity
            || entity.getVehicle() != null
            || entity.isPassenger()) {
          continue;
        }

        entity.setOnGround(true);

        if (entity.getBoundingBox().minY < Math.min(topY, oldTopY) - 0.2) continue;
        if (entity.getBoundingBox().minY > Math.max(topY, oldTopY) + 0.5) continue;

        Vec3 previousPos = entity.position();

        Vec3 offset =
            new Vec3(entity.getX() - this.lastTickPos.x, 0, entity.getZ() - this.lastTickPos.z);
        double rad = Math.toRadians(deltaRot);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double newOffsetX = offset.x * cos - offset.z * sin;
        double newOffsetZ = offset.x * sin + offset.z * cos;

        Vec3 expectedPos =
            new Vec3(
                this.getX() + newOffsetX, entity.getY() + movement.y, this.getZ() + newOffsetZ);
        Vec3 requiredMovement = expectedPos.subtract(entity.position());

        entity.move(MoverType.SELF, requiredMovement);

        if (entity.getBoundingBox().minY < topY) {
          double dy = topY - entity.getBoundingBox().minY;
          entity.setPos(entity.getX(), entity.getY() + dy, entity.getZ());
        }

        Vec3 velocity = entity.getDeltaMovement();
        float slipperiness = getDisplayBlockState().getBlock().getFriction();
        float friction = slipperiness * 0.5F;

        double x = velocity.x * friction;
        double z = velocity.z * friction;
        double y = velocity.y;

        if (Math.abs(x) < 0.003) x = 0;
        if (Math.abs(z) < 0.003) z = 0;

        entity.setDeltaMovement(x, y, z);
        entity.setOnGround(true);
        entity.applyEffectsFromBlocks(previousPos, entity.position());
        entity.hurtMarked = true;
        entity.needsSync = true;
      }
    } finally {
      this.movingEntities = false;
      this.lastTickPos = this.position();
      this.lastTickRot = this.getYRot();
    }
  }

  @Override
  public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float damage) {
    if (source.getEntity() instanceof Player player && !isEntityInvulnerableTo(player, source, false)) {
      this.addDeltaMovement(
          player.getLookAngle().multiply(0.1, player.isShiftKeyDown() ? 0.3 : 0, 0.1));
    }
    return super.hurtServer(level, source, damage);
  }

  @Override
  public boolean hurtClient(DamageSource source) {
    if (source.getEntity() instanceof Player player) {
      this.addDeltaMovement(
          player.getLookAngle().multiply(0.1, player.isShiftKeyDown() ? 0.3 : 0, 1.5));
    }
    return super.hurtClient(source);
  }

  @Override
  public void move(@NonNull MoverType moverType, @NonNull Vec3 delta) {
    Vec3 toPosition = this.position().add(delta);
    super.move(moverType, delta);
    Vec3 newPosition = this.position();
    boolean shouldContinue = this.getBehavior().pushAndPickupEntities();
    if (toPosition.distanceToSqr(newPosition) > getCollisionSensitive()) {
      shouldContinue = onCollision(position(), toPosition, newPosition, delta) && shouldContinue;
      if (!this.level().isClientSide()) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
            this,
            new MinecartCollisionPacket(this.getId(), position(), delta, toPosition, newPosition));
      }
    }
    if (shouldContinue) {
      super.move(moverType, toPosition.subtract(this.position()));
    }

    if (moverType.equals(MoverType.PISTON)) {
      this.setOnRails(false);
    }
  }

  public boolean onCollision(Vec3 position, Vec3 target, Vec3 actual, Vec3 delta) {
    return true;
  }

  public float getCollisionSensitive() {
    return 0.01f;
  }

  @Override
  protected void addAdditionalSaveData(@NonNull ValueOutput output) {
    super.addAdditionalSaveData(output);
    Optional<BlockState> blockState = this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY_BLOCK);
    if (blockState.isPresent() && !blockState.get().isAir()) {
      output.store("DisplayState", BlockState.CODEC, blockState.get());
    }
    output.putInt("DisplayOffset", this.getDisplayOffset());
    output.putBoolean("FlippedRotation", this.isFlipped());
    output.putBoolean("HasTicked", this.firstTick);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    Optional<BlockState> blockState = input.read("DisplayState", BlockState.CODEC);
    if (blockState.isPresent()) {
      this.setCustomDisplayBlockState(blockState);
    }
    this.setDisplayOffset(input.getIntOr("DisplayOffset", this.getDefaultDisplayOffset()));
    this.setFlipped(input.getBooleanOr("FlippedRotation", false));
    this.firstTick = input.getBooleanOr("HasTicked", false);
  }
  public void collideWithEntities() {
    if (!this.isOnRails() || this.level().isClientSide()) return;

    Vec3 movement = this.getDeltaMovement();

    if (movement.lengthSqr() > 4) {
      movement = movement.normalize().scale(2);
    }

    if (movement.lengthSqr() < 0.25D) return;

    Vec3 direction = movement.normalize();
    Vec3 pos = this.position();
    boolean collided = false;

    for (Entity entity : this.level().getEntities(
            this,
            this.getBoundingBox().expandTowards(direction.scale(0.8D)),
            e -> e instanceof LivingEntity
                    && e.canCollideWith(this)
                    && e != this.getControllingPassenger()
                    && e.isAlive())) {

      if (isEntityInvulnerableTo(entity, entity.damageSources().flyIntoWall(), false)
              || direction.dot(entity.position().subtract(pos)) <= 0) {
        continue;
      }

      double pushForce = Math.min(getMass() * 1.5D, 3.5D);
      Vec3 newVelocity = entity.getDeltaMovement().add(direction.scale(pushForce));

      double velocityLength = newVelocity.length();
      if (velocityLength > 1.5D) {
        newVelocity = newVelocity.scale(1.5D / velocityLength);
      }

      entity.setDeltaMovement(newVelocity);
      entity.hurtMarked = true;

      double damage = Math.min(getMass() * 4.0D * movement.length(), 30.0D);
      entity.hurtServer(
              (ServerLevel) this.level(),
              entity.damageSources().flyIntoWall(),
              (float) damage
      );

      collided = true;
    }

    if (collided) {
      this.setDeltaMovement(this.getDeltaMovement().scale(0.89D));
    }
  }

  public float getMass() {
    return 0.5f;
  }
}
