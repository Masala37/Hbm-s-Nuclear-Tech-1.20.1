package com.hbm.blocks.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchPadOffsetsTest {

    @Test
    void packUnpackRoundTrip() {
        for (int signed = -1; signed <= 1; signed++) {
            int packed = LaunchPadOffsets.pack(signed);
            assertTrue(packed >= LaunchPadOffsets.PACKED_MIN);
            assertTrue(packed <= LaunchPadOffsets.PACKED_MAX);
            assertEquals(signed, LaunchPadOffsets.unpack(packed));
        }
    }

    @Test
    void coreIsPackedOneOne() {
        assertTrue(LaunchPadOffsets.isCore(1, 1));
        assertFalse(LaunchPadOffsets.isCore(0, 1));
        assertFalse(LaunchPadOffsets.isCore(1, 0));
        assertFalse(LaunchPadOffsets.isCore(2, 2));
    }

    @Test
    void cornersAreBothOffAxis() {
        assertTrue(LaunchPadOffsets.isCorner(0, 0));
        assertTrue(LaunchPadOffsets.isCorner(0, 2));
        assertTrue(LaunchPadOffsets.isCorner(2, 0));
        assertTrue(LaunchPadOffsets.isCorner(2, 2));
        assertFalse(LaunchPadOffsets.isCorner(1, 1));
        assertFalse(LaunchPadOffsets.isCorner(0, 1));
        assertFalse(LaunchPadOffsets.isCorner(1, 0));
        assertFalse(LaunchPadOffsets.isCorner(2, 1));
        assertFalse(LaunchPadOffsets.isCorner(1, 2));
    }

    @Test
    void nineCellsCoverTheGrid() {
        int cores = 0;
        int corners = 0;
        int edges = 0;
        for (int ox = 0; ox <= 2; ox++) {
            for (int oz = 0; oz <= 2; oz++) {
                if (LaunchPadOffsets.isCore(ox, oz)) {
                    cores++;
                } else if (LaunchPadOffsets.isCorner(ox, oz)) {
                    corners++;
                } else {
                    edges++;
                }
            }
        }
        assertEquals(1, cores);
        assertEquals(4, corners);
        assertEquals(4, edges);
    }

    @Test
    void corePosFromWestDummy() {
        int packedOx = LaunchPadOffsets.pack(-1);
        int packedOz = LaunchPadOffsets.pack(0);
        assertEquals(1, LaunchPadOffsets.coreDeltaX(packedOx));
        assertEquals(0, LaunchPadOffsets.coreDeltaZ(packedOz));
    }

    @Test
    void rotate90FourTimesIsIdentity() {
        for (int ox = 0; ox <= 2; ox++) {
            for (int oz = 0; oz <= 2; oz++) {
                int x = ox;
                int z = oz;
                for (int i = 0; i < 4; i++) {
                    int nx = LaunchPadOffsets.rotate90Ox(x, z);
                    int nz = LaunchPadOffsets.rotate90Oz(x, z);
                    x = nx;
                    z = nz;
                }
                assertEquals(ox, x);
                assertEquals(oz, z);
            }
        }
    }

    @Test
    void rotate180TwiceIsIdentity() {
        for (int ox = 0; ox <= 2; ox++) {
            assertEquals(ox, LaunchPadOffsets.rotate180Ox(LaunchPadOffsets.rotate180Ox(ox)));
        }
    }

    @Test
    void rotate90Then270IsIdentity() {
        int ox = LaunchPadOffsets.pack(-1);
        int oz = LaunchPadOffsets.pack(1);
        int x90 = LaunchPadOffsets.rotate90Ox(ox, oz);
        int z90 = LaunchPadOffsets.rotate90Oz(ox, oz);
        assertEquals(ox, LaunchPadOffsets.rotate270Ox(x90, z90));
        assertEquals(oz, LaunchPadOffsets.rotate270Oz(x90, z90));
    }

    @Test
    void mirrorsAreInvolutions() {
        for (int v = 0; v <= 2; v++) {
            assertEquals(v, LaunchPadOffsets.mirrorX(LaunchPadOffsets.mirrorX(v)));
            assertEquals(v, LaunchPadOffsets.mirrorZ(LaunchPadOffsets.mirrorZ(v)));
        }
    }
}
