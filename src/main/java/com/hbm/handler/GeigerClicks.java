package com.hbm.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * 1.7.10 geiger click selection ({@code ItemGeigerCounter} / {@code TileEntityGeiger}).
 */
public final class GeigerClicks {
    private GeigerClicks() {
    }

    public static List<Integer> tracksFor(float rads) {
        List<Integer> list = new ArrayList<>();
        if (rads < 1.0F) {
            list.add(0);
        }
        if (rads < 5.0F) {
            list.add(0);
        }
        if (rads < 10.0F) {
            list.add(1);
        }
        if (rads > 5.0F && rads < 15.0F) {
            list.add(2);
        }
        if (rads > 10.0F && rads < 20.0F) {
            list.add(3);
        }
        if (rads > 15.0F && rads < 25.0F) {
            list.add(4);
        }
        if (rads > 20.0F && rads < 30.0F) {
            list.add(5);
        }
        if (rads > 25.0F) {
            list.add(6);
        }
        return list;
    }

    /**
     * @param minActive handheld uses {@code 1e-5}; the block uses {@code 0} ({@code ticker > 0})
     * @return 0 = silent this pulse; 1–6 = {@code item.geigerN}
     */
    public static int pickTrack(float rads, float minActive, IntUnaryOperator nextInt) {
        if (rads > minActive) {
            List<Integer> list = tracksFor(rads);
            if (list.isEmpty()) {
                return 0;
            }
            return list.get(nextInt.applyAsInt(list.size()));
        }
        return nextInt.applyAsInt(50) == 0 ? 1 : 0;
    }

    public static int pickTrack(float rads, IntUnaryOperator nextInt) {
        return pickTrack(rads, 1.0E-5F, nextInt);
    }

    public static int analogSignal(float rads) {
        return Math.min((int) Math.ceil(rads / 5.0F), 15);
    }

    public static int hudBarWidth(double radiation, double maxRad, double scale) {
        return (int) Math.min(radiation / maxRad * scale, scale);
    }
}
