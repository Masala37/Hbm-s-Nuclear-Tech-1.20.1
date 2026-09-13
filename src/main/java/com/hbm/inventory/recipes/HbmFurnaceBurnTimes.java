package com.hbm.inventory.recipes;

/**
 * 1.7.10 {@code FuelHandler} burn ticks. No Minecraft types so unit tests can load this.
 */
public final class HbmFurnaceBurnTimes {
    private static final int SINGLE = 200;

    private HbmFurnaceBurnTimes() {
    }

    public static int burnTime(String path) {
        if (path == null || path.isEmpty()) {
            return 0;
        }
        return switch (path) {
            case "solid_fuel" -> SINGLE * 16;
            case "solid_fuel_presto" -> SINGLE * 40;
            case "solid_fuel_presto_triplet" -> SINGLE * 200;
            case "solid_fuel_bf" -> SINGLE * 160;
            case "solid_fuel_presto_bf" -> SINGLE * 400;
            case "solid_fuel_presto_triplet_bf" -> SINGLE * 2000;
            case "rocket_fuel" -> SINGLE * 32;
            case "biomass" -> SINGLE * 2;
            case "biomass_compressed" -> SINGLE * 4;
            case "powder_coal" -> SINGLE * 8;
            case "scrap" -> SINGLE / 4;
            case "dust" -> SINGLE / 8;
            case "block_scrap" -> SINGLE * 2;
            case "powder_fire" -> 6400;
            case "lignite", "powder_lignite" -> 1200;
            case "coal_infernal" -> 4800;
            case "crystal_coal" -> 6400;
            case "powder_sawdust" -> SINGLE / 2;
            case "book_guide" -> SINGLE;
            default -> 0;
        };
    }
}
