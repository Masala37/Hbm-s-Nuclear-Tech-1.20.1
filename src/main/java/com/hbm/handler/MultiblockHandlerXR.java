package com.hbm.handler;

import com.hbm.blocks.DummyableMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code MultiblockHandlerXR} — rotate / enumerate dummyable boxes.
 * Dimensions are {@code [UP, DOWN, NORTH, SOUTH, WEST, EAST]} when facing south.
 */
public final class MultiblockHandlerXR {
    public static final int CELL_LIMIT = 2000;

    private MultiblockHandlerXR() {
    }

    public record Cell(int x, int y, int z, int meta) {
    }

    public static int[] rotate(int[] dim, int dir) {
        if (dim == null) {
            return null;
        }
        if (dim.length != 6) {
            return dim;
        }
        if (dir == DummyableMeta.SOUTH) {
            return dim;
        }
        if (dir == DummyableMeta.NORTH) {
            return new int[]{dim[0], dim[1], dim[3], dim[2], dim[5], dim[4]};
        }
        if (dir == DummyableMeta.EAST) {
            return new int[]{dim[0], dim[1], dim[5], dim[4], dim[2], dim[3]};
        }
        if (dir == DummyableMeta.WEST) {
            return new int[]{dim[0], dim[1], dim[4], dim[5], dim[3], dim[2]};
        }
        return dim;
    }

    /**
     * Dummy cells around the core (core itself omitted). Meta faces toward the core.
     */
    public static List<Cell> dummyCells(int coreX, int coreY, int coreZ, int[] dim, int dir) {
        List<Cell> cells = new ArrayList<>();
        if (dim == null || dim.length != 6) {
            return cells;
        }
        int[] rot = rotate(dim, dir);
        int count = 0;
        for (int a = coreX - rot[4]; a <= coreX + rot[5]; a++) {
            for (int b = coreY - rot[1]; b <= coreY + rot[0]; b++) {
                for (int c = coreZ - rot[2]; c <= coreZ + rot[3]; c++) {
                    if (a == coreX && b == coreY && c == coreZ) {
                        continue;
                    }
                    count++;
                    if (count > CELL_LIMIT) {
                        return cells;
                    }
                    cells.add(new Cell(a, b, c, DummyableMeta.dummyMeta(coreX, coreY, coreZ, a, b, c)));
                }
            }
        }
        return cells;
    }

    public static int cellCount(int[] dim, int dir) {
        return dummyCells(0, 0, 0, dim, dir).size();
    }
}
