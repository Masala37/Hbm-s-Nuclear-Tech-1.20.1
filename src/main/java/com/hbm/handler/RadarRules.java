package com.hbm.handler;

/**
 * Pure radar scan / redstone math (1.7.10 {@code TileEntityMachineRadarNT}).
 */
public final class RadarRules {
    public static final int RANGE = 1_000;
    public static final int RANGE_LARGE = 3_000;
    public static final int BUFFER = 30;
    public static final int ALTITUDE = 55;
    public static final int MAX_POWER = 100_000;
    public static final int CONSUMPTION = 500;
    public static final int PING_INTERVAL = 80;
    public static final int MAP_SIZE = 40_000;
    public static final int CHUNK_LOAD_CAP = 10;

    private RadarRules() {
    }

    public static boolean altitudeOk(int radarY) {
        return radarY >= ALTITUDE;
    }

    public static boolean inScanVolume(double radarX, double radarY, double radarZ, int range,
                                       double entityX, double entityY, double entityZ) {
        return Math.abs(entityX - (radarX + 0.5D)) <= range
                && Math.abs(entityZ - (radarZ + 0.5D)) <= range
                && entityY - radarY > BUFFER;
    }

    public static int proximityPower(int radarX, int radarZ, int range, int entryX, int entryZ) {
        double maxRange = range * Math.sqrt(2.0D);
        if (maxRange <= 0.0D) {
            return 0;
        }
        double dist = Math.sqrt(Math.pow(entryX - radarX, 2) + Math.pow(entryZ - radarZ, 2));
        int power = 15 - (int) Math.floor(dist / maxRange * 15);
        return Math.max(0, Math.min(15, power));
    }

    public static int tierPower(int blipLevel) {
        return Math.max(0, Math.min(15, blipLevel + 1));
    }

    public static int combineProximity(int current, int next) {
        return Math.max(current, next);
    }

    public static int combineTier(int current, int next) {
        return Math.max(current, next);
    }
}
