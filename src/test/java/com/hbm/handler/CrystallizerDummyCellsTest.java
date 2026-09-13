package com.hbm.handler;

import com.hbm.blocks.DummyableMeta;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystallizerDummyCellsTest {
    private static final int[] DIM = {5, 0, 1, 1, 1, 1};

    @Test
    void footprintIsThreeByThreeBySixAndCornersExist() {
        assertEquals(53, MultiblockHandlerXR.cellCount(DIM, DummyableMeta.SOUTH));
        HashSet<String> cells = new HashSet<>();
        List<MultiblockHandlerXR.Cell> list = MultiblockHandlerXR.dummyCells(
                0, 0, 0, DIM, DummyableMeta.SOUTH);
        for (MultiblockHandlerXR.Cell cell : list) {
            cells.add(cell.x() + "," + cell.y() + "," + cell.z());
        }
        assertTrue(cells.contains("1,0,1"));
        assertTrue(cells.contains("1,0,-1"));
        assertTrue(cells.contains("-1,0,1"));
        assertTrue(cells.contains("-1,0,-1"));
    }
}
