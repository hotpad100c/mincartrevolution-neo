package ml.mypals.minecartrevolution.registeries;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;
public class MRDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final Supplier<DataComponentType<BlockState>> BLOCK_STATE =
            DATA_COMPONENT_TYPES.registerComponentType("block_state",
                    builder -> builder.persistent(BlockState.CODEC));
}
