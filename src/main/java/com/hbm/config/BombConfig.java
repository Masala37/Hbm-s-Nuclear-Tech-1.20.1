package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Nuke / bomb radii and dig budgets (legacy BombConfig subset).
 */
public final class BombConfig {
    public static ForgeConfigSpec.IntValue gadgetRadius;
    public static ForgeConfigSpec.IntValue boyRadius;
    public static ForgeConfigSpec.IntValue manRadius;
    public static ForgeConfigSpec.IntValue mikeRadius;
    public static ForgeConfigSpec.IntValue mk5;
    public static ForgeConfigSpec.IntValue falloutRange;
    public static ForgeConfigSpec.IntValue fDelay;

    private BombConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("Nuclear and conventional blast settings").push("bombs");

        gadgetRadius = builder
                .comment("Radius of The Gadget (crater length after MK5 scaling)")
                .defineInRange("gadgetRadius", 150, 1, 500);

        boyRadius = builder
                .comment("Radius of Little Boy (crater length after MK5 scaling)")
                .defineInRange("boyRadius", 120, 1, 500);

        manRadius = builder
                .comment("Radius of Fat Man (crater length after MK5 scaling)")
                .defineInRange("manRadius", 175, 1, 500);

        mikeRadius = builder
                .comment("Radius of Ivy Mike when fully assembled (crater length after MK5 scaling)")
                .defineInRange("mikeRadius", 250, 1, 500);

        mk5 = builder
                .comment("Milliseconds of dig work per tick for MK5 / Batched nuke rays")
                .defineInRange("mk5BlastTime", 50, 1, 1000);

        falloutRange = builder
                .comment("Fallout radius as a percent of (blast length * 2.5)")
                .defineInRange("falloutRange", 100, 0, 200);

        fDelay = builder
                .comment("Ticks between fallout processing bursts")
                .defineInRange("fDelay", 4, 0, 40);

        builder.pop();
    }
}
