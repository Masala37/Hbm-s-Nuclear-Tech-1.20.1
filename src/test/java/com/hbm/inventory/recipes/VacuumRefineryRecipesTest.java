package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VacuumRefineryRecipesTest {
    @Test
    void copiedOneSevenRatios() {
        assertEquals(100, VacuumRefineryRecipes.INPUT_MB);
        assertEquals(10_000, VacuumRefineryRecipes.POWER_PER_OP);
        assertEquals(40, VacuumRefineryRecipes.VAC_HEAVY);
        assertEquals(25, VacuumRefineryRecipes.VAC_REFORM);
        assertEquals(20, VacuumRefineryRecipes.VAC_LIGHT);
        assertEquals(15, VacuumRefineryRecipes.VAC_GAS);
        assertEquals(100, VacuumRefineryRecipes.VAC_HEAVY + VacuumRefineryRecipes.VAC_REFORM
                + VacuumRefineryRecipes.VAC_LIGHT + VacuumRefineryRecipes.VAC_GAS);
    }
}
