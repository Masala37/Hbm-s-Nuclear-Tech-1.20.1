package com.hbm.inventory.recipes;

import java.util.Locale;

/**
 * 1.7.10 {@code ModuleBurnTime} log×4 / wood×2 for {@code TileEntityMachineWoodBurner}.
 * No Minecraft types so unit tests can load this.
 * <p>
 * 1.7 ore dict: {@code name.startsWith("log")}, then {@code name.contains("Wood")}.
 * 1.20 item paths use {@code oak_log} / {@code oak_planks} instead.
 */
public final class WoodBurnerFuelMods {
    public static final double LOG_TIME = 4.0D;
    public static final double WOOD_TIME = 2.0D;

    private WoodBurnerFuelMods() {
    }

    public static double timeMod(String path) {
        String p = path == null ? "" : path.toLowerCase(Locale.ROOT);
        if (isLog(p)) {
            return LOG_TIME;
        }
        if (isWood(p)) {
            return WOOD_TIME;
        }
        return 1.0D;
    }

    static boolean isLog(String path) {
        return path.contains("_log") || path.startsWith("log") || path.endsWith("log");
    }

    static boolean isWood(String path) {
        return path.contains("wood") || path.contains("plank")
                || "stick".equals(path) || path.endsWith("_stick") || path.contains("sapling");
    }
}
