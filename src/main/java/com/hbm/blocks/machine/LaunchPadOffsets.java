package com.hbm.blocks.machine;

/**
 * 3×3 launch-pad dummy cells. World offsets are {-1, 0, 1}; Minecraft
 * {@code IntegerProperty} only allows min ≥ 0, so they are stored as {0, 1, 2}.
 */
public final class LaunchPadOffsets {
    public static final int PACKED_MIN = DummyGridOffsets.SILO.packedMin;
    public static final int PACKED_MAX = DummyGridOffsets.SILO.packedMax;
    public static final int CORE = DummyGridOffsets.SILO.core;

    private LaunchPadOffsets() {
    }

    public static int pack(int signed) {
        return DummyGridOffsets.SILO.pack(signed);
    }

    public static int unpack(int packed) {
        return DummyGridOffsets.SILO.unpack(packed);
    }

    public static boolean isCore(int packedOx, int packedOz) {
        return DummyGridOffsets.SILO.isCore(packedOx, packedOz);
    }

    public static boolean isCorner(int packedOx, int packedOz) {
        return DummyGridOffsets.SILO.isCorner(packedOx, packedOz);
    }

    /** CLOCKWISE_90: (x, z) → (−z, x). */
    public static int rotate90Ox(int packedOx, int packedOz) {
        return DummyGridOffsets.SILO.rotate90Ox(packedOx, packedOz);
    }

    public static int rotate90Oz(int packedOx, int packedOz) {
        return DummyGridOffsets.SILO.rotate90Oz(packedOx, packedOz);
    }

    /** CLOCKWISE_180: (x, z) → (−x, −z). */
    public static int rotate180Ox(int packedOx) {
        return DummyGridOffsets.SILO.rotate180Ox(packedOx);
    }

    public static int rotate180Oz(int packedOz) {
        return DummyGridOffsets.SILO.rotate180Oz(packedOz);
    }

    /** COUNTERCLOCKWISE_90: (x, z) → (z, −x). */
    public static int rotate270Ox(int packedOx, int packedOz) {
        return DummyGridOffsets.SILO.rotate270Ox(packedOx, packedOz);
    }

    public static int rotate270Oz(int packedOx, int packedOz) {
        return DummyGridOffsets.SILO.rotate270Oz(packedOx, packedOz);
    }

    /** LEFT_RIGHT — flip Z. */
    public static int mirrorZ(int packedOz) {
        return DummyGridOffsets.SILO.mirrorZ(packedOz);
    }

    /** FRONT_BACK — flip X. */
    public static int mirrorX(int packedOx) {
        return DummyGridOffsets.SILO.mirrorX(packedOx);
    }

    public static int coreDeltaX(int packedOx) {
        return DummyGridOffsets.SILO.coreDeltaX(packedOx);
    }

    public static int coreDeltaZ(int packedOz) {
        return DummyGridOffsets.SILO.coreDeltaZ(packedOz);
    }
}
