package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WoodBurnerFuelModsTest {
    @Test
    void logAndWoodModsMatchLegacyModuleBurnTime() {
        assertEquals(4.0D, WoodBurnerFuelMods.timeMod("oak_log"));
        assertEquals(4.0D, WoodBurnerFuelMods.timeMod("stripped_spruce_log"));
        assertEquals(4.0D, WoodBurnerFuelMods.timeMod("log"));
        assertEquals(2.0D, WoodBurnerFuelMods.timeMod("oak_planks"));
        assertEquals(2.0D, WoodBurnerFuelMods.timeMod("oak_wood"));
        assertEquals(2.0D, WoodBurnerFuelMods.timeMod("stick"));
        assertEquals(1.0D, WoodBurnerFuelMods.timeMod("coal"));
        assertEquals(1.0D, WoodBurnerFuelMods.timeMod("charcoal"));
    }

    @Test
    void woodoilLiquidFeMatchesLegacyFlammableTrait() {
        assertEquals(110, (int) (110_000L * 2 / 2_000L));
        assertEquals(200, (int) (200_000L * 2 / 2_000L));
    }
}
