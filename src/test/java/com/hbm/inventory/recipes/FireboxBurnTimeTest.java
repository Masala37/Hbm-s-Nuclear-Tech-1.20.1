package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FireboxBurnTimeTest {
    @Test
    void modsFromPathMatchLegacyModuleBurnTime() {
        assertArrayEquals(new double[]{1.25D, 2.0D}, FireboxFuelMods.fromPath("coal"));
        assertArrayEquals(new double[]{1.25D, 2.0D}, FireboxFuelMods.fromPath("coal_block"));
        assertArrayEquals(new double[]{1.0D, 1.0D}, FireboxFuelMods.fromPath("charcoal"));
        assertArrayEquals(new double[]{1.0D, 1.0D}, FireboxFuelMods.fromPath("char_coal"));
        assertArrayEquals(new double[]{1.25D, 2.0D}, FireboxFuelMods.fromPath("coke"));
        assertArrayEquals(new double[]{0.5D, 15.0D}, FireboxFuelMods.fromPath("solid_fuel_bf"));
        assertArrayEquals(new double[]{1.5D, 3.0D}, FireboxFuelMods.fromPath("solid_fuel"));
        assertEquals(1.0D, FireboxFuelMods.fromPath("stick")[0]);
    }
}
