package com.hbm.sound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SirenFalloffTest {
    @Test
    void volumeFallsOffLikeLegacySoundLoopSiren() {
        assertEquals(2.0F, SirenFalloff.volumeAtDistance(0.0F, 100.0F), 0.0001F);
        assertEquals(1.0F, SirenFalloff.volumeAtDistance(50.0F, 100.0F), 0.0001F);
        assertEquals(0.0F, SirenFalloff.volumeAtDistance(100.0F, 100.0F), 0.0001F);
        assertEquals(0.0F, SirenFalloff.volumeAtDistance(200.0F, 100.0F), 0.0001F);
        assertEquals(0.0F, SirenFalloff.volumeAtDistance(10.0F, 0.0F), 0.0001F);
    }
}
