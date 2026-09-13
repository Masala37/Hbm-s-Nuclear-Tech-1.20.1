package com.hbm.handler;

import com.hbm.blocks.DummyableMeta;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PumpjackDummyCellsTest {
    @Test
    void walkwayBoxesAddEightCellsFacingSouth() {
        int rot = DummyableMeta.rotateYCounterClockwise(DummyableMeta.SOUTH);
        int ox = DummyableMeta.offsetX(rot) * 3;
        int oz = DummyableMeta.offsetZ(rot) * 3;
        HashSet<String> main = new HashSet<>();
        for (MultiblockHandlerXR.Cell cell : MultiblockHandlerXR.dummyCells(
                0, 0, 0, new int[]{3, 0, 0, 0, 0, 6}, DummyableMeta.SOUTH)) {
            main.add(pack(cell.x(), cell.y(), cell.z()));
        }
        List<MultiblockHandlerXR.Cell> a = MultiblockHandlerXR.dummyCells(
                ox, 0, oz, new int[]{0, 0, -1, 1, 1, 1}, DummyableMeta.SOUTH);
        List<MultiblockHandlerXR.Cell> b = MultiblockHandlerXR.dummyCells(
                ox, 0, oz, new int[]{0, 0, 1, -1, 2, 2}, DummyableMeta.SOUTH);
        HashSet<String> extra = new HashSet<>();
        for (MultiblockHandlerXR.Cell cell : a) {
            extra.add(pack(cell.x(), cell.y(), cell.z()));
        }
        for (MultiblockHandlerXR.Cell cell : b) {
            extra.add(pack(cell.x(), cell.y(), cell.z()));
        }
        extra.removeAll(main);
        extra.remove(pack(0, 0, 0));
        assertEquals(8, extra.size());
        assertTrue(a.stream().noneMatch(c -> c.x() == 0 && c.y() == 0 && c.z() == 0));
    }

    private static String pack(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
