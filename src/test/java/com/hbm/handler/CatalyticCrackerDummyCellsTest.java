package com.hbm.handler;

import com.hbm.blocks.DummyableMeta;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalyticCrackerDummyCellsTest {
    private static final int[] DIM = {0, 0, 3, 3, 2, 3};
    private static final int[] EXTRA_A = {8, -1, 3, -1, 2, 0};
    private static final int[] EXTRA_B = {13, 0, 0, 3, 2, 1};
    private static final int[] EXTRA_C = {14, -13, -1, 2, 1, 0};
    private static final int[] EXTRA_D = {3, -1, 2, 3, -1, 3};
    private static final int[][] PORTS = {
            {3, 1}, {3, -2}, {-3, 1}, {-3, -2},
            {2, 2}, {2, -3}, {-2, 2}, {-2, -3}
    };

    @Test
    void extraBoxesAddCellsAndEightPortsExist() {
        HashSet<String> cells = new HashSet<>();
        add(cells, DIM);
        add(cells, EXTRA_A);
        add(cells, EXTRA_B);
        add(cells, EXTRA_C);
        add(cells, EXTRA_D);
        cells.remove("0,0,0");
        assertTrue(cells.size() > MultiblockHandlerXR.cellCount(DIM, DummyableMeta.SOUTH));
        assertEquals(41, MultiblockHandlerXR.cellCount(DIM, DummyableMeta.SOUTH));
        int rot = DummyableMeta.rotateYClockwise(DummyableMeta.SOUTH);
        int dx = DummyableMeta.offsetX(DummyableMeta.SOUTH);
        int dz = DummyableMeta.offsetZ(DummyableMeta.SOUTH);
        int rx = DummyableMeta.offsetX(rot);
        int rz = DummyableMeta.offsetZ(rot);
        for (int[] port : PORTS) {
            int x = dx * port[0] + rx * port[1];
            int z = dz * port[0] + rz * port[1];
            assertTrue(cells.contains(x + ",0," + z), x + ",0," + z);
        }
    }

    private static void add(HashSet<String> cells, int[] dim) {
        List<MultiblockHandlerXR.Cell> list = MultiblockHandlerXR.dummyCells(
                0, 0, 0, dim, DummyableMeta.SOUTH);
        for (MultiblockHandlerXR.Cell cell : list) {
            cells.add(cell.x() + "," + cell.y() + "," + cell.z());
        }
    }
}
