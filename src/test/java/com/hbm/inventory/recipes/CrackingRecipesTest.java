package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrackingRecipesTest {
    @Test
    void copiedOneSevenRatios() {
        assertEquals(100, CrackingRecipes.INPUT_MB);
        assertEquals(200, CrackingRecipes.STEAM_MB);
        assertEquals(2, CrackingRecipes.SPENT_MB);
        assertEquals(5, CrackingRecipes.TICK_DELAY);
        assertEquals(2, CrackingRecipes.OPS_PER_PULSE);
        assertEquals(100, CrackingRecipes.OIL_CRACKOIL + CrackingRecipes.OIL_PETRO);
        assertEquals(100, CrackingRecipes.SMEAR_NAPHTHA + CrackingRecipes.SMEAR_PETRO);
        assertEquals(70, CrackingRecipes.DIESEL_KERO + CrackingRecipes.DIESEL_PETRO);
        assertEquals(60, CrackingRecipes.KERO_PETRO);
        assertEquals(80, CrackingRecipes.OIL_CRACKOIL);
        assertEquals(20, CrackingRecipes.OIL_PETRO);
        assertEquals(40, CrackingRecipes.DIESEL_KERO);
        assertEquals(30, CrackingRecipes.DIESEL_PETRO);
    }
}
