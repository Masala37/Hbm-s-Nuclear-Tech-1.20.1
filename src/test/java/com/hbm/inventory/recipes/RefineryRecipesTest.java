package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RefineryRecipesTest {
    @Test
    void hotOilFractionsSumToOneHundred() {
        assertEquals(100, RefineryRecipes.INPUT_MB);
        assertEquals(100, RefineryRecipes.HEAVY_MB + RefineryRecipes.NAPHTHA_MB
                + RefineryRecipes.LIGHT_MB + RefineryRecipes.PETRO_MB);
        assertEquals(50, RefineryRecipes.HEAVY_MB);
        assertEquals(25, RefineryRecipes.NAPHTHA_MB);
        assertEquals(15, RefineryRecipes.LIGHT_MB);
        assertEquals(10, RefineryRecipes.PETRO_MB);
        assertEquals(5, RefineryRecipes.POWER_PER_OP);
        assertEquals(10, RefineryRecipes.SULFUR_EVERY);
    }
}
