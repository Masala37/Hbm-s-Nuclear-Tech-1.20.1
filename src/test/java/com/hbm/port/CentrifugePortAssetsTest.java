package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CentrifugePortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void centrifugeAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_centrifuge\": \"Centrifuge\""));
        assertTrue(lang.contains("\"container.centrifuge\": \"Centrifuge\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_centrifuge\""));
        assertTrue(ironTool.contains("\"hbm:machine_centrifuge\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_centrifuge.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/centrifuge_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/centrifuge.json"))
                .contains("centrifuge.obj"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_centrifuge.json")));
        String assembler = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(assembler.contains("\"ass.centrifuge\""));
        assertTrue(assembler.contains("\"hbm:machine_centrifuge\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/centrifuge.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/centrifuge.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib centrifuge.mtl"));
        assertTrue(text.contains("usemtl material"));
        assertTrue(text.contains("\nf "));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/centrifuge.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_centrifuge.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_centrifuge.json")));
        String dump = Files.readString(ROOT.resolve("data/hbm/machine_recipes/centrifuge.json"));
        assertTrue(dump.contains("\"oreCoal\""));
        assertTrue(dump.contains("\"hbm:crystal_iron\""));
        assertFalse(dump.contains("ItemBedrockOre"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/CentrifugeBlockEntity.java"))
                .contains("BASE_CONSUMPTION = 200"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineCentrifugeBlock.java"))
                .contains("{3, 0, 0, 0, 0, 0}"));
    }
}
