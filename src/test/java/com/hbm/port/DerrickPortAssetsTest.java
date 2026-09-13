package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DerrickPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void derrickAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_well\": \"Oil Derrick\""));
        assertTrue(lang.contains("\"container.oilWell\": \"Oil Derrick\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_well\""));
        assertTrue(ironTool.contains("\"hbm:machine_well\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_well.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/derrick_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_well.json"))
                .contains("derrick.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/derrick.json"))
                .contains("derrick.obj"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_well.json")));
        String assembler = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(assembler.contains("\"ass.derrick\""));
        assertTrue(assembler.contains("\"hbm:machine_well\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/derrick.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/derrick.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib derrick.mtl"));
        assertTrue(text.contains("usemtl material"));
        assertTrue(text.contains("\nf "));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/derrick.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_well.json")));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/OilWellBlockEntity.java"))
                .contains("ENERGY_PER_TICK = 100"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineOilWellBlock.java"))
                .contains("{9, 0, 0, 0, 0, 0}"));
    }
}
