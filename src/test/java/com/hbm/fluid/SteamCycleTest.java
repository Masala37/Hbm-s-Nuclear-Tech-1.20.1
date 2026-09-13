package com.hbm.fluid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SteamCycleTest {
    @Test
    void boilerHeatReqIsWaterHeatOverEfficiency() {
        assertEquals(200, SteamCycle.boilerHeatReq());
    }

    @Test
    void boilIsLimitedByHeatWaterAndSteamSpace() {
        int heatReq = SteamCycle.boilerHeatReq();
        assertEquals(5, SteamCycle.boil(10, 0, 2000, 1000, heatReq));
        assertEquals(0, SteamCycle.boil(0, 0, 2000, 1000, heatReq));
        assertEquals(0, SteamCycle.boil(10, 2000, 2000, 1000, heatReq));
        assertEquals(0, SteamCycle.boil(10, 0, 2000, 199, heatReq));
        assertEquals(1, SteamCycle.boil(1, 0, 100, 200, heatReq));
    }

    @Test
    void oilHeatIsTenTuPerMillibucket() {
        assertEquals(10, SteamCycle.oilHeatReq());
        assertEquals(5, SteamCycle.heatOil(10, 0, 100, 50, SteamCycle.oilHeatReq()));
        assertEquals(0, SteamCycle.heatOil(0, 0, 100, 50, SteamCycle.oilHeatReq()));
        assertEquals(0, SteamCycle.heatOil(10, 0, 100, 9, SteamCycle.oilHeatReq()));
        assertEquals(1, SteamCycle.heatOil(1, 0, 1, 10, SteamCycle.oilHeatReq()));
    }

    @Test
    void turbineOpsMatchLegacySteamStep() {
        int ops = SteamCycle.turbineOps(6000, 0, 128_000,
                SteamCycle.STEAM_TURBINE_REQ, SteamCycle.STEAM_TURBINE_PROD, 6_000);
        assertEquals(60, ops);
        int generated = (int) (ops * SteamCycle.STEAM_TURBINE_HEAT
                * SteamCycle.TURBINE_TRAIT_EFF * 0.85D);
        assertEquals(10_200, generated);
    }

    @Test
    void condenserConvertsOneToOneUntilWaterIsFull() {
        assertEquals(50, SteamCycle.condenserConvert(50, 40, 100));
        assertEquals(0, SteamCycle.condenserConvert(10, 100, 100));
        assertEquals(0, SteamCycle.condenserConvert(0, 0, 100));
    }
}
