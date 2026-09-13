package com.hbm.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PylonLinksTest {
    @Test
    void typeMismatch() {
        assertEquals(PylonLinks.TYPE, PylonLinks.canConnect(
                PylonConnectionType.SINGLE, PylonConnectionType.TRIPLE, false, 1.0D, 25.0D, 45.0D));
        assertEquals(PylonLinks.TYPE, PylonLinks.canConnect(
                PylonConnectionType.TRIPLE, PylonConnectionType.QUAD, false, 1.0D, 45.0D, 100.0D));
    }

    @Test
    void samePylon() {
        assertEquals(PylonLinks.SAME, PylonLinks.canConnect(
                PylonConnectionType.SINGLE, PylonConnectionType.SINGLE, true, 0.0D, 10.0D, 10.0D));
    }

    @Test
    void connectorRangeTenMetres() {
        assertEquals(PylonLinks.OK, PylonLinks.canConnect(
                PylonConnectionType.SINGLE, PylonConnectionType.SINGLE, false, 10.0D, 10.0D, 10.0D));
        assertEquals(PylonLinks.FAR, PylonLinks.canConnect(
                PylonConnectionType.SINGLE, PylonConnectionType.SINGLE, false, 10.01D, 10.0D, 10.0D));
    }

    @Test
    void smallPylonRangeTwentyFiveMetres() {
        assertEquals(PylonLinks.OK, PylonLinks.canConnect(
                PylonConnectionType.SINGLE, PylonConnectionType.SINGLE, false, 25.0D, 25.0D, 25.0D));
        assertEquals(PylonLinks.FAR, PylonLinks.canConnect(
                PylonConnectionType.SINGLE, PylonConnectionType.SINGLE, false, 25.01D, 25.0D, 25.0D));
    }

    @Test
    void mediumPylonRangeFortyFiveMetres() {
        assertEquals(PylonLinks.OK, PylonLinks.canConnect(
                PylonConnectionType.TRIPLE, PylonConnectionType.TRIPLE, false, 45.0D, 45.0D, 45.0D));
        assertEquals(PylonLinks.FAR, PylonLinks.canConnect(
                PylonConnectionType.TRIPLE, PylonConnectionType.TRIPLE, false, 45.01D, 45.0D, 45.0D));
    }

    @Test
    void largeAndSubstationUseShorterRange() {
        assertEquals(PylonLinks.OK, PylonLinks.canConnect(
                PylonConnectionType.QUAD, PylonConnectionType.QUAD, false, 20.0D, 100.0D, 20.0D));
        assertEquals(PylonLinks.FAR, PylonLinks.canConnect(
                PylonConnectionType.QUAD, PylonConnectionType.QUAD, false, 20.01D, 100.0D, 20.0D));
        assertEquals(PylonLinks.OK, PylonLinks.canConnect(
                PylonConnectionType.QUAD, PylonConnectionType.QUAD, false, 100.0D, 100.0D, 100.0D));
        assertEquals(PylonLinks.FAR, PylonLinks.canConnect(
                PylonConnectionType.QUAD, PylonConnectionType.QUAD, false, 100.01D, 100.0D, 100.0D));
    }
}
