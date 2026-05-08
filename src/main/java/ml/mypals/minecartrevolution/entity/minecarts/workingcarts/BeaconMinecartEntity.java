package ml.mypals.minecartrevolution.entity.minecarts.workingcarts;

import net.minecraft.world.item.ItemStack;
import ml.mypals.minecartrevolution.entity.minecarts.HasVariantRegularBlockMinecartEntity;
import ml.mypals.minecartrevolution.registeries.MRMinecarts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BeaconMinecartEntity extends HasVariantRegularBlockMinecartEntity implements MenuProvider, BeaconBeamOwner {
    public static final List<List<Holder<MobEffect>>> BEACON_EFFECTS = List.of(
            List.of(MobEffects.SPEED, MobEffects.HASTE),
            List.of(MobEffects.RESISTANCE, MobEffects.JUMP_BOOST),
            List.of(MobEffects.STRENGTH),
            List.of(MobEffects.REGENERATION)
    );
    private static final Set<Holder<MobEffect>> VALID_EFFECTS = BEACON_EFFECTS.stream().flatMap(Collection::stream).collect(Collectors.toSet());

    private static final EntityDataAccessor<Integer> DATA_PRIMARY_ID = SynchedEntityData.defineId(BeaconMinecartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SECONDARY_ID = SynchedEntityData.defineId(BeaconMinecartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEVELS = SynchedEntityData.defineId(BeaconMinecartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_CHARGE_TICKS = SynchedEntityData.defineId(BeaconMinecartEntity.class, EntityDataSerializers.LONG);

    private boolean onBase = false;
    private int levels = 0;
    private long chargeTicks = 0;
    private BlockPos lastCheckPos = null;
    private boolean isBlocked = false;
    private List<BeaconBeamOwner.Section> beamSections = new ArrayList<>();
    private List<BeaconBeamOwner.Section> checkingBeamSections = new ArrayList<>();

    private static @Nullable Holder<MobEffect> filterEffect(@Nullable Holder<MobEffect> effect) {
        return VALID_EFFECTS.contains(effect) ? effect : null;
    }

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> BeaconMinecartEntity.this.levels;
                case 1 -> BeaconMenu.encodeEffect(BeaconMinecartEntity.this.getPrimaryEffect());
                case 2 -> BeaconMenu.encodeEffect(BeaconMinecartEntity.this.getSecondaryEffect());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    BeaconMinecartEntity.this.levels = value;
                    BeaconMinecartEntity.this.entityData.set(DATA_LEVELS, value);
                    break;
                case 1:
                    BeaconMinecartEntity.this.setPrimaryEffect(filterEffect(BeaconMenu.decodeEffect(value)));
                    break;
                case 2:
                    BeaconMinecartEntity.this.setSecondaryEffect(filterEffect(BeaconMenu.decodeEffect(value)));
                    break;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Override
    public @NonNull ItemStack getPickResult() {
        return MRMinecarts.BEACON_MINECART.item().toStack();
    }

    public BeaconMinecartEntity(EntityType<? extends AbstractMinecart> entityType, Level world) {
        super(entityType, world);
    }

    public BeaconMinecartEntity(EntityType<? extends AbstractMinecart> minecart, Level world, double x, double y, double z, Item item) {
        super(minecart, world, x, y, z, item);
    }

    @Override
    public int getLightLevel() {
        float alpha = getChargeTicks() >= 200 ? 1.0f : (float) getChargeTicks() / 200.0f;
        return (int) (15 * alpha);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PRIMARY_ID, -1);
        builder.define(DATA_SECONDARY_ID, -1);
        builder.define(DATA_LEVELS, 0);
        builder.define(DATA_CHARGE_TICKS, 0L);
    }

    @Nullable
    public Holder<MobEffect> getPrimaryEffect() {
        int id = this.entityData.get(DATA_PRIMARY_ID);
        return BeaconMenu.decodeEffect(id);
    }

    @Nullable
    public Holder<MobEffect> getSecondaryEffect() {
        int id = this.entityData.get(DATA_SECONDARY_ID);
        return BeaconMenu.decodeEffect(id);
    }

    public void setPrimaryEffect(@Nullable Holder<MobEffect> effect) {
        this.entityData.set(DATA_PRIMARY_ID, BeaconMenu.encodeEffect(effect));
    }

    public void setSecondaryEffect(@Nullable Holder<MobEffect> effect) {
        this.entityData.set(DATA_SECONDARY_ID, BeaconMenu.encodeEffect(effect));
    }

    public long getChargeTicks() {
        return this.entityData.get(DATA_CHARGE_TICKS);
    }

    @Override
    public void tick() {
        super.tick();

        keepUpdatingLight = getLightLevel() < 15;
        if (!this.level().isClientSide()) {

            BlockPos currentPos = this.blockPosition();
            if (lastCheckPos == null || !lastCheckPos.equals(currentPos)) {
                updateLevels();
                updateBeam();
                lastCheckPos = currentPos;
            } else if (this.level().getGameTime() % 80 == 0) {
                updateBeam();
            }

            if (this.onBase) {
                this.chargeTicks++;
            } else if (this.chargeTicks > 0) {
                if(chargeTicks-1<=0){
                    updateLevels();
                    updateBeam();
                }
                this.chargeTicks--;
            }
            this.entityData.set(DATA_CHARGE_TICKS, this.chargeTicks);

            if ((this.chargeTicks > 0 || (this.levels > 0 && !isBlocked))) {
                applyEffects();
            }
        }
    }

    public void updateBeam() {
        this.isBlocked = false;
        this.checkingBeamSections.clear();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int x = this.blockPosition().getX();
        int yStart = this.blockPosition().getY() + 1;
        int z = this.blockPosition().getZ();

        BeaconBeamOwner.Section lastBeamSection = new BeaconBeamOwner.Section(0xFFFFFF);
        this.checkingBeamSections.add(lastBeamSection);
        
        int worldHeight = this.level().getMaxY();

        for (int y = yStart; y < worldHeight; y++) {
            mutablePos.set(x, y, z);
            BlockState state = this.level().getBlockState(mutablePos);
            Integer color = state.getBeaconColorMultiplier(this.level(), mutablePos, this.blockPosition());
            
            if (color != null) {
                if (color == lastBeamSection.getColor()) {
                    lastBeamSection.increaseHeight();
                } else {
                    lastBeamSection = new BeaconBeamOwner.Section(ARGB.average(lastBeamSection.getColor(), color));
                    this.checkingBeamSections.add(lastBeamSection);
                }
            } else {
                if (state.getLightDampening() >= 15 && !state.is(Blocks.BEDROCK)) {
                    this.isBlocked = true;
                    this.checkingBeamSections.clear();
                    break;
                }
                lastBeamSection.increaseHeight();
            }
        }
        this.beamSections = new ArrayList<>(this.checkingBeamSections);
    }

    @Override
    public @NonNull List<BeaconBeamOwner.Section> getBeamSections() {
        if (this.level().isClientSide()) {
            if (lastCheckPos == null || !lastCheckPos.equals(this.blockPosition())) {
                updateBeam();
                lastCheckPos = this.blockPosition();
            }
        }
        return this.beamSections;
    }

    private void updateLevels() {
        int oldLevel = this.levels;
        int newLevel = 0;

        BlockPos basePos = this.blockPosition().below();

        for (int level = 1; level <= 4; level++) {
            int y = basePos.getY() - level;
            if (y < this.level().getMinY()) {
                break;
            }

            boolean levelValid = true;

            for (int x = basePos.getX() - level; x <= basePos.getX() + level && levelValid; x++) {
                for (int z = basePos.getZ() - level; z <= basePos.getZ() + level; z++) {
                    if (!this.level().getBlockState(new BlockPos(x, y, z))
                            .is(BlockTags.BEACON_BASE_BLOCKS)) {
                        levelValid = false;
                        break;
                    }
                }
            }

            if (levelValid) {
                newLevel = level;
            } else {
                break;
            }
        }

        onBase = newLevel > 0;
        if (this.chargeTicks == 0 || newLevel > oldLevel) {
            this.levels = newLevel;
            this.entityData.set(DATA_LEVELS, this.levels);
        }
    }

    private void applyEffects() {
        if (this.level().isClientSide()) return;

        Holder<MobEffect> primary = getPrimaryEffect();
        Holder<MobEffect> secondary = getSecondaryEffect();

        if (primary == null) return;

        double range = this.levels * 10 + 10;
        int duration = (9 + this.levels * 2) * 20;
        int amplifier = 0;

        if (this.levels >= 4 && Objects.equals(primary, secondary)) {
            amplifier = 1;
        }

        AABB aabb = (new AABB(this.blockPosition())).inflate(range).expandTowards(0.0, (double) this.level().getHeight(), 0.0);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, aabb);

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(primary, duration, amplifier, true, true));
        }

        if (this.levels >= 4 && !Objects.equals(primary, secondary) && secondary != null) {
            for (Player player : players) {
                player.addEffect(new MobEffectInstance(secondary, duration, 0, true, true));
            }
        }
    }

    @Override
    public @NonNull InteractionResult interact(Player player, @NonNull InteractionHand hand, @NonNull Vec3 pos) {
        if (!player.isSecondaryUseActive() && !player.isSprinting()) {
            if (!this.level().isClientSide()) {
                player.openMenu(this);
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand, pos);
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("container.beacon");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory, @NonNull Player player) {
        return new BeaconMenu(containerId, inventory, this.dataAccess, ContainerLevelAccess.create(this.level(), this.blockPosition())) {
            @Override
            public boolean stillValid(@NonNull Player player) {
                return player.distanceToSqr(BeaconMinecartEntity.this) <= 64.0D;
            }
        };
    }

    private static void storeEffect(ValueOutput output, String field, @Nullable Holder<MobEffect> effect) {
        if (effect != null) {
            effect.unwrapKey().ifPresent(key -> output.putString(field, key.identifier().toString()));
        }
    }

    private static @Nullable Holder<MobEffect> loadEffect(ValueInput input, String field) {
        return input.read(field, BuiltInRegistries.MOB_EFFECT.holderByNameCodec()).filter(VALID_EFFECTS::contains).orElse(null);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPrimaryEffect(loadEffect(input, "primary_effect"));
        this.setSecondaryEffect(loadEffect(input, "secondary_effect"));

        this.chargeTicks = input.getLongOr("ChargeTicks", 0);
        this.levels = input.getIntOr("Levels", 0);
        this.entityData.set(DATA_LEVELS, this.levels);
        this.entityData.set(DATA_CHARGE_TICKS, this.chargeTicks);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        storeEffect(output, "primary_effect", this.getPrimaryEffect());
        storeEffect(output, "secondary_effect", this.getSecondaryEffect());
        output.putLong("ChargeTicks", this.chargeTicks);
        output.putInt("Levels", this.levels);
    }

    @Override
    public @NonNull Item getDropItem() {
        return MRMinecarts.BEACON_MINECART.item().get();
    }
}
