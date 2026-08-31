package com.hbm.blocks.machine;

/**
 * Nested placement / dismantle flag so dummy cells can survive while the
 * structure is still being written (BlockItem.canPlace requires canSurvive).
 */
public final class DummyablePlacement {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> DISMANTLING = ThreadLocal.withInitial(() -> false);

    private DummyablePlacement() {
    }

    public static void begin() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void end() {
        int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            DEPTH.set(0);
        } else {
            DEPTH.set(depth);
        }
    }

    public static boolean placing() {
        return DEPTH.get() > 0;
    }

    public static void beginDismantle() {
        DISMANTLING.set(true);
    }

    public static void endDismantle() {
        DISMANTLING.set(false);
    }

    public static boolean dismantling() {
        return DISMANTLING.get();
    }
}
