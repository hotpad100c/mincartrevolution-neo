package ml.mypals.minecartrevolution.packets;

import static ml.mypals.minecartrevolution.registeries.MRPackets.ENDER_PORTAL_SHAKE;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record EnderPortalShakePacket(int durationTicks, float intensity)
    implements CustomPacketPayload {

  public static final Identifier ENDER_PORTAL_SHAKE_ID = ENDER_PORTAL_SHAKE;

  public static final Type<EnderPortalShakePacket> TYPE = new Type<>(ENDER_PORTAL_SHAKE_ID);

  public static final StreamCodec<RegistryFriendlyByteBuf, EnderPortalShakePacket> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          EnderPortalShakePacket::durationTicks,
          ByteBufCodecs.FLOAT,
          EnderPortalShakePacket::intensity,
          EnderPortalShakePacket::new);

  @Override
  public @NonNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
