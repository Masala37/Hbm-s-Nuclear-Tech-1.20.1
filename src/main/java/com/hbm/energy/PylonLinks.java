package com.hbm.energy;

/**
 * 1.7.10 {@code TileEntityPylonBase.canConnect} without tile entities.
 * 0 OK, 1 type mismatch, 2 same pylon, 3 too far.
 */
public final class PylonLinks {
    public static final int OK = 0;
    public static final int TYPE = 1;
    public static final int SAME = 2;
    public static final int FAR = 3;

    private PylonLinks() {
    }

    public static int canConnect(PylonConnectionType firstType, PylonConnectionType secondType,
                                 boolean samePylon, double mountDistance, double firstRange, double secondRange) {
        if (firstType != secondType) {
            return TYPE;
        }
        if (samePylon) {
            return SAME;
        }
        double len = Math.min(firstRange, secondRange);
        return len >= mountDistance ? OK : FAR;
    }
}
