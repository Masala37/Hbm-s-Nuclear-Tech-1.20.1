package com.hbm.items.weapon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MissileItemFuelTest {

    @Test
    void fuelCapsMatchLegacyFormFactors() {
        assertEquals(0, MissileItem.GuiTier.TIER0.fuelCap);
        assertEquals(4_000, MissileItem.GuiTier.TIER1.fuelCap);
        assertEquals(8_000, MissileItem.GuiTier.TIER2.fuelCap);
        assertEquals(12_000, MissileItem.GuiTier.TIER3.fuelCap);
        assertEquals(8_000, MissileItem.GuiTier.STEALTH.fuelCap);
        assertEquals(8_000, MissileItem.GuiTier.ROBIN.fuelCap);
        assertEquals(16_000, MissileItem.GuiTier.TIER4.fuelCap);
        assertEquals(0, MissileItem.GuiTier.ABM.fuelCap);
    }
}
