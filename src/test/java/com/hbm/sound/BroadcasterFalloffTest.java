package com.hbm.sound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BroadcasterFalloffTest {
    @Test
    void volumeFallsOffLikeLegacyAudioDynamic() {
        assertEquals(25.0F, BroadcasterFalloff.volumeAtDistance(0.0F), 0.0001F);
        assertEquals(12.5F, BroadcasterFalloff.volumeAtDistance(12.5F), 0.0001F);
        assertEquals(0.0F, BroadcasterFalloff.volumeAtDistance(25.0F), 0.0001F);
        assertEquals(0.0F, BroadcasterFalloff.volumeAtDistance(40.0F), 0.0001F);
    }
}
