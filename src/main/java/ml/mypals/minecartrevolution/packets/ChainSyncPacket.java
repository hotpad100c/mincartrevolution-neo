package ml.mypals.minecartrevolution.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

import static ml.mypals.minecartrevolution.registeries.MRPackets.CHAIN_SYNC;

public record ChainSyncPacket(int chainEntityId, List<Vec3> segments) implements CustomPacketPayload {

    public static final Type<ChainSyncPacket> TYPE = new Type<>(CHAIN_SYNC);

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Vec3>> VEC3_LIST_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull List<Vec3> decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<Vec3> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            }
            return list;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, @NonNull List<Vec3> list) {
            buf.writeVarInt(list.size());
            for (Vec3 v : list) {
                buf.writeDouble(v.x);
                buf.writeDouble(v.y);
                buf.writeDouble(v.z);
            }
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, ChainSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ChainSyncPacket::chainEntityId,
                    VEC3_LIST_CODEC,
                    ChainSyncPacket::segments,
                    ChainSyncPacket::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
