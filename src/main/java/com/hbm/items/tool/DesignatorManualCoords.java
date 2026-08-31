package com.hbm.items.tool;

public final class DesignatorManualCoords {
    public static final String TAG_X = "xCoord";
    public static final String TAG_Z = "zCoord";

    private DesignatorManualCoords() {
    }

    public static int next(int current, int operator, int value, int playerCoord) {
        if (operator == 2) {
            return playerCoord;
        }
        if (operator == 0) {
            return current + value;
        }
        if (operator == 1) {
            return current - value;
        }
        return current;
    }
}
