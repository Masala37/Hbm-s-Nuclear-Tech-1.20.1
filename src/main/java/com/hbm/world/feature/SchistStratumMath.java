package com.hbm.world.feature;

/**
 * 1.7 {@code SchistStratum} band math.
 * <p>
 * Legacy compared 4-octave Perlin to threshold 5, which never fires on a true
 * {@code [-2, 2]} domain. Use the same taper on that domain so gneiss stays a
 * thin Y≈30 lens instead of a solid sheet.
 */
public final class SchistStratumMath {
    public static final double SCALE = 0.01D;
    public static final double THRESHOLD = 1.15D;
    public static final int CENTER_Y = 30;

    private SchistStratumMath() {
    }

    public static double unitNoise(double scaledSample) {
        return scaledSample / OreLayer3DNoise.THRESHOLD_SCALE;
    }

    /** Same 1.7 {@code (n - threshold) * 3} / {@code 8 - range} taper. Negative means skip. */
    public static int bandRange(double n) {
        if (n <= THRESHOLD) {
            return -1;
        }
        int range = (int) ((n - THRESHOLD) * 3.0D);
        if (range > 4) {
            range = 8 - range;
        }
        return range;
    }
}
