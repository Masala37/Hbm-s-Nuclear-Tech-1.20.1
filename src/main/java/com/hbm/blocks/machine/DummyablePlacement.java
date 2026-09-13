package com.hbm.blocks.machine;

/**
 * Nested placement / dismantle flag so dummy cells can survive while the
 * structure is still being written (BlockItem.canPlace requires canSurvive).
 */
public final class DummyablePlacement {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> DISMANTLING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Integer> SAFE_REM = ThreadLocal.withInitial(() -> 0);

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

    /** 1.7.10 {@code BlockDummyable.safeRem} while drilling out a dummy cell. */
    public static void beginSafeRem() {
        SAFE_REM.set(SAFE_REM.get() + 1);
    }

    public static void endSafeRem() {
        int depth = SAFE_REM.get() - 1;
        SAFE_REM.set(Math.max(0, depth));
    }

    public static boolean editing() {
        return SAFE_REM.get() > 0;
    }

    /** 1.7.10 {@code BlockDummyable.safeRem} — skip orphan checks while editing. */
    public static boolean safeRem() {
        return placing() || dismantling() || editing();
    }
}
