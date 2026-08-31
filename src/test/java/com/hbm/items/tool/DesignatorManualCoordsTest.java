package com.hbm.items.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DesignatorManualCoordsTest {

    @Test
    void addAndSubtractSteps() {
        assertEquals(10, DesignatorManualCoords.next(0, 0, 10, 0));
        assertEquals(-5, DesignatorManualCoords.next(0, 1, 5, 0));
        assertEquals(150, DesignatorManualCoords.next(50, 0, 100, 0));
        assertEquals(-49, DesignatorManualCoords.next(1, 1, 50, 0));
    }

    @Test
    void setUsesPlayerCoord() {
        assertEquals(123, DesignatorManualCoords.next(0, 2, 0, 123));
        assertEquals(-40, DesignatorManualCoords.next(99, 2, 0, -40));
    }

    @Test
    void unknownOperatorLeavesValue() {
        assertEquals(7, DesignatorManualCoords.next(7, 9, 1, 0));
    }
}
