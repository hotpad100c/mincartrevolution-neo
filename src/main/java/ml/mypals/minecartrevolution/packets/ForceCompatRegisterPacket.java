package ml.mypals.minecartrevolution.packets;

import static net.neoforged.neoforge.common.NeoForgeMod.MOD_ID;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ForceCompatRegisterPacket(boolean active) implements CustomPacketPayload {

  public static final Identifier FORCE_COMPAT_PID =
      Identifier.fromNamespaceAndPath(MOD_ID, "register_force_compat");

  public static final Type<ForceCompatRegisterPacket> TYPE = new Type<>(FORCE_COMPAT_PID);

  public static final StreamCodec<ByteBuf, ForceCompatRegisterPacket> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.BOOL, ForceCompatRegisterPacket::active, ForceCompatRegisterPacket::new);

  @Override
  public @NonNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
