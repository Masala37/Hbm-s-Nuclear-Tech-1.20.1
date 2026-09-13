package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Worldgen knobs (legacy WorldConfig subset).
 */
public final class WorldConfig {
    public static ForgeConfigSpec.IntValue dudStructure;
    public static ForgeConfigSpec.IntValue broadcaster;
    public static ForgeConfigSpec.IntValue oilSpawn;
    public static ForgeConfigSpec.IntValue gasbubbleSpawn;
    public static ForgeConfigSpec.IntValue explosivebubbleSpawn;
    public static ForgeConfigSpec.BooleanValue enableHematite;
    public static ForgeConfigSpec.BooleanValue enableMalachite;
    public static ForgeConfigSpec.BooleanValue enableBauxite;

    public static ForgeConfigSpec.BooleanValue enableCraterBiomes;
    public static ForgeConfigSpec.DoubleValue craterBiomeRad;
    public static ForgeConfigSpec.DoubleValue craterBiomeInnerRad;
    public static ForgeConfigSpec.DoubleValue craterBiomeOuterRad;
    public static ForgeConfigSpec.DoubleValue craterBiomeWaterMult;

    private WorldConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("World generation (ported subset)").push("world");

        dudStructure = builder
                .comment("Spawn a crashed-bomb dud on average every nTH chunk (0 = disabled)")
                .defineInRange("dudStructure", 500, 0, 100000);

        broadcaster = builder
                .comment("Spawn a corrupted broadcaster on average every nTH chunk (0 = disabled)")
                .defineInRange("broadcaster", 5000, 0, 100000);

        oilSpawn = builder
                .comment("Spawns an oil bubble every nTH chunk (0 = disabled). Deserts roll 3× as often.")
                .defineInRange("oilSpawn", 100, 0, 100000);

        gasbubbleSpawn = builder
                .comment("Spawns a flammable gas bubble every nTH chunk (0 = disabled)")
                .defineInRange("gasbubbleSpawn", 12, 0, 100000);

        explosivebubbleSpawn = builder
                .comment("Spawns an explosive gas bubble every nTH chunk (0 = disabled)")
                .defineInRange("explosivebubbleSpawn", 0, 0, 100000);

        enableHematite = builder
                .comment("Toggles hematite deposits")
                .define("enableHematite", true);

        enableMalachite = builder
                .comment("Toggles malachite deposits")
                .define("enableMalachite", true);

        enableBauxite = builder
                .comment("Toggles bauxite deposits")
                .define("enableBauxite", true);

        builder.pop();

        builder.comment("Crater biomes from nuclear fallout (resource-location biomes, no numeric IDs)").push("biomes");

        enableCraterBiomes = builder
                .comment("Enables the biome change caused by nuclear explosions")
                .define("enableCraterBiomes", true);

        craterBiomeRad = builder
                .comment("RAD/s for the crater biome")
                .defineInRange("craterBiomeRad", 5.0D, 0.0D, Double.MAX_VALUE);

        craterBiomeInnerRad = builder
                .comment("RAD/s for the inner crater biome")
                .defineInRange("craterBiomeInnerRad", 25.0D, 0.0D, Double.MAX_VALUE);

        craterBiomeOuterRad = builder
                .comment("RAD/s for the outer crater biome")
                .defineInRange("craterBiomeOuterRad", 0.5D, 0.0D, Double.MAX_VALUE);

        craterBiomeWaterMult = builder
                .comment("Multiplier for RAD/s in crater biomes when in water")
                .defineInRange("craterBiomeWaterMult", 5.0D, 0.0D, Double.MAX_VALUE);

        builder.pop();
    }
}
