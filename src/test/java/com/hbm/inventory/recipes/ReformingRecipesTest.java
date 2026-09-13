package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReformingRecipesTest {
    @Test
    void copiedOneSevenRatios() {
        assertEquals(100, ReformingRecipes.INPUT_MB);
        assertEquals(20_000, ReformingRecipes.POWER_PER_OP);
        assertEquals(50, ReformingRecipes.HEAT_NAPHTHA);
        assertEquals(15, ReformingRecipes.HEAT_PETRO);
        assertEquals(10, ReformingRecipes.HEAT_H2);
        assertEquals(50, ReformingRecipes.NAPHTHA_REFORMATE);
        assertEquals(15, ReformingRecipes.NAPHTHA_PETRO);
        assertEquals(10, ReformingRecipes.NAPHTHA_H2);
        assertEquals(75, ReformingRecipes.SOUR_ACID);
        assertEquals(10, ReformingRecipes.SOUR_PETRO);
        assertEquals(15, ReformingRecipes.SOUR_H2);
        assertEquals(75, ReformingRecipes.HEAT_NAPHTHA + ReformingRecipes.HEAT_PETRO + ReformingRecipes.HEAT_H2);
        assertEquals(75, ReformingRecipes.NAPHTHA_REFORMATE + ReformingRecipes.NAPHTHA_PETRO
                + ReformingRecipes.NAPHTHA_H2);
        assertEquals(100, ReformingRecipes.SOUR_ACID + ReformingRecipes.SOUR_PETRO + ReformingRecipes.SOUR_H2);
    }
}
