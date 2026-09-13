package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConveyorN10PortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void conveyorAndCranesMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.conveyor\": \"Conveyor Belt\""));
        assertTrue(lang.contains("\"item.hbm.conveyor_wand\": \"Conveyor Belt\""));
        assertTrue(lang.contains("\"block.hbm.crane_inserter\": \"Conveyor Inserter\""));
        assertTrue(lang.contains("\"block.hbm.crane_extractor\": \"Conveyor Ejector\""));
        assertTrue(lang.contains("\"container.craneInserter\": \"Conveyor Inserter\""));
        assertTrue(lang.contains("\"container.craneExtractor\": \"Conveyor Ejector\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        for (String id : new String[]{"hbm:conveyor", "hbm:crane_inserter", "hbm:crane_extractor"}) {
            assertTrue(pickaxe.contains("\"" + id + "\""), id);
            assertTrue(ironTool.contains("\"" + id + "\""), id);
        }

        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/conveyor_wand.json")).contains("minecraft:leather"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/conveyor_wand_from_rubber.json")).contains("hbm:ingot_rubber"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/crane_inserter.json")).contains("minecraft:stone_bricks"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/crane_extractor.json")).contains("hbm:piston_pneumatic"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/piston_pneumatic.json")).contains("hbm:plate_iron"));

        assertTrue(Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/conveyor.json")).contains("hbm:conveyor_wand"));
        assertTrue(Files.exists(ROOT.resolve("data/hbm/loot_tables/blocks/crane_inserter.json")));
        assertTrue(Files.exists(ROOT.resolve("data/hbm/loot_tables/blocks/crane_extractor.json")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/gui/storage/gui_crane_inserter.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/gui/storage/gui_crane_ejector.png")));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/conveyor.json")).contains("bend=2"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/conveyor.json")).contains("\"to\": [16, 4, 16]"));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/conveyor.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/crane_side_in.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/models/item/conveyor_wand.json")));
    }
}
