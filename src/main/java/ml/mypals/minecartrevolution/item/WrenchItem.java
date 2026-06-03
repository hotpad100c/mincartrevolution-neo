package ml.mypals.minecartrevolution.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
public class WrenchItem extends Item {

    private static final String PROPERTY_KEY = "wrench_property";

    public WrenchItem(Properties props) {
        super(props);
    }
    public InteractionResult useOnMinecart(Player player, AbstractMinecart cart, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockState state = cart.getDisplayBlockState();

        if (state.isAir() || state.getProperties().isEmpty()) {
            sendMessage(player, Component.translatable("item.minecartrevolution.wrench.empty"));
            return InteractionResult.FAIL;
        }

        Collection<Property<?>> properties = state.getProperties();
        String selectedName = getSelectedPropertyName(stack);
        Property<?> selected = findProperty(state, selectedName);

        if (player.isSecondaryUseActive()) {
            selected = nextInCollection(new ArrayList<>(properties), selected);
            setSelectedPropertyName(stack, selected.getName());
            sendMessage(player,
                    Component.translatable("item.minecartrevolution.wrench.select", selected.getName()));
        } else {
            if (!cart.level().isClientSide()) {
                BlockState newState = cycleValue(state, selected);
                cart.setCustomDisplayBlockState(Optional.of(newState));
                sendMessage(player,
                        Component.translatable("item.minecartrevolution.wrench.set",
                                selected.getName(),
                                stringify(newState, selected)));
            }
        }
        player.swing(hand);
        return InteractionResult.SUCCESS;
    }

    private String getSelectedPropertyName(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "";
        return data.copyTag().getStringOr(PROPERTY_KEY, "");
    }

    private void setSelectedPropertyName(ItemStack stack, String name) {
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        tag.putString(PROPERTY_KEY, name);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private Property<?> findProperty(BlockState state, String name) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equals(name)) return prop;
        }
        return state.getProperties().iterator().next();
    }

    private <T> T nextInCollection(List<T> list, T current) {
        if (list.isEmpty()) throw new IllegalStateException("Empty list");
        int idx = list.indexOf(current);
        return list.get((idx + 1) % list.size());
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState cycleValue(BlockState state, Property<T> prop) {
        T current = state.getValue(prop);
        List<T> values = new ArrayList<>(prop.getPossibleValues());
        int idx = values.indexOf(current);
        T next = values.get((idx + 1) % values.size());
        return state.setValue(prop, next);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends Comparable<T>> String stringify(BlockState state, Property<T> prop) {
        return ((Property) prop).getName(state.getValue(prop));
    }

    private void sendMessage(Player player, Component message) {
        if (player instanceof ServerPlayer sp) {
            sp.sendOverlayMessage(message);
        }
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        return Component.translatable(getDescriptionId());
    }
}
