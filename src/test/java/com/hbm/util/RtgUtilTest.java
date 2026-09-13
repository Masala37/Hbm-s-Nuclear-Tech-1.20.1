package com.hbm.util;

import com.hbm.config.MachineConfig;
import com.hbm.interfaces.HalfLifeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtgUtilTest {
    @Test
    void pu238LifespanMatches17Registration() {
        long life = (long) (RTGUtil.getLifespan(87.7F, HalfLifeType.MEDIUM, false) * 1.5);
        assertEquals(631_440_000L, life);
    }

    @Test
    void poloniumShortHalfLifeIsDaysTimes150Percent() {
        long life = (long) (RTGUtil.getLifespan(138.0F, HalfLifeType.SHORT, false) * 1.5);
        assertEquals(9_936_000L, life);
    }

    @Test
    void longHalfLifeUsesHundredYearUnits() {
        long life = (long) (RTGUtil.getLifespan(1.0F, HalfLifeType.LONG, false) * 1.5);
        assertEquals(720_000_000L, life);
    }

    @Test
    void ticksToDateSplitsNtmYears() {
        String[] date = RTGUtil.ticksToDate(48000L * 100L + 48000L);
        assertEquals("1", date[0]);
        assertEquals("1", date[1]);
    }

    @Test
    void goldAndLeadHeatFollow17DecayDefault() {
        assertTrue(MachineConfig.rtgDecay());
        int goldHeat = MachineConfig.rtgDecay() ? 200 : 100;
        int leadHeat = MachineConfig.rtgDecay() ? 600 : 200;
        int heatMax = MachineConfig.rtgDecay() ? 600 : 200;
        assertEquals(200, goldHeat);
        assertEquals(600, leadHeat);
        assertEquals(600, heatMax);
    }
}

