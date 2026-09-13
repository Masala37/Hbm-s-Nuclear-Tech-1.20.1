package com.hbm.items.tool;

/**
 * 1.7 {@code ItemOilDetector} column checks: direct underfoot, then ±5 / ±10 and diagonals.
 * Scan floor is the 1.18 world bottom (1.7 used Y 5 because stone started there).
 */
public final class OilDetectorScan {
    public static final int SCAN_FLOOR = -60;
    public static final int FAR_SCAN_FLOOR = SCAN_FLOOR + 5;
    public enum Hit {
        NONE,
        NEARBY,
        DIRECT
    }

    @FunctionalInterface
    public interface OilProbe {
        boolean hasOil(int x, int y, int z);
    }

    private OilDetectorScan() {
    }

    public static Hit scan(OilProbe world, int x, int y, int z) {
        boolean direct = false;
        boolean oil = false;

        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x, i, z)) {
                direct = true;
            }
        }
        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x + 5, i, z)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x - 5, i, z)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x, i, z + 5)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x, i, z - 5)) {
                oil = true;
            }
        }

        for (int i = y + 15; i > FAR_SCAN_FLOOR; i--) {
            if (world.hasOil(x + 10, i, z)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > FAR_SCAN_FLOOR; i--) {
            if (world.hasOil(x - 10, i, z)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > FAR_SCAN_FLOOR; i--) {
            if (world.hasOil(x, i, z + 10)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > FAR_SCAN_FLOOR; i--) {
            if (world.hasOil(x, i, z - 10)) {
                oil = true;
            }
        }

        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x + 5, i, z + 5)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x - 5, i, z + 5)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x + 5, i, z - 5)) {
                oil = true;
            }
        }
        for (int i = y + 15; i > SCAN_FLOOR; i--) {
            if (world.hasOil(x - 5, i, z - 5)) {
                oil = true;
            }
        }

        if (direct) {
            return Hit.DIRECT;
        }
        if (oil) {
            return Hit.NEARBY;
        }
        return Hit.NONE;
    }
}
