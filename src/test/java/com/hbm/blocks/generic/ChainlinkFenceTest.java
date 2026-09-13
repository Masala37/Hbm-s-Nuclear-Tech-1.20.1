package com.hbm.blocks.generic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainlinkFenceTest {
    @Test
    void poleHiddenOnStraightRunsUnlessForced() {
        assertFalse(ChainlinkFence.showPole(false, false, true, false, true));
        assertFalse(ChainlinkFence.showPole(false, true, false, true, false));
        assertTrue(ChainlinkFence.showPole(true, false, true, false, true));
        assertTrue(ChainlinkFence.showPole(true, true, false, true, false));
    }

    @Test
    void poleShownOnEndsCornersAndIsolated() {
        assertTrue(ChainlinkFence.showPole(false, false, false, false, false));
        assertTrue(ChainlinkFence.showPole(false, true, false, false, false));
        assertTrue(ChainlinkFence.showPole(false, true, true, false, false));
        assertTrue(ChainlinkFence.showPole(false, true, true, true, false));
        assertTrue(ChainlinkFence.showPole(false, true, true, true, true));
    }

    @Test
    void collisionBoxesMatchLegacyFenceOverride() {
        assertTrue(ChainlinkFence.usesEastWestBox(false, false, false, false));
        assertFalse(ChainlinkFence.usesNorthSouthBox(false, false));

        assertTrue(ChainlinkFence.usesNorthSouthBox(true, true));
        assertFalse(ChainlinkFence.usesEastWestBox(true, false, true, false));

        assertTrue(ChainlinkFence.usesEastWestBox(false, true, false, true));
        assertFalse(ChainlinkFence.usesNorthSouthBox(false, false));

        assertTrue(ChainlinkFence.usesNorthSouthBox(true, false));
        assertTrue(ChainlinkFence.usesEastWestBox(true, true, false, false));
    }
}
