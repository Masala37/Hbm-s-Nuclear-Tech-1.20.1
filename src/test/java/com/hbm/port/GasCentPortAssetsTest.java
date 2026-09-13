package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GasCentPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void gasCentAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_gascent\": \"Gas Centrifuge\""));
        assertTrue(lang.contains("\"container.gasCentrifuge\": \"Gas Centrifuge\""));
        assertTrue(lang.contains("\"hbmpseudofluid.nuf6\": \"Natural UF6\""));
        assertTrue(lang.contains("\"fluid.hbm.uf6\": \"Uranium Hexafluoride\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_gascent\""));
        assertTrue(ironTool.contains("\"hbm:machine_gascent\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_gascent.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/gascent_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_gascent.json"))
                .contains("gascent.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/machine_gascent.json"))
                .contains("hbm:block/machine_gascent"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_gascent.json")));
        String assembler = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(assembler.contains("\"ass.gascent\""));
        assertTrue(assembler.contains("\"hbm:machine_gascent\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/gascent.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/gascent.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib gascent.mtl"));
        assertTrue(text.contains("o Centrifuge"));
        assertTrue(text.contains("usemtl material"));
        assertTrue(text.contains("\nf "));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/gascent_flag.obj"), StandardCharsets.UTF_8)
                .contains("o Flag"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/gascent.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_centrifuge_gas.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_gascent.json")));
        String loot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/machine_gascent.json"));
        assertTrue(loot.contains("\"meta\": \"12\""));
        String recipes = Files.readString(ROOT.resolve("data/hbm/machine_recipes/gas_centrifuge.json"));
        assertTrue(recipes.contains("\"NUF6\""));
        assertTrue(recipes.contains("\"hbm:uf6\""));
        assertTrue(recipes.contains("\"HEUF6\""));
        assertTrue(recipes.contains("\"highSpeed\": true"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineGasCentBlock.java"))
                .contains("{3, 0, 0, 0, 0, 0}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/GasCentBlockEntity.java"))
                .contains("PROCESSING_SPEED = 150"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/GasCentBlockEntity.java"))
                .contains("CONSUMPTION = 200"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/GasCentBlockEntity.java"))
                .contains("TANK_CAPACITY = 2_000"));
    }
}
