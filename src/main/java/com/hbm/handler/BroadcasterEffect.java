package com.hbm.handler;

import java.util.Random;

/**
 * 1.7.10 {@code TileEntityBroadcaster} nausea/damage ranges and the per-position loop track.
 */
public final class BroadcasterEffect {
    public static final double NAUSEA_RANGE = 25.0D;
    public static final double DAMAGE_RANGE = 15.0D;
    public static final int NAUSEA_DURATION = 300;
    public static final int NAUSEA_REFRESH_BELOW = 100;

    private BroadcasterEffect() {
    }

    public static boolean inNauseaRange(double distance) {
        return distance <= NAUSEA_RANGE;
    }

    public static float damageAt(double distance) {
        if (distance > DAMAGE_RANGE) {
            return 0.0F;
        }
        return (float) ((DAMAGE_RANGE - distance) / DAMAGE_RANGE * 10.0D);
    }

    public static boolean shouldRefreshNausea(int remainingDuration) {
        return remainingDuration < NAUSEA_REFRESH_BELOW;
    }

    /** 1.7.10 {@code new Random(x + y + z).nextInt(3) + 1} → broadcast1/2/3. */
    public static int trackIndex(int x, int y, int z) {
        return new Random(x + y + z).nextInt(3) + 1;
    }

    public static String trackPath(int x, int y, int z) {
        return "block.broadcast" + trackIndex(x, y, z);
    }
}
