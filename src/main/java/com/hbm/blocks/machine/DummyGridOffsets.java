package com.hbm.blocks.machine;

/**
 * Dummy-cell grid stored as packed {0 .. 2*radius} because {@code IntegerProperty}
 * requires min ≥ 0. World offsets are {-radius .. radius}.
 */
public final class DummyGridOffsets {
    public static final DummyGridOffsets SILO = new DummyGridOffsets(1);
    public static final DummyGridOffsets LARGE = new DummyGridOffsets(4);

    public final int radius;
    public final int packedMin;
    public final int packedMax;
    public final int core;

    private DummyGridOffsets(int radius) {
        this.radius = radius;
        this.packedMin = 0;
        this.packedMax = radius * 2;
        this.core = radius;
    }

    public int pack(int signed) {
        return signed + radius;
    }

    public int unpack(int packed) {
        return packed - radius;
    }

    public boolean isCore(int packedOx, int packedOz) {
        return packedOx == core && packedOz == core;
    }

    public boolean isCorner(int packedOx, int packedOz) {
        return packedOx != core && packedOz != core;
    }

    /**
     * 1.7.10 {@code makeExtra} ports: silo corners; large pad (±4,±2) and (±2,±4).
     */
    public boolean isPowerFluidPort(int packedOx, int packedOz) {
        int dx = Math.abs(unpack(packedOx));
        int dz = Math.abs(unpack(packedOz));
        if (radius == 1) {
            return dx == 1 && dz == 1;
        }
        return (dx == radius && dz == 2) || (dx == 2 && dz == radius);
    }

    public int rotate90Ox(int packedOx, int packedOz) {
        return pack(-unpack(packedOz));
    }

    public int rotate90Oz(int packedOx, int packedOz) {
        return pack(unpack(packedOx));
    }

    public int rotate180Ox(int packedOx) {
        return pack(-unpack(packedOx));
    }

    public int rotate180Oz(int packedOz) {
        return pack(-unpack(packedOz));
    }

    public int rotate270Ox(int packedOx, int packedOz) {
        return pack(unpack(packedOz));
    }

    public int rotate270Oz(int packedOx, int packedOz) {
        return pack(-unpack(packedOx));
    }

    public int mirrorZ(int packedOz) {
        return pack(-unpack(packedOz));
    }

    public int mirrorX(int packedOx) {
        return pack(-unpack(packedOx));
    }

    public int coreDeltaX(int packedOx) {
        return -unpack(packedOx);
    }

    public int coreDeltaZ(int packedOz) {
        return -unpack(packedOz);
    }

    /**
     * 1.7.10 dummyable TESR yaw from horizontal facing (meta − 10).
     * NORTH 90, SOUTH 270, WEST 180, EAST 0.
     */
    public static float dummyableYaw(net.minecraft.core.Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            default -> 0.0F;
        };
    }
}
