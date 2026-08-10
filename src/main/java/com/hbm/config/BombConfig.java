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
    public static ForgeConfigSpec.IntValue missileRadius;
    public static ForgeConfigSpec.IntValue tsarRadius;
    public static ForgeConfigSpec.IntValue fleijaRadius;
    public static ForgeConfigSpec.IntValue soliniumRadius;
    public static ForgeConfigSpec.IntValue prototypeRadius;
    public static ForgeConfigSpec.IntValue customRadius;
    public static ForgeConfigSpec.IntValue balefireRadius;
    public static ForgeConfigSpec.IntValue n2Radius;
    public static ForgeConfigSpec.IntValue fatmanRadius;
    public static ForgeConfigSpec.IntValue mk5;
    public static ForgeConfigSpec.IntValue blastSpeed;
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

        missileRadius = builder
                .comment("Radius of det_nuke / missile warhead charges")
                .defineInRange("missileRadius", 100, 1, 500);

        tsarRadius = builder
                .comment("Radius of Tsar Bomba when fully assembled")
                .defineInRange("tsarRadius", 500, 1, 1000);

        fleijaRadius = builder
                .comment("Radius of FLEIJA (MK3 fleija dig)")
                .defineInRange("fleijaRadius", 50, 1, 500);

        soliniumRadius = builder
                .comment("Radius of Solinium (MK3 solinium dig)")
                .defineInRange("soliniumRadius", 150, 1, 500);

        prototypeRadius = builder
                .comment("Radius of the Prototype nuke")
                .defineInRange("prototypeRadius", 150, 1, 500);

        customRadius = builder
                .comment("Fallback radius when Custom Nuke yield resolves empty (normally unused)")
                .defineInRange("customRadius", 100, 1, 500);

        balefireRadius = builder
                .comment("Radius of the Balefire Bomb dig (EntityBalefire)")
                .defineInRange("balefireRadius", 250, 1, 500);

        n2Radius = builder
                .comment("Radius of the N2 mine (MK5 dig without fallout)")
                .defineInRange("n2Radius", 200, 1, 500);

        fatmanRadius = builder
                .comment("Legacy Fat Man / crashed-balefire base radius (dud balefire uses 1.25x)")
                .defineInRange("fatmanRadius", 35, 1, 500);

        mk5 = builder
                .comment("Milliseconds of dig/cache work per tick for MK5 / Batched nuke rays (legacy default 50)")
                .defineInRange("mk5BlastTime", 50, 1, 1000);

        blastSpeed = builder
                .comment("Columns processed per MK3 fleija/solinium tick (increases each tick)")
                .defineInRange("blastSpeed", 1024, 1, 10000);

        falloutRange = builder
                .comment("Fallout radius as a percent of (blast length * 2.5)")
                .defineInRange("falloutRange", 100, 0, 200);

        fDelay = builder
                .comment("Ticks between fallout processing bursts")
                .defineInRange("fDelay", 4, 0, 40);

        builder.pop();
    }
}
