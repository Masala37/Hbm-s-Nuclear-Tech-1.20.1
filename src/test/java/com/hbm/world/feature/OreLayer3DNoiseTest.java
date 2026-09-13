package com.hbm.world.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OreLayer3DNoiseTest {
    @Test
    void hematiteThresholdProducesLargeRareBlobs() {
        OreLayer3DNoise noiseX = new OreLayer3DNoise(101L);
        OreLayer3DNoise noiseY = new OreLayer3DNoise(102L);
        int hits = 0;
        int samples = 0;
        double scaleH = 0.04D;
        double scaleV = 0.25D;
        double threshold = 230.0D;
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                double ny = noiseY.sample(x * scaleH, z * scaleH);
                for (int y = 6; y <= 64; y++) {
                    double nx = noiseX.sample(y * scaleV, z * scaleH);
                    double nz = noiseX.sample(y * scaleV, x * scaleH);
                    if (OreLayer3DNoise.inside(nx, ny, nz, threshold)) {
                        hits++;
                    }
                    samples++;
                }
            }
        }
        double occupancy = hits / (double) samples;
        assertTrue(occupancy > 0.001D, "hematite occupancy too low: " + occupancy);
        assertTrue(occupancy < 0.40D, "hematite occupancy too high: " + occupancy);
    }

    @Test
    void negativeWorldCoordsDoNotThrow() {
        OreLayer3DNoise noiseX = new OreLayer3DNoise(101L);
        OreLayer3DNoise noiseY = new OreLayer3DNoise(102L);
        double scaleH = 0.04D;
        double scaleV = 0.25D;
        for (int x = -48; x < -16; x++) {
            for (int z = -48; z < -16; z++) {
                double ny = noiseY.sample(x * scaleH, z * scaleH);
                for (int y = OreLayer3DNoise.MAX_Y; y >= OreLayer3DNoise.MIN_Y_EXCLUSIVE + 1; y--) {
                    double nx = noiseX.sample(y * scaleV, z * scaleH);
                    double nz = noiseX.sample(y * scaleV, x * scaleH);
                    OreLayer3DNoise.inside(nx, ny, nz, 230.0D);
                }
            }
        }
    }
}
