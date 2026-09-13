package com.hbm.handler;

import com.hbm.handler.LaunchPadLoadingCycle.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LaunchPadLoadingCycleTest {
    private static final State HOME = new State(1.0F, 90.0F, 20, false, false, false);

    @Test
    void rotatesThenLowersThenDeploys() {
        State state = advance(HOME, 20, true, true, false);
        assertEquals(90.0F, state.erector());
        state = advance(state, 30, true, true, false);
        assertEquals(45.0F, state.erector());
        assertEquals(1.0F, state.lift());
        assertFalse(state.erected());
        state = advance(state, 50, true, true, false);
        assertEquals(0.0F, state.erector());
        assertEquals(1.0F, state.lift());
        state = advance(state, 41, true, true, false);
        assertEquals(0.0F, state.lift());
        assertTrue(state.scheduleErect());
        assertFalse(state.erected());
        state = advance(state, 11, true, true, false);
        assertTrue(state.erected());
        assertTrue(state.readyToLoad());
        assertFalse(state.scheduleErect());
    }

    @Test
    void heavyMissilesMoveAtHalfSpeed() {
        State state = advance(HOME, 50, true, true, true);
        assertEquals(67.5F, state.erector());
        assertFalse(state.erected());
        assertTrue(advance(state, 240, true, true, true).erected());
    }

    @Test
    void removingMissileCancelsPendingDeployment() {
        State pending = new State(0, 0, 12, false, true, true);
        State removed = LaunchPadLoadingCycle.tick(pending, false, true, false);
        assertFalse(removed.scheduleErect());
        assertFalse(removed.erected());
        assertFalse(removed.readyToLoad());
        State empty = advance(removed, 150, false, true, false);
        assertEquals(90, empty.erector());
        assertEquals(1, empty.lift());
        assertFalse(empty.erected());
        assertFalse(empty.readyToLoad());
    }

    @Test
    void replacingMissileMustReturnHomeBeforeReloading() {
        State replaced = new State(0, 0, 12, false, true, true).missileChanged();
        State returning = advance(replaced, 12, true, true, false);
        assertFalse(returning.erected());
        assertFalse(returning.scheduleErect());
        assertFalse(returning.readyToLoad());
        assertTrue(returning.erector() > 0);
        assertTrue(advance(returning, 350, true, true, false).erected());
    }

    @Test
    void powerLossPausesMotionAndPendingDeployment() {
        State pending = new State(0, 0, 12, false, true, true);
        assertEquals(pending, advance(pending, 100, true, false, false));
        assertTrue(advance(pending, 3, true, true, false).erected());
        State moving = new State(0.5F, 0, 0, false, true, false);
        assertEquals(moving, advance(moving, 100, true, false, false));
    }

    @Test
    void deployedMissileStaysReadyWhileMechanismReturnsHome() {
        State state = advance(HOME, 400, true, true, false);
        assertTrue(state.erected());
        assertTrue(state.readyToLoad());
        assertEquals(90, state.erector());
        assertEquals(1, state.lift());
        State reloaded = advance(state.missileChanged(), 50, true, true, false);
        assertFalse(reloaded.erected());
        assertEquals(45, reloaded.erector());
    }

    @Test
    void positionsStayBoundedAcrossRepeatedLoadingCycles() {
        State state = HOME;
        for (int tick = 0; tick < 5000; tick++) {
            boolean valid = tick % 500 < 350;
            if (tick % 500 == 0) {
                state = state.missileChanged();
            }
            state = LaunchPadLoadingCycle.tick(state, valid, tick % 7 != 0, tick % 1000 < 500);
            assertTrue(state.lift() >= 0 && state.lift() <= 1);
            assertTrue(state.erector() >= 0 && state.erector() <= 90);
            assertTrue(state.delay() >= 0);
            if (!valid) {
                assertFalse(state.erected());
                assertFalse(state.scheduleErect());
            }
        }
    }

    private static State advance(State state, int ticks, boolean valid, boolean powered, boolean slow) {
        for (int tick = 0; tick < ticks; tick++) {
            state = LaunchPadLoadingCycle.tick(state, valid, powered, slow);
        }
        return state;
    }
}
