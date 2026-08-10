package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Radiation / contamination / Sellafield timing (legacy RadiationConfig subset + port knobs).
 */
public final class RadiationConfig {
    public static ForgeConfigSpec.BooleanValue enableChunkRads;
    public static ForgeConfigSpec.BooleanValue enableContamination;
    public static ForgeConfigSpec.BooleanValue enableDigamma;
    public static ForgeConfigSpec.BooleanValue worldRadEffects;
    public static ForgeConfigSpec.IntValue sellafieldTickInterval;
    public static ForgeConfigSpec.IntValue sellafieldDecayChanceHigh;
    public static ForgeConfigSpec.IntValue sellafieldDecayChanceZero;

    private RadiationConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("Radiation, contamination, Sellafield decay").push("radiation");

        enableChunkRads = builder
                .comment("Enable per-chunk radiation field (spread + decay)")
                .define("enableChunkRads", true);
        enableContamination = builder
                .comment("Enable entity radiation dose accumulation")
                .define("enableContamination", true);
        enableDigamma = builder
                .comment("Enable digamma contamination axis")
                .define("enableDigamma", true);
        worldRadEffects = builder
                .comment("High chunk rad terraforms grass into waste earth (legacy Simple world destruction)")
                .define("worldRadEffects", false);

        sellafieldTickInterval = builder
                .comment("Ticks between Sellafield decay/emission attempts (~5 min default)")
                .defineInRange("sellafieldTickInterval", 6000, 20, 72000);
        sellafieldDecayChanceHigh = builder
                .comment("1/N chance to drop Sellafield rank when rank > 0")
                .defineInRange("sellafieldDecayChanceHigh", 40, 2, 500);
        sellafieldDecayChanceZero = builder
                .comment("1/N chance for Sellafield 0 to become gravel/sand")
                .defineInRange("sellafieldDecayChanceZero", 60, 2, 500);

        builder.pop();
    }
}
