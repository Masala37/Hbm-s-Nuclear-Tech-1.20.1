package com.hbm.handler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GeigerClicksTest {
    @Test
    void tracksMatchLegacyItemGeigerCounter() {
        assertEquals(List.of(0, 0, 1), GeigerClicks.tracksFor(0.5F));
        assertEquals(List.of(0, 1), GeigerClicks.tracksFor(3.0F));
        assertEquals(List.of(1, 2), GeigerClicks.tracksFor(7.0F));
        assertEquals(List.of(2, 3), GeigerClicks.tracksFor(12.0F));
        assertEquals(List.of(3, 4), GeigerClicks.tracksFor(17.0F));
        assertEquals(List.of(4, 5), GeigerClicks.tracksFor(22.0F));
        assertEquals(List.of(5, 6), GeigerClicks.tracksFor(27.0F));
        assertEquals(List.of(6), GeigerClicks.tracksFor(40.0F));
    }

    @Test
    void analogSignalMatchesLegacyComparator() {
        assertEquals(0, GeigerClicks.analogSignal(0.0F));
        assertEquals(1, GeigerClicks.analogSignal(0.1F));
        assertEquals(1, GeigerClicks.analogSignal(5.0F));
        assertEquals(2, GeigerClicks.analogSignal(5.1F));
        assertEquals(15, GeigerClicks.analogSignal(75.0F));
        assertEquals(15, GeigerClicks.analogSignal(100.0F));
    }

    @Test
    void hudBarScalesLikeLegacyOverlay() {
        assertEquals(0, GeigerClicks.hudBarWidth(0.0D, 1000.0D, 74.0D));
        assertEquals(37, GeigerClicks.hudBarWidth(500.0D, 1000.0D, 74.0D));
        assertEquals(74, GeigerClicks.hudBarWidth(1000.0D, 1000.0D, 74.0D));
        assertEquals(74, GeigerClicks.hudBarWidth(2000.0D, 1000.0D, 74.0D));
    }

    @Test
    void idleClickIsRareGeiger1() {
        assertEquals(1, GeigerClicks.pickTrack(0.0F, bound -> 0));
        assertEquals(0, GeigerClicks.pickTrack(0.0F, bound -> bound - 1));
        assertEquals(6, GeigerClicks.pickTrack(40.0F, bound -> 0));
        assertFalse(GeigerClicks.tracksFor(0.0F).contains(6));
        assertEquals(1, GeigerClicks.pickTrack(0.0F, 0.0F, bound -> 0));
        assertEquals(0, GeigerClicks.pickTrack(1.0E-6F, 0.0F, bound -> 0));
    }
}
