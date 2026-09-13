package com.hbm.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyConnectTest {
    @Test
    void assemblyExtraIsNotSkippedAsCable() {
        assertFalse(EnergyConnect.canTraverseFace(false, false, false));
        assertTrue(EnergyConnect.canTraverseFace(true, false, false));
        assertFalse(EnergyConnect.isActiveConductor(null));
    }

    @Test
    void connectorOnlyJoinsListedFaces() {
        assertTrue(EnergyConnect.canTraverseFace(true, true, true));
        assertFalse(EnergyConnect.canTraverseFace(true, true, false));
        assertFalse(EnergyConnect.canTraverseFace(false, true, true));
    }
}
