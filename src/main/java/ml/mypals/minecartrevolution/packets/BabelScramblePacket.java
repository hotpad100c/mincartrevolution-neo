package ml.mypals.minecartrevolution.packets;

import static ml.mypals.minecartrevolution.registeries.MRPackets.BABEL_SCRAMBLE;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record BabelScramblePacket() implements CustomPacketPayload {

  public static final Identifier BABEL_SCRAMBLE_ID = BABEL_SCRAMBLE;

  public static final Type<BabelScramblePacket> TYPE = new Type<>(BABEL_SCRAMBLE_ID);

  public static final StreamCodec<ByteBuf, BabelScramblePacket> STREAM_CODEC =
      StreamCodec.unit(new BabelScramblePacket());

  @Override
  public @NonNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
