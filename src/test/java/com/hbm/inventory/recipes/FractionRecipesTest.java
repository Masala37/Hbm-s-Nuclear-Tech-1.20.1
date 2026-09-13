package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FractionRecipesTest {
    @Test
    void oilOneRowsSumToOneHundred() {
        assertEquals(100, FractionRecipes.INPUT_MB);
        assertEquals(10, FractionRecipes.TICK_DELAY);
        assertEquals(100, FractionRecipes.HEAVYOIL_BITUMEN + FractionRecipes.HEAVYOIL_SMEAR);
        assertEquals(100, FractionRecipes.SMEAR_HEATINGOIL + FractionRecipes.SMEAR_LUBRICANT);
        assertEquals(100, FractionRecipes.NAPHTHA_HEATINGOIL + FractionRecipes.NAPHTHA_DIESEL);
        assertEquals(100, FractionRecipes.LIGHTOIL_DIESEL + FractionRecipes.LIGHTOIL_KEROSENE);
        assertEquals(30, FractionRecipes.HEAVYOIL_BITUMEN);
        assertEquals(70, FractionRecipes.HEAVYOIL_SMEAR);
        assertEquals(40, FractionRecipes.NAPHTHA_HEATINGOIL);
        assertEquals(60, FractionRecipes.NAPHTHA_DIESEL);
        assertEquals(40, FractionRecipes.LIGHTOIL_DIESEL);
        assertEquals(60, FractionRecipes.LIGHTOIL_KEROSENE);
        assertEquals(100, FractionRecipes.VAC_HEAVY_SMEAR + FractionRecipes.VAC_HEAVY_HEAT);
        assertEquals(100, FractionRecipes.VAC_LIGHT_KEROSENE + FractionRecipes.VAC_LIGHT_REFORMGAS);
        assertEquals(40, FractionRecipes.VAC_HEAVY_SMEAR);
        assertEquals(60, FractionRecipes.VAC_HEAVY_HEAT);
        assertEquals(70, FractionRecipes.VAC_LIGHT_KEROSENE);
        assertEquals(30, FractionRecipes.VAC_LIGHT_REFORMGAS);
    }
}
