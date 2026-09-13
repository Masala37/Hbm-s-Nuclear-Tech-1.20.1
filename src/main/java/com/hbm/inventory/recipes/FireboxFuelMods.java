package com.hbm.inventory.recipes;

/**
 * 1.7.10 {@code ModuleBurnTime} path bonuses. No Minecraft types so unit tests can load this.
 */
public final class FireboxFuelMods {
    public static final double LIGNITE_TIME = 1.25D;
    public static final double COAL_TIME = 1.25D;
    public static final double COKE_TIME = 1.25D;
    public static final double SOLID_TIME = 1.5D;
    public static final double ROCKET_TIME = 1.5D;
    public static final double BALEFIRE_TIME = 0.5D;

    public static final double LIGNITE_HEAT = 2.0D;
    public static final double COAL_HEAT = 2.0D;
    public static final double COKE_HEAT = 2.0D;
    public static final double SOLID_HEAT = 3.0D;
    public static final double ROCKET_HEAT = 5.0D;
    public static final double BALEFIRE_HEAT = 15.0D;

    private FireboxFuelMods() {
    }

    public static double[] fromPath(String path) {
        if (path.contains("solid_fuel") && (path.contains("_bf") || path.endsWith("bf"))) {
            return new double[]{BALEFIRE_TIME, BALEFIRE_HEAT};
        }
        if (path.contains("solid_fuel")) {
            return new double[]{SOLID_TIME, SOLID_HEAT};
        }
        if (path.contains("rocket_fuel")) {
            return new double[]{ROCKET_TIME, ROCKET_HEAT};
        }
        if (path.contains("coke")) {
            return new double[]{COKE_TIME, COKE_HEAT};
        }
        if (path.contains("lignite")) {
            return new double[]{LIGNITE_TIME, LIGNITE_HEAT};
        }
        // 1.7 ore dict is contains("Coal"); charcoal is not "Coal", and "charcoal" contains "coal".
        if (path.contains("charcoal") || path.contains("char_coal")) {
            return new double[]{1.0D, 1.0D};
        }
        if (path.contains("coal")) {
            return new double[]{COAL_TIME, COAL_HEAT};
        }
        return new double[]{1.0D, 1.0D};
    }
}
