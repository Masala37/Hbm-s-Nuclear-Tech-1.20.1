package com.hbm.sound;

/**
 * 1.7.10 {@code SoundLoopSiren.func}: volume 2 at the source, 0 at {@code range} blocks.
 */
public final class SirenFalloff {
    private SirenFalloff() {
    }

    public static float volumeAtDistance(float distance, float range) {
        if (range <= 0.0F) {
            return 0.0F;
        }
        return Math.max(0.0F, (distance / range) * -2.0F + 2.0F);
    }
}
