package ml.mypals.minecartrevolution.registeries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static net.neoforged.neoforge.common.NeoForgeMod.MOD_ID;

public class MREntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS,
                    MOD_ID
            );
    public static final Supplier<EntityDataSerializer<CompoundTag>> COMPOUND_TAG_SERIALIZER =
            ENTITY_DATA_SERIALIZERS.register(
                    "compound_tag",
                    () -> EntityDataSerializer.forValueType(
                            ByteBufCodecs.COMPOUND_TAG
                    )
            );
    public static void init() {
        System.out.println("MREntityDataSerializers loaded");
    }
}
