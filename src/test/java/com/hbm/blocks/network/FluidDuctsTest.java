package com.hbm.blocks.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FluidDuctsTest {
    @Test
    void counterAddsNetTransferWhenTyped() {
        assertEquals(0L, FluidDucts.addCounter(0L, 50, false));
        assertEquals(0L, FluidDucts.addCounter(0L, 0, true));
        assertEquals(150L, FluidDucts.addCounter(100L, 50, true));
    }
}
