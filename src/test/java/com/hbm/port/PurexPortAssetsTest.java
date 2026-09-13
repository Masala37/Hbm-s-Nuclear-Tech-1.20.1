package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurexPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void purexAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_purex\": \"PUREX\""));
        assertTrue(lang.contains("\"container.machinePUREX\": \"PUREX\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_purex\""));
        assertTrue(ironTool.contains("\"hbm:machine_purex\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_purex.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/purex_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_purex.json"))
                .contains("purex.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/machine_purex.json"))
                .contains("hbm:block/machine_purex"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_purex.json")));
        String assembler = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(assembler.contains("\"hbm:machine_purex\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/purex.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/purex.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib purex.mtl"));
        assertTrue(text.contains("o Base"));
        assertTrue(text.contains("o Frame"));
        assertTrue(text.contains("o Fan"));
        assertTrue(text.contains("o Pump"));
        assertTrue(text.contains("usemtl material"));
        assertTrue(text.contains("\nf "));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/purex.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_purex.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_purex.json")));
        String loot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/machine_purex.json"));
        assertTrue(loot.contains("\"meta\": \"12\""));
        String recipes = Files.readString(ROOT.resolve("data/hbm/machine_recipes/purex.json"));
        assertTrue(recipes.contains("\"purex.uzh\""));
        assertTrue(recipes.contains("\"hbm:billet_uranium_fuel\""));
        assertTrue(recipes.contains("billetZirconium"));
        assertTrue(recipes.contains("\"purex.pilepu\""));
        assertTrue(recipes.contains("\"purex.thoriumsalt\""));
        assertTrue(recipes.contains("\"chance\":  0.5") || recipes.contains("\"chance\": 0.5"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachinePurexBlock.java"))
                .contains("{4, 0, 2, 2, 2, 2}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/PurexBlockEntity.java"))
                .contains("POWER_FLOOR = 1_000_000"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/PurexBlockEntity.java"))
                .contains("TANK_CAPACITY = 24_000"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/PurexBlockEntity.java"))
                .contains("SLOT_COUNT = 13"));
    }
}
