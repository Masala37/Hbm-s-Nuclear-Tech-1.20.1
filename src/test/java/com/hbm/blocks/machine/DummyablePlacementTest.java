package com.hbm.blocks.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DummyablePlacementTest {
    @Test
    void safeRemFollowsPlaceAndDismantleFlags() {
        DummyablePlacement.end();
        DummyablePlacement.endDismantle();
        assertFalse(DummyablePlacement.safeRem());
        DummyablePlacement.begin();
        assertTrue(DummyablePlacement.placing());
        assertTrue(DummyablePlacement.safeRem());
        DummyablePlacement.end();
        DummyablePlacement.beginDismantle();
        assertTrue(DummyablePlacement.dismantling());
        assertTrue(DummyablePlacement.safeRem());
        DummyablePlacement.endDismantle();
        assertFalse(DummyablePlacement.safeRem());
    }
}
