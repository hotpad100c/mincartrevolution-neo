package ml.mypals.minecartrevolution.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue FORCE_COMPATIBILITY = BUILDER
            .comment("Enable experimental compatibility mode. Use at your own risk.")
            .translation("config.yourmod.force_compatibility")
            .define("forceCompatibility", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
