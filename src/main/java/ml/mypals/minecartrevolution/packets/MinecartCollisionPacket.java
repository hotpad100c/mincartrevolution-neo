package ml.mypals.minecartrevolution.packets;

import static ml.mypals.minecartrevolution.registeries.MRPackets.MINECART_COLLISION;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public record MinecartCollisionPacket(int entityId, Vec3 pos, Vec3 delta, Vec3 target, Vec3 actual)
    implements CustomPacketPayload {

  public static final Identifier MINECART_COLLISION_ID = MINECART_COLLISION;

  public static final Type<MinecartCollisionPacket> TYPE = new Type<>(MINECART_COLLISION_ID);

  private static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3_STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.DOUBLE,
          Vec3::x,
          ByteBufCodecs.DOUBLE,
          Vec3::y,
          ByteBufCodecs.DOUBLE,
          Vec3::z,
          Vec3::new);

  public static final StreamCodec<RegistryFriendlyByteBuf, MinecartCollisionPacket> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          MinecartCollisionPacket::entityId,
          VEC3_STREAM_CODEC,
          MinecartCollisionPacket::pos,
          VEC3_STREAM_CODEC,
          MinecartCollisionPacket::delta,
          VEC3_STREAM_CODEC,
          MinecartCollisionPacket::target,
          VEC3_STREAM_CODEC,
          MinecartCollisionPacket::actual,
          MinecartCollisionPacket::new);

  @Override
  public @NonNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
