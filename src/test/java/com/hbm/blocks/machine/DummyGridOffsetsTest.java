package com.hbm.blocks.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyGridOffsetsTest {

    @Test
    void largePacksSignedMinusFourToEight() {
        DummyGridOffsets grid = DummyGridOffsets.LARGE;
        assertEquals(0, grid.pack(-4));
        assertEquals(4, grid.pack(0));
        assertEquals(8, grid.pack(4));
        assertEquals(-4, grid.unpack(0));
        assertTrue(grid.isCore(4, 4));
    }

    @Test
    void largeRotate90FourTimesIsIdentity() {
        DummyGridOffsets grid = DummyGridOffsets.LARGE;
        for (int ox = 0; ox <= 8; ox++) {
            for (int oz = 0; oz <= 8; oz++) {
                int x = ox;
                int z = oz;
                for (int i = 0; i < 4; i++) {
                    int nx = grid.rotate90Ox(x, z);
                    int nz = grid.rotate90Oz(x, z);
                    x = nx;
                    z = nz;
                }
                assertEquals(ox, x);
                assertEquals(oz, z);
            }
        }
    }

    @Test
    void siloCornersArePowerFluidPorts() {
        DummyGridOffsets grid = DummyGridOffsets.SILO;
        assertTrue(grid.isPowerFluidPort(0, 0));
        assertTrue(grid.isPowerFluidPort(2, 2));
        assertFalse(grid.isPowerFluidPort(1, 0));
        assertFalse(grid.isPowerFluidPort(1, 1));
    }

    @Test
    void largeMakeExtraPortsMatchLegacy() {
        DummyGridOffsets grid = DummyGridOffsets.LARGE;
        assertTrue(grid.isPowerFluidPort(grid.pack(4), grid.pack(2)));
        assertTrue(grid.isPowerFluidPort(grid.pack(-4), grid.pack(-2)));
        assertTrue(grid.isPowerFluidPort(grid.pack(2), grid.pack(4)));
        assertFalse(grid.isPowerFluidPort(grid.pack(4), grid.pack(4)));
        assertFalse(grid.isPowerFluidPort(grid.core, grid.core));
    }
}
