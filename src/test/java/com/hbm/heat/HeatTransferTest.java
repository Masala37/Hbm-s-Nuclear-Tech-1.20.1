package com.hbm.heat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeatTransferTest {
    @Test
    void boilerDiffusionPullsTenPercentRoundedUp() {
        HeatBuffer firebox = new HeatBuffer(100_000);
        firebox.setHeat(1000);
        int heat = HeatTransfer.pull(firebox, 0, 100_000);
        assertEquals(100, heat);
        assertEquals(900, firebox.getHeatStored());
    }

    @Test
    void missingSourceDecaysHeat() {
        assertEquals(0, HeatTransfer.pull(null, 0, 1000));
        assertEquals(99, HeatTransfer.pull(null, 100, 1000));
        assertEquals(1, HeatTransfer.pull(null, 2, 1000));
    }

    @Test
    void equalSourceLeavesHeatAlone() {
        HeatBuffer source = new HeatBuffer(100_000);
        source.setHeat(500);
        assertEquals(500, HeatTransfer.pull(source, 500, 100_000));
        assertEquals(500, source.getHeatStored());
    }

    @Test
    void colderSourceDecaysLikeMissingSource() {
        HeatBuffer source = new HeatBuffer(100_000);
        source.setHeat(10);
        assertEquals(99, HeatTransfer.pull(source, 100, 100_000));
        assertEquals(10, source.getHeatStored());
    }

    @Test
    void useUpHeatDoesNotGoNegative() {
        HeatBuffer buf = new HeatBuffer(50);
        buf.setHeat(10);
        buf.useUpHeat(50);
        assertEquals(0, buf.getHeatStored());
    }
}
