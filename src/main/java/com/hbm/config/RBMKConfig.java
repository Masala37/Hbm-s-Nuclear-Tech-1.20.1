package com.hbm.config;

public final class RBMKConfig {
    /** Number of dummy segments above the core (legacy default: 4 → 5 blocks total). */
    public static int columnHeight = 4;

    public static double passiveCooling = 2.5D;
    public static double passiveCoolingInner = 0.1D;
    public static double columnHeatFlow = 0.2D;
    public static double maxHeat = 1500D;

    private RBMKConfig() {
    }
}
