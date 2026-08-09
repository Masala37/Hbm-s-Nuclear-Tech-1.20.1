package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RBMKConfig {
    /** Number of dummy segments above the core (legacy default: 4 → 5 blocks total). */
    public static ForgeConfigSpec.IntValue columnHeight;
    public static ForgeConfigSpec.DoubleValue passiveCooling;
    public static ForgeConfigSpec.DoubleValue passiveCoolingInner;
    public static ForgeConfigSpec.DoubleValue columnHeatFlow;
    public static ForgeConfigSpec.DoubleValue maxHeat;

    private RBMKConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("RBMK reactor column tuning").push("rbmk");

        columnHeight = builder
                .comment("Number of dummy segments above the core (total height = value + 1)")
                .defineInRange("columnHeight", 4, 1, 8);

        passiveCooling = builder
                .comment("Passive heat loss per tick for outer column surfaces")
                .defineInRange("passiveCooling", 2.5D, 0.0D, 1000.0D);

        passiveCoolingInner = builder
                .comment("Passive heat loss per tick for inner column surfaces")
                .defineInRange("passiveCoolingInner", 0.1D, 0.0D, 1000.0D);

        columnHeatFlow = builder
                .comment("Heat transfer factor between adjacent RBMK columns")
                .defineInRange("columnHeatFlow", 0.2D, 0.0D, 10.0D);

        maxHeat = builder
                .comment("Heat level at which meltdown behavior begins (when simulated)")
                .defineInRange("maxHeat", 1500.0D, 1.0D, 100000.0D);

        builder.pop();
    }
}
