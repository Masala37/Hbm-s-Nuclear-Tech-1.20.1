package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HbmFurnaceBurnTimesTest {
    @Test
    void copiesOneSevenFuelHandlerTicks() {
        assertEquals(3200, HbmFurnaceBurnTimes.burnTime("solid_fuel"));
        assertEquals(8000, HbmFurnaceBurnTimes.burnTime("solid_fuel_presto"));
        assertEquals(40000, HbmFurnaceBurnTimes.burnTime("solid_fuel_presto_triplet"));
        assertEquals(32000, HbmFurnaceBurnTimes.burnTime("solid_fuel_bf"));
        assertEquals(80000, HbmFurnaceBurnTimes.burnTime("solid_fuel_presto_bf"));
        assertEquals(400000, HbmFurnaceBurnTimes.burnTime("solid_fuel_presto_triplet_bf"));
        assertEquals(6400, HbmFurnaceBurnTimes.burnTime("rocket_fuel"));
        assertEquals(400, HbmFurnaceBurnTimes.burnTime("biomass"));
        assertEquals(800, HbmFurnaceBurnTimes.burnTime("biomass_compressed"));
        assertEquals(1600, HbmFurnaceBurnTimes.burnTime("powder_coal"));
        assertEquals(50, HbmFurnaceBurnTimes.burnTime("scrap"));
        assertEquals(25, HbmFurnaceBurnTimes.burnTime("dust"));
        assertEquals(400, HbmFurnaceBurnTimes.burnTime("block_scrap"));
        assertEquals(6400, HbmFurnaceBurnTimes.burnTime("powder_fire"));
        assertEquals(1200, HbmFurnaceBurnTimes.burnTime("lignite"));
        assertEquals(1200, HbmFurnaceBurnTimes.burnTime("powder_lignite"));
        assertEquals(4800, HbmFurnaceBurnTimes.burnTime("coal_infernal"));
        assertEquals(6400, HbmFurnaceBurnTimes.burnTime("crystal_coal"));
        assertEquals(100, HbmFurnaceBurnTimes.burnTime("powder_sawdust"));
        assertEquals(200, HbmFurnaceBurnTimes.burnTime("book_guide"));
        assertEquals(0, HbmFurnaceBurnTimes.burnTime("ingot_steel"));
        assertEquals(0, HbmFurnaceBurnTimes.burnTime("coal"));
    }
}
