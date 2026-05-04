package ml.mypals.minecartrevolution.packets;

import static ml.mypals.minecartrevolution.registeries.MRPackets.JUKEBOX_MINECART_UPDATE;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record JukeboxUpdateS2CPacket(int uuid, int dickID, boolean play) implements CustomPacketPayload {

    public static final Identifier JUKEBOX_MINECART_UPDATE_ID = JUKEBOX_MINECART_UPDATE;

    public static final Type<JukeboxUpdateS2CPacket> TYPE =
            new Type<>(JUKEBOX_MINECART_UPDATE_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxUpdateS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    JukeboxUpdateS2CPacket::uuid,
                    ByteBufCodecs.VAR_INT,
                    JukeboxUpdateS2CPacket::dickID,
                    ByteBufCodecs.BOOL,
                    JukeboxUpdateS2CPacket::play,
                    JukeboxUpdateS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
