package com.hbm.sound;

/**
 * 1.7.10 {@code AudioDynamic.func} with volume 25 and range 25.
 */
public final class BroadcasterFalloff {
    public static final float VOLUME = 25.0F;
    public static final float RANGE = 25.0F;

    private BroadcasterFalloff() {
    }

    public static float volumeAtDistance(float distance) {
        if (RANGE <= 0.0F) {
            return 0.0F;
        }
        return Math.max(0.0F, (distance / RANGE) * -VOLUME + VOLUME);
    }
}
