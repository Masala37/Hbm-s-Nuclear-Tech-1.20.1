package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Central Forge config registration for the 1.20.1 port.
 * Additional legacy categories will be added as systems are ported.
 */
public final class ModConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        GeneralConfig.build(builder);
        RBMKConfig.build(builder);
        BombConfig.build(builder);
        WorldConfig.build(builder);
        RadiationConfig.build(builder);
        COMMON_SPEC = builder.build();
    }

    private ModConfig() {
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                COMMON_SPEC);
    }
}
