package com.hbm.world.feature;

/**
 * Oblate-sphere test from 1.7.10 {@code MapGenBubble} (radius square is halved).
 */
public final class OilBubbleMath {
    private OilBubbleMath() {
    }

    public static double radiusSqr(double radius) {
        return (radius * radius) / 2.0D;
    }

    public static int neighborRange(int maxSize) {
        return (maxSize / 8) + 1;
    }

    /**
     * {@code x}/{@code z} are 1.7 relative coords ({@code xCoord + local}), {@code y} is
     * {@code yCoord - worldY}. Fuzzy offset is {@code 0} when not fuzzy.
     */
    public static boolean inside(int x, int y, int z, double radiusSqr, double fuzzyOffset) {
        double rSqr = (double) x * x + (double) z * z + (double) y * y * 3.0D - fuzzyOffset;
        return rSqr < radiusSqr;
    }

    public static double fuzzyOffset(double radiusSqr, double unitRandom) {
        return unitRandom * radiusSqr / 3.0D;
    }
}
