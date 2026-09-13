package com.hbm.port;

import com.hbm.inventory.recipes.ChemicalPlantRecipes;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrateCanDeathBucketPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void crateCanUsesConservecrateObjAndOneSevenLoot() throws IOException {
        String block = Files.readString(ROOT.resolve("assets/hbm/models/block/crate_can.json"));
        assertTrue(block.contains("\"loader\": \"forge:obj\""));
        assertTrue(block.contains("conservecrate.obj"));
        assertTrue(block.contains("hbm:block/crate_can"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/block/crate_can.png")));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/crate_can.json"))
                .contains("hbm:block/crate_can"));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/conservecrate.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String objText = Files.readString(ROOT.resolve("assets/hbm/models/obj/conservecrate.obj"),
                StandardCharsets.UTF_8);
        assertTrue(objText.startsWith("mtllib conservecrate.mtl"));
        assertTrue(objText.contains("usemtl material"));
        assertTrue(objText.contains("\nf "));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/conservecrate.mtl"))
                .contains("hbm:block/crate_can"));

        String crate = Files.readString(Path.of("src/main/java/com/hbm/blocks/generic/LootCrateBlock.java"));
        assertTrue(crate.contains("Kind.CAN, MapColor.WOOD, SoundType.WOOD, true"));
        assertTrue(crate.contains("noOcclusion()"));
        String blocks = Files.readString(Path.of("src/main/java/com/hbm/registry/ModBlocks.java"));
        assertTrue(blocks.contains("\"crate_can\", LootCrateBlock::can"));

        String loot = Files.readString(Path.of("src/main/java/com/hbm/inventory/loot/StructureLoot.java"));
        int canPool = loot.indexOf("pool(\"CRATE_CAN\"");
        int nextPool = loot.indexOf("alias(\"POOL_NUKE_TRASH\"", canPool);
        assertTrue(canPool >= 0);
        assertTrue(nextPool > canPool);
        String can = loot.substring(canPool, nextPool);
        assertTrue(can.contains("hbm:canned_beef"));
        assertTrue(can.contains("hbm:canned_bark"));
        assertTrue(can.contains("hbm:can_smart"));
        assertTrue(can.contains("hbm:can_bepis"));
        assertTrue(can.contains("hbm:pudding"));
        assertFalse(can.contains("bottle_nuka"));
        assertFalse(can.contains("minecraft:bread"));
        assertTrue(loot.contains("kind == LootCrateBlock.Kind.CAN"));
        assertTrue(loot.contains("5 + random.nextInt(4)"));
    }

    @Test
    void deathBucketModelAndOsmiridiumChemRecipeMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"fluid.hbm.death\": \"Osmiridic Solution\""));
        assertTrue(lang.contains("\"item.hbm.death_bucket\": \"Osmiridic Solution Bucket\""));

        String bucket = Files.readString(ROOT.resolve("assets/hbm/models/item/death_bucket.json"));
        assertTrue(bucket.contains("\"loader\": \"forge:fluid_container\""));
        assertTrue(bucket.contains("\"parent\": \"forge:item/bucket\""));
        assertTrue(bucket.contains("\"fluid\": \"hbm:death\""));

        ChemicalPlantRecipes.loadFromSourceTree();
        GenericMachineRecipe death = ChemicalPlantRecipes.recipes().stream()
                .filter(r -> "chem.osmiridiumdeath".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertEquals(240, death.duration());
        assertEquals(1000L, death.power());
        assertEquals("hbm:powder_paleogenite", death.inputItem().get(0).item());
        assertEquals("dustFluorite", death.inputItem().get(1).ore());
        assertEquals(8, death.inputItem().get(1).count());
        assertEquals("hbm:nugget_bismuth", death.inputItem().get(2).item());
        assertEquals("hbm:peroxide", death.inputFluid().get(0).fluid());
        assertEquals(1000, death.inputFluid().get(0).amount());
        assertEquals("hbm:death", death.outputFluid().get(0).fluid());
        assertEquals(1000, death.outputFluid().get(0).amount());
        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/death_bucket.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/crate_can.json")));
    }
}
