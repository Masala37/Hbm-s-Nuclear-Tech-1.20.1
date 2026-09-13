package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HydrotreatingRecipesTest {
    @Test
    void copiedOneSevenRatios() {
        assertEquals(100, HydrotreatingRecipes.INPUT_MB);
        assertEquals(20_000, HydrotreatingRecipes.POWER_PER_OP);
        assertEquals(2, HydrotreatingRecipes.TICK_DELAY);
        assertEquals(5, HydrotreatingRecipes.OIL_H2);
        assertEquals(90, HydrotreatingRecipes.OIL_DS);
        assertEquals(15, HydrotreatingRecipes.OIL_SOUR);
        assertEquals(5, HydrotreatingRecipes.CRACK_H2);
        assertEquals(90, HydrotreatingRecipes.CRACK_DS);
        assertEquals(15, HydrotreatingRecipes.CRACK_SOUR);
        assertEquals(5, HydrotreatingRecipes.GAS_H2);
        assertEquals(80, HydrotreatingRecipes.GAS_PETRO);
        assertEquals(15, HydrotreatingRecipes.GAS_SOUR);
        assertEquals(105, HydrotreatingRecipes.OIL_DS + HydrotreatingRecipes.OIL_SOUR);
        assertEquals(95, HydrotreatingRecipes.GAS_PETRO + HydrotreatingRecipes.GAS_SOUR);
    }
}
