package com.hbm.blockentity.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiFurnaceN9LogicTest {
    @Test
    void extensionTriplesCookStep() {
        assertEquals(1, DiFurnaceBlockEntity.cookStep(false));
        assertEquals(3, DiFurnaceBlockEntity.cookStep(true));
        assertEquals(400, DiFurnaceBlockEntity.PROCESSING_SPEED);
        assertEquals(1200, DiFurnaceRtgBlockEntity.TIME_REQUIRED);
        assertEquals(15, DiFurnaceRtgBlockEntity.MIN_HEAT);
    }
}
