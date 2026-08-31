package com.hbm.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RadarRulesTest {

    @Test
    void altitudeRequiresY55() {
        assertFalse(RadarRules.altitudeOk(54));
        assertTrue(RadarRules.altitudeOk(55));
        assertTrue(RadarRules.altitudeOk(100));
    }

    @Test
    void scanVolumeUsesRangeAndHeightBuffer() {
        assertTrue(RadarRules.inScanVolume(0, 64, 0, 1000, 0, 95, 0));
        assertFalse(RadarRules.inScanVolume(0, 64, 0, 1000, 0, 94, 0));
        assertFalse(RadarRules.inScanVolume(0, 64, 0, 1000, 1001, 200, 0));
        assertTrue(RadarRules.inScanVolume(0, 64, 0, 1000, 1000, 200, 0));
        // Legacy uses entity pos vs (x+0.5, y, z+0.5) without flooring.
        assertTrue(RadarRules.inScanVolume(0, 64, 0, 1000, 0.0, 94.1, 0.0));
        assertFalse(RadarRules.inScanVolume(0, 64, 0, 1000, 0.0, 94.0, 0.0));
        assertTrue(RadarRules.inScanVolume(0, 64, 0, 1000, 1000.4, 200, 0));
        assertFalse(RadarRules.inScanVolume(0, 64, 0, 1000, 1000.6, 200, 0));
    }

    @Test
    void proximityRedstoneIsCloserHigher() {
        assertEquals(15, RadarRules.proximityPower(0, 0, 1000, 0, 0));
        int far = RadarRules.proximityPower(0, 0, 1000, 1000, 1000);
        assertTrue(far < 15);
        assertTrue(far >= 0);
        assertEquals(15, RadarRules.combineProximity(8, 15));
    }

    @Test
    void tierRedstoneIsBlipPlusOne() {
        assertEquals(1, RadarRules.tierPower(0));
        assertEquals(5, RadarRules.tierPower(4));
        assertEquals(15, RadarRules.tierPower(20));
        assertEquals(11, RadarRules.combineTier(4, 11));
    }
}
