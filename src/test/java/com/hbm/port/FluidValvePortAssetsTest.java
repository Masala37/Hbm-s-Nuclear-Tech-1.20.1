package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidValvePortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void valveSwitchCounterMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.fluid_valve\": \"Fluid Valve\""));
        assertTrue(lang.contains("\"block.hbm.fluid_switch\": \"Redstone Fluid Valve\""));
        assertTrue(lang.contains("\"block.hbm.fluid_counter_valve\": \"Fluid Valve with Counter\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        for (String id : new String[]{"hbm:fluid_valve", "hbm:fluid_switch", "hbm:fluid_counter_valve"}) {
            assertTrue(pickaxe.contains("\"" + id + "\""), id);
            assertTrue(ironTool.contains("\"" + id + "\""), id);
        }

        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/fluid_valve.json")).contains("minecraft:lever"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/fluid_valve.json")).contains("hbm:fluid_duct_paintable"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/fluid_duct_paintable.json")).contains("hbm:ingot_steel"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/fluid_switch.json")).contains("minecraft:redstone"));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/fluid_counter_valve.json")));

        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/fluid_valve.json")).contains("open=true"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/fluid_switch.json")).contains("fluid_switch_on"));
        assertTrue(Files.exists(ROOT.resolve("data/hbm/loot_tables/blocks/fluid_counter_valve.json")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/fluid_valve_off.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/fluid_switch_on.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/fluid_counter_valve_off.png")));
    }
}
