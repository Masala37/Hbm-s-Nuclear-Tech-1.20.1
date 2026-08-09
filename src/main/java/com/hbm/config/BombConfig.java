package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Nuke / bomb radii and dig budgets (legacy BombConfig subset).
 */
public final class BombConfig {
    public static ForgeConfigSpec.IntValue boyRadius;
    public static ForgeConfigSpec.IntValue manRadius;
    public static ForgeConfigSpec.IntValue mk5;

    private BombConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("Nuclear and conventional blast settings").push("bombs");

        boyRadius = builder
                .comment("Radius of Little Boy (crater length after MK5 scaling)")
                .defineInRange("boyRadius", 120, 1, 500);

        manRadius = builder
                .comment("Radius of Fat Man (crater length after MK5 scaling)")
                .defineInRange("manRadius", 175, 1, 500);

        mk5 = builder
                .comment("Milliseconds of dig work per tick for MK5 / Batched nuke rays")
                .defineInRange("mk5BlastTime", 50, 1, 1000);

        builder.pop();
    }
}
