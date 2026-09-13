package com.hbm.fluid;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidNetTest {
    @Test
    void movesMinOfSupplyAndDemand() {
        FluidNet.BufferTank src = new FluidNet.BufferTank(1000, 1000);
        FluidNet.BufferTank dst = new FluidNet.BufferTank(0, 400);
        int moved = FluidNet.transfer(List.of(src), List.of(dst));
        assertEquals(400, moved);
        assertEquals(600, src.amount);
        assertEquals(400, dst.amount);
    }

    @Test
    void splitsAcrossReceivers() {
        FluidNet.BufferTank src = new FluidNet.BufferTank(100, 100);
        FluidNet.BufferTank a = new FluidNet.BufferTank(0, 100);
        FluidNet.BufferTank b = new FluidNet.BufferTank(0, 100);
        FluidNet.transfer(List.of(src), List.of(a, b));
        assertEquals(50, a.amount);
        assertEquals(50, b.amount);
        assertEquals(0, src.amount);
    }
}
