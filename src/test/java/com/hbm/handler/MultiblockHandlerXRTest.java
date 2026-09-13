package com.hbm.handler;

import com.hbm.blocks.DummyableMeta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiblockHandlerXRTest {
    @Test
    void southIsIdentity() {
        int[] dim = {1, 0, 2, 2, 1, 1};
        int[] rot = MultiblockHandlerXR.rotate(dim, DummyableMeta.SOUTH);
        assertEquals(dim, rot);
    }

    @Test
    void northSwapsNsAndWe() {
        int[] dim = {1, 0, 2, 3, 4, 5};
        int[] rot = MultiblockHandlerXR.rotate(dim, DummyableMeta.NORTH);
        assertEquals(1, rot[0]);
        assertEquals(0, rot[1]);
        assertEquals(3, rot[2]);
        assertEquals(2, rot[3]);
        assertEquals(5, rot[4]);
        assertEquals(4, rot[5]);
    }

    @Test
    void oneHighCubeHasTwentySixDummies() {
        int[] dim = {1, 1, 1, 1, 1, 1};
        List<MultiblockHandlerXR.Cell> cells = MultiblockHandlerXR.dummyCells(0, 0, 0, dim, DummyableMeta.SOUTH);
        assertEquals(26, cells.size());
        assertTrue(cells.stream().noneMatch(c -> c.x() == 0 && c.y() == 0 && c.z() == 0));
        MultiblockHandlerXR.Cell east = cells.stream().filter(c -> c.x() == 1 && c.y() == 0 && c.z() == 0).findFirst().orElseThrow();
        assertEquals(DummyableMeta.EAST, east.meta());
    }
}
