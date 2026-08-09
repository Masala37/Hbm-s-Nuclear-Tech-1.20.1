package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Slim port of legacy GeneralConfig feature flags needed by early systems.
 * Full category parity is deferred until those features are ported.
 */
public final class GeneralConfig {
    public static ForgeConfigSpec.BooleanValue enableDebugMode;
    public static ForgeConfigSpec.BooleanValue enableMycelium;
    public static ForgeConfigSpec.BooleanValue enablePlutoniumOre;
    public static ForgeConfigSpec.ConfigValue<String> enableDungeons;
    public static ForgeConfigSpec.BooleanValue enableMDOres;
    public static ForgeConfigSpec.BooleanValue enableMines;
    public static ForgeConfigSpec.BooleanValue enableRad;
    public static ForgeConfigSpec.BooleanValue enableGuns;
    public static ForgeConfigSpec.BooleanValue enableMOTD;

    private GeneralConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("General feature flags (ported subset)").push("general");

        enableDebugMode = builder
                .comment("Enable debugging mode")
                .define("enableDebugMode", false);

        enableMycelium = builder
                .comment("Allows glowing mycelium to spread")
                .define("enableMyceliumSpread", false);

        enablePlutoniumOre = builder
                .comment("Enables plutonium ore generation in the nether")
                .define("enablePlutoniumNetherOre", false);

        enableDungeons = builder
                .comment("Structure/dungeon generation: true, false, or flag (legacy structure flag)")
                .define("enableDungeons", "flag");

        enableMDOres = builder
                .comment("Allows NTM ores to generate in modded dimensions")
                .define("enableOresInModdedDimensions", true);

        enableMines = builder
                .comment("Allows landmines to generate")
                .define("enableLandmineSpawn", true);

        enableRad = builder
                .comment("Allows radiation hotspots to generate")
                .define("enableRadHotspotSpawn", true);

        enableGuns = builder
                .comment("If false, prevents new-system guns from firing")
                .define("enableGuns", true);

        enableMOTD = builder
                .comment("Shows the 'Loaded mod!' chat message and update notifications when joining a world")
                .define("enableMOTD", true);

        builder.pop();
    }

    /**
     * Legacy helper: true / false / flag → 1 / 0 / 2.
     */
    public static int parseStructureFlag() {
        String value = enableDungeons.get();
        if (value == null) {
            return 2;
        }
        return switch (value.toLowerCase()) {
            case "true", "1", "yes" -> 1;
            case "false", "0", "no" -> 0;
            default -> 2;
        };
    }
}
