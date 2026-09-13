package com.hbm.blocks;

/**
 * 1.7.10 dummyable metadata encoding.
 * 0–5 dummy facing, 6–11 extra (ports), 12–15 core rotation ({@code offset} + dir).
 */
public final class DummyableMeta {
    public static final int OFFSET = 10;
    public static final int EXTRA = 6;

    public static final int DOWN = 0;
    public static final int UP = 1;
    public static final int NORTH = 2;
    public static final int SOUTH = 3;
    public static final int WEST = 4;
    public static final int EAST = 5;

    private DummyableMeta() {
    }

    public static int stripExtra(int meta) {
        return meta >= EXTRA ? meta - EXTRA : meta;
    }

    public static boolean isExtra(int meta) {
        return meta >= EXTRA && meta <= EAST + EXTRA;
    }

    public static boolean isCore(int meta) {
        return stripExtra(meta) > EAST;
    }

    public static int coreMeta(int facingOrdinal) {
        return OFFSET + facingOrdinal;
    }

    public static int coreFacing(int meta) {
        return meta - OFFSET;
    }

    public static int makeExtra(int meta) {
        if (isCore(meta) || isExtra(meta)) {
            return meta;
        }
        return meta + EXTRA;
    }

    public static int removeExtra(int meta) {
        return isExtra(meta) ? meta - EXTRA : meta;
    }

    public static int dummyMeta(int coreX, int coreY, int coreZ, int x, int y, int z) {
        if (y < coreY) {
            return DOWN;
        }
        if (y > coreY) {
            return UP;
        }
        if (x < coreX) {
            return WEST;
        }
        if (x > coreX) {
            return EAST;
        }
        if (z < coreZ) {
            return NORTH;
        }
        return SOUTH;
    }

    public static int opposite(int dir) {
        return switch (dir) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
            default -> dir;
        };
    }

    public static int offsetX(int dir) {
        if (dir == WEST) {
            return -1;
        }
        if (dir == EAST) {
            return 1;
        }
        return 0;
    }

    public static int offsetY(int dir) {
        if (dir == DOWN) {
            return -1;
        }
        if (dir == UP) {
            return 1;
        }
        return 0;
    }

    public static int offsetZ(int dir) {
        if (dir == NORTH) {
            return -1;
        }
        if (dir == SOUTH) {
            return 1;
        }
        return 0;
    }

    /**
     * Step from a dummy toward the core (1.7 {@code findCore} walks {@code meta.opposite()}).
     */
    public static int towardCoreX(int meta) {
        return offsetX(opposite(stripExtra(meta)));
    }

    public static int towardCoreY(int meta) {
        return offsetY(opposite(stripExtra(meta)));
    }

    public static int towardCoreZ(int meta) {
        return offsetZ(opposite(stripExtra(meta)));
    }

    /**
     * 1.7.10 {@code onBlockPlacedBy} yaw buckets: 0 north, 1 east, 2 south, 3 west.
     */
    public static int facingFromYaw(float yRot) {
        int i = ((int) Math.floor(yRot * 4.0F / 360.0F + 0.5D)) & 3;
        return switch (i) {
            case 0 -> NORTH;
            case 1 -> EAST;
            case 2 -> SOUTH;
            default -> WEST;
        };
    }

    /**
     * 1.7.10 dummyable TESR yaw from core facing (meta − 10).
     */
    public static float tesrYaw(int facingOrdinal) {
        return switch (facingOrdinal) {
            case NORTH -> 90.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            default -> 0.0F;
        };
    }

    /**
     * 1.7 {@code RenderFEL} {@code switch(meta - offset)}: NORTH 0, SOUTH 180, WEST 90, EAST 270.
     */
    public static float felYaw(int facingOrdinal) {
        return switch (facingOrdinal) {
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }

    /**
     * 1.7 TESR {@code switch(meta - offset)} used by firebox and heat boiler:
     * SOUTH 0, EAST 90, NORTH 180, WEST 270.
     */
    public static float dummyableYaw(int facingOrdinal) {
        return switch (facingOrdinal) {
            case SOUTH -> 0.0F;
            case EAST -> 90.0F;
            case NORTH -> 180.0F;
            default -> 270.0F;
        };
    }

    /** 1.7 {@code ForgeDirection.getRotation(UP)}. */
    public static int rotateYClockwise(int dir) {
        return switch (dir) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> dir;
        };
    }

    /** 1.7 {@code ForgeDirection.getRotation(DOWN)}. */
    public static int rotateYCounterClockwise(int dir) {
        return switch (dir) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
            default -> dir;
        };
    }
}
