package com.hbm.blocks.generic;

import com.hbm.blocks.DummyableMeta;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/**
 * 1.7 {@code DoorDecl.SILO_HATCH} / {@code SILO_HATCH_LARGE}: open ranges, part pose, sounds.
 */
public final class SiloHatchLogic {
    public static final byte STATE_CLOSED = 0;
    public static final byte STATE_OPEN = 1;
    public static final byte STATE_CLOSING = 2;
    public static final byte STATE_OPENING = 3;

    public static final int TIME_TO_OPEN = 60;
    public static final float SOUND_VOLUME = 2.0F;
    public static final float HATCH_ORIGIN_Y = 0.875F;

    private static final int[][] SMALL_RANGES = {
            {1, 0, 1, -3, 3, 0},
            {0, 0, 1, -3, 3, 0},
            {-1, 0, 1, -3, 3, 0}
    };
    private static final int[][] LARGE_RANGES = {
            {2, 0, 1, -3, 3, 0},
            {1, 0, 2, -5, 3, 0},
            {0, 0, 2, -5, 3, 0},
            {-1, 0, 2, -5, 3, 0},
            {-2, 0, 1, -3, 3, 0}
    };

    private SiloHatchLogic() {
    }

    public static int[][] ranges(boolean large) {
        return large ? LARGE_RANGES : SMALL_RANGES;
    }

    public static float hatchOriginZ(boolean large) {
        return large ? -2.875F : -1.875F;
    }

    public static float normTime(float time, float min, float max) {
        if (max == min) {
            return time >= min ? 1.0F : 0.0F;
        }
        return Mth.clamp((time - min) / (max - min), 0.0F, 1.0F);
    }

    public static float smoothstep(float t, float edge0, float edge1) {
        float x = Mth.clamp((t - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    public static float hatchLift(float openTicks) {
        return 0.25F * smoothstep(normTime(openTicks, 0.0F, 10.0F), 0.0F, 1.0F);
    }

    public static float hatchPitch(float openTicks) {
        return smoothstep(normTime(openTicks, 20.0F, 100.0F), 0.0F, 1.0F) * -240.0F;
    }

    public static float rangeOpenTime(int ticks) {
        return normTime(ticks, 20.0F, 20.0F);
    }

    public static float clientOpenTicks(byte state, long animStartMillis, long nowMillis) {
        float ms = (nowMillis - animStartMillis);
        float ticks = Mth.clamp(state == STATE_CLOSING || state == STATE_CLOSED
                ? TIME_TO_OPEN * 50.0F - ms
                : ms, 0.0F, TIME_TO_OPEN * 50.0F) * 0.02F;
        if (state == STATE_OPEN) {
            return TIME_TO_OPEN;
        }
        if (state == STATE_CLOSED) {
            return 0.0F;
        }
        return ticks;
    }

    /**
     * 1.7 {@code TileEntityDoorGeneric} range walk: {@code Rotation.getBlockRotation}
     * plus 180° when facing east/west.
     */
    public static BlockPos rotateDoorOffset(int facing, int x, int y, int z) {
        return switch (facing) {
            case DummyableMeta.SOUTH -> new BlockPos(-x, y, -z);
            case DummyableMeta.EAST -> new BlockPos(-z, y, x);
            case DummyableMeta.WEST -> new BlockPos(z, y, -x);
            default -> new BlockPos(x, y, z);
        };
    }

    public static BlockPos rangeCell(int facing, int[] range, int j, int k) {
        int sign = Integer.signum(range[3]);
        int addX;
        int addY;
        int addZ;
        switch (range[5]) {
            case 1 -> {
                addX = k;
                addY = sign * j;
                addZ = 0;
            }
            case 2 -> {
                addX = sign * j;
                addY = k;
                addZ = 0;
            }
            default -> {
                addX = 0;
                addY = k;
                addZ = sign * j;
            }
        }
        return rotateDoorOffset(facing, range[0] + addX, range[1] + addY, range[2] + addZ);
    }

    public static boolean moving(byte state) {
        return state == STATE_OPENING || state == STATE_CLOSING;
    }

    public static boolean passable(byte state) {
        return state != STATE_CLOSED;
    }
}
