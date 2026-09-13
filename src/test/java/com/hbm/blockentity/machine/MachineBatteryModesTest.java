package com.hbm.blockentity.machine;

import com.hbm.energy.ConnectionPriority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MachineBatteryModesTest {
    @Test
    void redstoneModesCycleThroughFourStates() {
        assertEquals(1, MachineBatteryBlockEntity.cycleMode((short) 0));
        assertEquals(2, MachineBatteryBlockEntity.cycleMode((short) 1));
        assertEquals(3, MachineBatteryBlockEntity.cycleMode((short) 2));
        assertEquals(0, MachineBatteryBlockEntity.cycleMode((short) 3));
    }

    @Test
    void priorityCyclesLowNormalHigh() {
        assertEquals(ConnectionPriority.NORMAL, MachineBatteryBlockEntity.cyclePriority(ConnectionPriority.LOW));
        assertEquals(ConnectionPriority.HIGH, MachineBatteryBlockEntity.cyclePriority(ConnectionPriority.NORMAL));
        assertEquals(ConnectionPriority.LOW, MachineBatteryBlockEntity.cyclePriority(ConnectionPriority.HIGH));
        assertEquals(ConnectionPriority.LOW, MachineBatteryBlockEntity.cyclePriority(ConnectionPriority.HIGHEST));
    }
}
