package ml.mypals.minecartrevolution.registeries;

import static ml.mypals.minecartrevolution.MinecartRevolution.MODID;

import net.minecraft.resources.Identifier;

public class MRPackets {
  public static final Identifier JUKEBOX_MINECART_UPDATE =
      Identifier.fromNamespaceAndPath(MODID, "jukebox_minecart_update");
  public static final Identifier BABEL_SCRAMBLE =
      Identifier.fromNamespaceAndPath(MODID, "babel_scramble");
  public static final Identifier MINECART_COLLISION =
      Identifier.fromNamespaceAndPath(MODID, "minecart_collision");
  public static final Identifier CHAIN_SYNC = Identifier.fromNamespaceAndPath(MODID, "chain_sync");
  public static final Identifier ENDER_PORTAL_SHAKE =
      Identifier.fromNamespaceAndPath(MODID, "ender_portal_shake");
}
