package ml.mypals.minecartrevolution.entity.minecarts;

import ml.mypals.minecartrevolution.registeries.MRModCriteria;
import ml.mypals.minecartrevolution.entity.minecarts.redstone.PowerEmitterMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import ml.mypals.minecartrevolution.item.MinecartWithBlockItem;
import ml.mypals.minecartrevolution.manager.MovingJukeboxManager;
import ml.mypals.minecartrevolution.packets.JukeboxUpdateS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class JukeboxMinecartEntity extends SingleBlockMinecartEntity
        implements PowerEmitterMinecartEntity, Clearable {
    private MovingJukeboxManager jukeboxManager = new MovingJukeboxManager(this::onManagerChange, this.blockPosition());
    protected static final EntityDataAccessor<ItemStack> DISC =
            SynchedEntityData.defineId(JukeboxMinecartEntity.class, EntityDataSerializers.ITEM_STACK);
    private ItemStack disc = Items.AIR.getDefaultInstance();

    public JukeboxMinecartEntity(EntityType<? extends JukeboxMinecartEntity> entityType, Level world) {
        super(entityType, world);
        disc = Items.AIR.getDefaultInstance();
    }

    public void onManagerChange() {
        this.level().updateNeighborsAt(this.blockPosition(), Blocks.JUKEBOX);
    }

    public JukeboxMinecartEntity(EntityType<? extends SingleBlockMinecartEntity> minecart, Level world, double x, double y, double z, MinecartWithBlockItem correspondingItem) {
        super(minecart, world, x, y, z, correspondingItem);
        disc = Items.AIR.getDefaultInstance();
    }

    private void onRecordStackChanged(boolean hasRecord) {
        this.level().gameEvent(GameEvent.BLOCK_CHANGE, this.position(), GameEvent.Context.of(this));
    }

    @Override
    public void clearContent() {
        playOrStop(false);
        this.disc = Items.AIR.getDefaultInstance();
        this.entityData.set(DISC, this.disc);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DISC, Items.AIR.getDefaultInstance());
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, @NonNull DamageSource source, float amount) {
        if (this.isRemoved()) {
            return true;
        } else if (this.isInvulnerableToBase(source)) {
            return false;
        } else {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(this.getDamage() + amount * 10.0F);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
            boolean bl = source.getEntity() instanceof Player && ((Player) source.getEntity()).getAbilities().instabuild;
            if ((bl || !(this.getDamage() > 40.0F)) && !this.shouldSourceDestroy(source)) {
                if (bl) {
                    this.playOrStop(false);
                    this.discard();
                }
            } else {
                this.destroy(serverLevel, source);
            }

            return true;
        }
    }

    @Override
    public void destroy(@NonNull ServerLevel serverLevel, DamageSource damageSource) {
        super.destroy(serverLevel, damageSource);
        if (serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
            this.spawnAtLocation(serverLevel, this.getDisc());
        }
    }

    @Override
    public @NonNull ItemStack getPickResult() {
        return new ItemStack(MRMinecarts.JUKEBOX_MINECART.item().get());
    }

    public ItemStack getDisc() {
        return entityData.get(DISC).isEmpty() ? this.disc : entityData.get(DISC);
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.JUKEBOX_MINECART.item().get();
    }

    @Override
    public ItemStack addDataToStack(ItemStack originalStack) {
        /*if(originalStack.isEmpty() || originalStack.getItem() != Items.JUKEBOX || this.disc.isEmpty()) {
            return originalStack;
        }
        NbtCompound blockEntityTag = new NbtCompound();
        blockEntityTag.put("RecordItem", this.disc.encode(getWorld().getRegistryManager()));
        originalStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(blockEntityTag));*/
        dropRecord();
        playOrStop(false);
        return originalStack;
    }

    public void dropRecord() {
        if (!this.level().isClientSide()) {
            BlockPos pos = this.blockPosition();
            ItemStack itemStack = getDisc();
            if (!itemStack.isEmpty()) {
                clearContent();
                Vec3 vec3d = Vec3.atLowerCornerWithOffset(pos, 0.5, 1.01, 0.5).offsetRandom(this.level().getRandom(), 0.7F);
                ItemStack itemStack2 = itemStack.copy();
                ItemEntity itemEntity = new ItemEntity(this.level(), vec3d.x(), vec3d.y(), vec3d.z(), itemStack2);
                itemEntity.setDefaultPickUpDelay();
                this.level().addFreshEntity(itemEntity);
            }
            playOrStop(false);
        }
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());
        this.updateNeighbors(level(), this.getPreviousBlockPos(), blockState.getBlock());
        this.updateNeighbors(level(), this.blockPosition(), blockState.getBlock());
    }

    @Override
    public void tick() {
        super.tick();
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

        this.jukeboxManager.tick(level(), blockState);
        if (this.getPreviousBlockPos() == null || !this.getPreviousBlockPos().equals(this.blockPosition())) {
            if (this.getPreviousBlockPos() == null) this.setPreviousBlockPos(this.blockPosition());
            updateNeighbors(this.level(), previousBlockPos, blockState.getBlock());
            this.setPreviousBlockPos(this.blockPosition());
            updateNeighbors(this.level(), this.blockPosition(), blockState.getBlock());
        }

        if (!this.disc.isEmpty()) {
            if (this.getHurtTime() <= 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(20);
                this.setDamage(2);
            }
            if (!level().isClientSide()) {
                jukeboxManager.movingPos = this.position();
            }
        }
        if (this.moveDist > 2f && !this.level().isClientSide() && !this.disc.isEmpty()) {
            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof ServerPlayer player) {
                    MRModCriteria.ENTITY_MOVED.get().trigger(player, this);
                }
            }
        }
    }

    @Override
    public void load(@NonNull ValueInput nbt) {
        super.load(nbt);

        if (nbt.getInt("RecordItem").isPresent()) {
            this.disc = (ItemStack) Item.byId(nbt.getInt("RecordItem").orElse(0)).getDefaultInstance();
        } else {
            this.disc = ItemStack.EMPTY;
        }
        this.entityData.set(DISC, this.disc);

        if (nbt.getLong("ticks_since_song_started").isPresent()) {
            JukeboxSong.fromStack(this.disc).
                    ifPresent((song) -> this.jukeboxManager.
                            setSongWithoutPlaying(song, nbt.getLong("ticks_since_song_started").orElse(4L)));
        }

    }

    @Override
    public void saveWithoutId(ValueOutput nbt) {
        if (!this.disc.isEmpty()) {
            nbt.putInt("RecordItem", Item.getId(this.disc.getItem()));
        }
        if (jukeboxManager != null && this.jukeboxManager.getSong() != null) {
            nbt.putLong("ticks_since_song_started", this.jukeboxManager.getTicksSinceSongStarted());
        }
        super.saveWithoutId(nbt);
    }

    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        ItemStack stack = player.getItemInHand(hand);
        player.swing(hand);
        if (player.isSecondaryUseActive()) {
            return super.interact(player, hand, pos);
        }
        if (level().isClientSide()) {
            return getDisc().isEmpty() && stack.isEmpty()
                    ? InteractionResult.PASS
                    : InteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            if (!getDisc().isEmpty()) {
                this.dropRecord();
                return InteractionResult.SUCCESS;
            }
            return super.interact(player, hand, pos);
        }

        JukeboxPlayable playable = stack.get(DataComponents.JUKEBOX_PLAYABLE);
        if (playable == null) {
            return super.interact(player, hand, pos);
        }

        if (!getDisc().isEmpty()) {
            this.dropRecord();
            return InteractionResult.SUCCESS;
        }
        this.disc = stack.copyWithCount(1);
        this.entityData.set(DISC, this.disc);
        this.onRecordStackChanged(true);
        this.playOrStop(true);

        player.getInventory().removeItem(stack);

        return InteractionResult.CONSUME;
    }


    @Override
    public int getPowerStrength(Direction direction, BlockPos pos) {
        if (!this.isAlive()) {
            return 0;
        }
        return this.jukeboxManager.isPlaying() ? 15 : 0;
    }

    private void playOrStop(boolean play) {
        if (level() instanceof ServerLevel serverWorld) {
            Optional<Holder<JukeboxSong>> optional = JukeboxSong.fromStack(this.getDisc());
            for (ServerPlayer players : getPlayersAround(serverWorld, this.position(), 128)) {

                if (optional.isPresent()) {
                    int i = level().registryAccess().lookupOrThrow(Registries.JUKEBOX_SONG).getId(optional.get().value());
                    PacketDistributor.sendToPlayer(players, new JukeboxUpdateS2CPacket(
                            this.getId(), i, play
                    ));
                }
            }
            if (play) {
                this.jukeboxManager.play(level(), optional.get());
            } else {
                this.jukeboxManager.stop(level(), null);
            }
        }

        this.setCustomDisplayBlockState(Optional.of(Blocks.JUKEBOX.defaultBlockState()
                .setValue(JukeboxBlock.HAS_RECORD, !this.getDisc().isEmpty())));
        BlockState blockState = entityData.get(DATA_ID_CUSTOM_DISPLAY_BLOCK).orElse(Blocks.AIR.defaultBlockState());

        this.updateNeighbors(level(), this.getPreviousBlockPos(), blockState.getBlock());
        this.updateNeighbors(level(), this.blockPosition(), blockState.getBlock());

    }

    public static Collection<ServerPlayer> getPlayersAround(ServerLevel world, Vec3 pos, double radius) {
        double radiusSq = radius * radius;
        return new ArrayList<>(Collections.unmodifiableCollection(world.getPlayers((player) -> player.distanceToSqr(pos) <= radiusSq)));
    }
}
