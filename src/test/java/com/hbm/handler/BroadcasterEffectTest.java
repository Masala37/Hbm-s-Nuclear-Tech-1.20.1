package com.hbm.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcasterEffectTest {
    @Test
    void damageScalesLikeLegacyTileEntityBroadcaster() {
        assertEquals(10.0F, BroadcasterEffect.damageAt(0.0D), 0.0001F);
        assertEquals(5.0F, BroadcasterEffect.damageAt(7.5D), 0.0001F);
        assertEquals(0.0F, BroadcasterEffect.damageAt(15.0D), 0.0001F);
        assertEquals(0.0F, BroadcasterEffect.damageAt(15.01D), 0.0001F);
        assertEquals(0.0F, BroadcasterEffect.damageAt(25.0D), 0.0001F);
    }

    @Test
    void nauseaRangeAndRefreshMatchLegacy() {
        assertTrue(BroadcasterEffect.inNauseaRange(0.0D));
        assertTrue(BroadcasterEffect.inNauseaRange(25.0D));
        assertFalse(BroadcasterEffect.inNauseaRange(25.01D));
        assertTrue(BroadcasterEffect.shouldRefreshNausea(0));
        assertTrue(BroadcasterEffect.shouldRefreshNausea(99));
        assertFalse(BroadcasterEffect.shouldRefreshNausea(100));
    }

    @Test
    void trackIndexMatchesLegacySeededRandom() {
        assertEquals(new java.util.Random(1 + 2 + 3).nextInt(3) + 1, BroadcasterEffect.trackIndex(1, 2, 3));
        assertEquals("block.broadcast" + BroadcasterEffect.trackIndex(8, 64, -4),
                BroadcasterEffect.trackPath(8, 64, -4));
        int track = BroadcasterEffect.trackIndex(0, 0, 0);
        assertTrue(track >= 1 && track <= 3);
    }
}
