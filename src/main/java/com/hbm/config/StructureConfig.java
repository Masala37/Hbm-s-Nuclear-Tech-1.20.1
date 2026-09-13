package com.hbm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 1.7 {@code StructureConfig} knobs for NBT surface structures and the meteor dungeon.
 */
public final class StructureConfig {
    public static ForgeConfigSpec.ConfigValue<String> enableStructures;
    public static ForgeConfigSpec.IntValue structureMinChunks;
    public static ForgeConfigSpec.IntValue structureMaxChunks;
    public static ForgeConfigSpec.DoubleValue lootAmountFactor;
    public static ForgeConfigSpec.BooleanValue debugStructures;
    public static ForgeConfigSpec.BooleanValue enableRuins;
    public static ForgeConfigSpec.BooleanValue enableOceanStructures;

    public static ForgeConfigSpec.IntValue ruinsASpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsBSpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsCSpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsDSpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsESpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsFSpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsGSpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsHSpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsISpawnWeight;
    public static ForgeConfigSpec.IntValue ruinsJSpawnWeight;
    public static ForgeConfigSpec.IntValue plane1SpawnWeight;
    public static ForgeConfigSpec.IntValue plane2SpawnWeight;
    public static ForgeConfigSpec.IntValue desertShack1SpawnWeight;
    public static ForgeConfigSpec.IntValue desertShack2SpawnWeight;
    public static ForgeConfigSpec.IntValue desertShack3SpawnWeight;
    public static ForgeConfigSpec.IntValue laboratorySpawnWeight;
    public static ForgeConfigSpec.IntValue lighthouseSpawnWeight;
    public static ForgeConfigSpec.IntValue oilRigSpawnWeight;
    public static ForgeConfigSpec.IntValue broadcastingTowerSpawnWeight;
    public static ForgeConfigSpec.IntValue beachedPatrolSpawnWeight;
    public static ForgeConfigSpec.IntValue vertibirdSpawnWeight;
    public static ForgeConfigSpec.IntValue vertibirdCrashedSpawnWeight;
    public static ForgeConfigSpec.IntValue factorySpawnWeight;
    public static ForgeConfigSpec.IntValue radioSpawnWeight;
    public static ForgeConfigSpec.IntValue forestChemSpawnWeight;
    public static ForgeConfigSpec.IntValue forestPostSpawnWeight;
    public static ForgeConfigSpec.IntValue spireSpawnWeight;
    public static ForgeConfigSpec.IntValue craneSpawnWeight;
    public static ForgeConfigSpec.IntValue dishSpawnWeight;
    public static ForgeConfigSpec.IntValue aircraftCarrierSpawnWeight;
    public static ForgeConfigSpec.IntValue featuresSpawnWeight;
    public static ForgeConfigSpec.IntValue bunkerSpawnWeight;
    public static ForgeConfigSpec.IntValue plainsNullWeight;
    public static ForgeConfigSpec.IntValue oceanNullWeight;

    private StructureConfig() {
    }

