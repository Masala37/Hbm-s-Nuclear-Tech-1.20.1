package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidDuctPortAssetsTest {
    @Test
    void fluidDuctNeoAssetsExist() {
        Path assets = Path.of("src/main/resources/assets/hbm");
        Path data = Path.of("src/main/resources/data/hbm");
        assertTrue(Files.exists(assets.resolve("blockstates/fluid_duct_neo.json")));
        assertTrue(Files.exists(assets.resolve("models/block/fluid_duct_neo.json")));
        assertTrue(Files.exists(assets.resolve("models/item/fluid_duct_neo.json")));
        assertTrue(Files.exists(data.resolve("loot_tables/blocks/fluid_duct_neo.json")));
        assertTrue(Files.exists(data.resolve("recipes/fluid_duct_neo.json")));
        assertTrue(Files.exists(data.resolve("machine_recipes/assembly_machine.json")));
        assertTrue(Files.exists(data.resolve("machine_recipes/chemical_plant.json")));
    }
}
