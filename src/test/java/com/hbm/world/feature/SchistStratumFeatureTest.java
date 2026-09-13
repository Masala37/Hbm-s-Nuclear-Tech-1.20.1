package com.hbm.world.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchistStratumFeatureTest {
    @Test
    void bandRangeMatchesOneSevenTaper() {
        assertEquals(-1, SchistStratumMath.bandRange(SchistStratumMath.THRESHOLD));
        assertEquals(0, SchistStratumMath.bandRange(SchistStratumMath.THRESHOLD + 0.2D));
        assertEquals(3, SchistStratumMath.bandRange(SchistStratumMath.THRESHOLD + 1.0D));
        assertEquals(4, SchistStratumMath.bandRange(SchistStratumMath.THRESHOLD + 1.4D));
        assertEquals(2, SchistStratumMath.bandRange(SchistStratumMath.THRESHOLD + 2.0D));
        assertEquals(-1, SchistStratumMath.bandRange(SchistStratumMath.THRESHOLD + 3.0D));
    }

    @Test
    void mappedNoiseHitsLegacyThresholdWindow() {
        OreLayer3DNoise noise = new OreLayer3DNoise(1L);
        int hits = 0;
        int samples = 0;
        for (int x = 0; x < 2048; x += 16) {
            for (int z = 0; z < 2048; z += 16) {
                double n = SchistStratumMath.unitNoise(
                        noise.sample(x * SchistStratumMath.SCALE, z * SchistStratumMath.SCALE));
                if (SchistStratumMath.bandRange(n) >= 0) {
                    hits++;
                }
                samples++;
            }
        }
        double occupancy = hits / (double) samples;
        assertTrue(occupancy > 0.001D, "schist too rare: " + occupancy);
        assertTrue(occupancy < 0.40D, "schist too common: " + occupancy);
    }
}