    static void build(ForgeConfigSpec.Builder builder) {
        builder.comment("NBT structures (1.7 StructureConfig). Each spawn is /locate-able as hbm:<name>. Silo is MapGen inside the features bucket. Unported houses/bunker still occupy 1.7 rarity as empty cells.")
                .push("structures");

        enableStructures = builder
                .comment("Modern NTM structures: true, false, or flag (respects Generate Structures)")
                .define("enableStructures", "flag");

        structureMinChunks = builder
                .comment("Minimum non-zero distance between structures in chunks")
                .defineInRange("structureMinChunks", 4, 1, 256);

        structureMaxChunks = builder
                .comment("Maximum non-zero distance between structures in chunks")
                .defineInRange("structureMaxChunks", 16, 2, 256);

        lootAmountFactor = builder
                .comment("General factor for loot spawns in IInventories, not loot blocks")
                .defineInRange("lootAmountFactor", 1.0D, 0.0D, 64.0D);

        debugStructures = builder
                .comment("If enabled, wand_jigsaw / wand_loot blocks are left in place")
                .define("debugStructures", false);

        enableRuins = builder
                .comment("Toggle for ruin structures A through J")
                .define("enableRuins", true);

        enableOceanStructures = builder
                .comment("Toggle for aircraft carrier, oil rig, and lighthouse")
                .define("enableOceanStructures", true);

        spireSpawnWeight = weight(builder, "spireSpawnWeight", 2);
        vertibirdSpawnWeight = weight(builder, "vertibirdSpawnWeight", 6);
        vertibirdCrashedSpawnWeight = weight(builder, "crashedVertibirdSpawnWeight", 10);
        aircraftCarrierSpawnWeight = weight(builder, "aircraftCarrierSpawnWeight", 3);
        oilRigSpawnWeight = weight(builder, "oilRigSpawnWeight", 5);
        lighthouseSpawnWeight = weight(builder, "lighthouseSpawnWeight", 1);
        beachedPatrolSpawnWeight = weight(builder, "beachedPatrolSpawnWeight", 15);
        dishSpawnWeight = weight(builder, "dishSpawnWeight", 10);
        forestChemSpawnWeight = weight(builder, "forestChemSpawnWeight", 30);
        plane1SpawnWeight = weight(builder, "plane1SpawnWeight", 25);
        plane2SpawnWeight = weight(builder, "plane2SpawnWeight", 25);
        desertShack1SpawnWeight = weight(builder, "desertShack1SpawnWeight", 18);
        desertShack2SpawnWeight = weight(builder, "desertShack2SpawnWeight", 20);
        desertShack3SpawnWeight = weight(builder, "desertShack3SpawnWeight", 22);
        laboratorySpawnWeight = weight(builder, "laboratorySpawnWeight", 20);
        forestPostSpawnWeight = weight(builder, "forestPostSpawnWeight", 30);
        radioSpawnWeight = weight(builder, "radioSpawnWeight", 25);
        factorySpawnWeight = weight(builder, "factorySpawnWeight", 40);
        craneSpawnWeight = weight(builder, "craneSpawnWeight", 20);
        broadcastingTowerSpawnWeight = weight(builder, "broadcastingTowerSpawnWeight", 25);
        ruinsASpawnWeight = weight(builder, "ruinASpawnWeight", 10);
        ruinsBSpawnWeight = weight(builder, "ruinBSpawnWeight", 12);
        ruinsCSpawnWeight = weight(builder, "ruinCSpawnWeight", 12);
        ruinsDSpawnWeight = weight(builder, "ruinDSpawnWeight", 12);
        ruinsESpawnWeight = weight(builder, "ruinESpawnWeight", 12);
        ruinsFSpawnWeight = weight(builder, "ruinFSpawnWeight", 12);
        ruinsGSpawnWeight = weight(builder, "ruinGSpawnWeight", 12);
        ruinsHSpawnWeight = weight(builder, "ruinHSpawnWeight", 12);
        ruinsISpawnWeight = weight(builder, "ruinISpawnWeight", 12);
        ruinsJSpawnWeight = weight(builder, "ruinJSpawnWeight", 12);
        featuresSpawnWeight = weight(builder, "featuresSpawnWeight", 50);
        bunkerSpawnWeight = weight(builder, "bunkerSpawnWeight", 6);
        plainsNullWeight = weight(builder, "plainsNullWeight", 20);
        oceanNullWeight = weight(builder, "oceanNullWeight", 35);

        builder.pop();
    }

    private static ForgeConfigSpec.IntValue weight(ForgeConfigSpec.Builder builder, String name, int def) {
        return builder.defineInRange(name, def, 0, 10_000);
    }

    /** Legacy helper: true / false / flag → 1 / 0 / 2. */
    public static int parseFlag() {
        String value = enableStructures.get();
        if (value == null) {
            return 2;
        }
        return switch (value.toLowerCase()) {
            case "true", "1", "yes" -> 1;
            case "false", "0", "no" -> 0;
            default -> 2;
        };
    }

    public static int minChunks() {
        int min = structureMinChunks.get();
        int max = structureMaxChunks.get();
        if (min > max) {
            return 8;
        }
        return Math.max(1, min);
    }

    public static int maxChunks() {
        int min = structureMinChunks.get();
        int max = structureMaxChunks.get();
        if (min > max) {
            return 24;
        }
        return Math.max(minChunks() + 1, max);
    }
}
