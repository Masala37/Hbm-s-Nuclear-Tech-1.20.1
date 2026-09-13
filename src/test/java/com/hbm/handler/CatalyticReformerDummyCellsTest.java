package com.hbm.handler;

import com.hbm.blocks.DummyableMeta;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalyticReformerDummyCellsTest {
    private static final int[] DIM = {2, 0, 1, 1, 2, 2};
    private static final int[] EXTRA_A = {3, -3, 1, 0, -1, 2};
    private static final int[] EXTRA_B = {6, -3, 1, 1, 2, 0};

    @Test
    void footprintIsFiveByThreeByThreePlusTowersAndSixPortsExist() {
        assertEquals(44, MultiblockHandlerXR.cellCount(DIM, DummyableMeta.SOUTH));
        HashSet<String> cells = new HashSet<>();
        add(cells, DIM);
        add(cells, EXTRA_A);
        add(cells, EXTRA_B);
        cells.remove("0,0,0");
        assertTrue(cells.size() > MultiblockHandlerXR.cellCount(DIM, DummyableMeta.SOUTH));
        assertTrue(cells.contains("1,0,1"));
        assertTrue(cells.contains("1,0,-1"));
        assertTrue(cells.contains("-1,0,1"));
        assertTrue(cells.contains("-1,0,-1"));
        int rot = DummyableMeta.rotateYClockwise(DummyableMeta.SOUTH);
        int rx = DummyableMeta.offsetX(rot);
        int rz = DummyableMeta.offsetZ(rot);
        assertTrue(cells.contains((rx * 2) + ",0," + (rz * 2)));
        assertTrue(cells.contains((-rx * 2) + ",0," + (-rz * 2)));
    }

    private static void add(HashSet<String> cells, int[] dim) {
        List<MultiblockHandlerXR.Cell> list = MultiblockHandlerXR.dummyCells(
                0, 0, 0, dim, DummyableMeta.SOUTH);
        for (MultiblockHandlerXR.Cell cell : list) {
            cells.add(cell.x() + "," + cell.y() + "," + cell.z());
        }
    }
}
