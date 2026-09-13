package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrystallizerPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void crystallizerAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_crystallizer\": \"Ore Acidizer\""));
        assertTrue(lang.contains("\"container.crystallizer\": \"Ore Acidizer\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_crystallizer\""));
        assertTrue(ironTool.contains("\"hbm:machine_crystallizer\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_crystallizer.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/crystallizer_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_crystallizer.json"))
                .contains("acidizer.obj"));

        String dump = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(dump.contains("\"ass.acidizer\""));
        assertTrue(dump.contains("\"hbm:machine_crystallizer\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/acidizer.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/acidizer.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib acidizer.mtl"));
        assertTrue(text.contains("o Body"));
        assertTrue(text.contains("o Spinner"));
        assertFalse(text.contains("o Fluid"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/acidizer_body.obj"))
                .contains("o Body"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/acidizer_spinner.obj"))
                .contains("o Spinner"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/acidizer_fluid.obj"))
                .contains("o Fluid"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/acidizer.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_crystallizer_alt.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_crystallizer.json")));
        String loot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/machine_crystallizer.json"));
        assertTrue(loot.contains("\"meta\": \"12\""));
        String recipes = Files.readString(ROOT.resolve("data/hbm/machine_recipes/crystallizer.json"));
        assertTrue(recipes.contains("\"ore\": \"oreIron\""));
        assertTrue(recipes.contains("\"hbm:crystal_iron\""));
        assertTrue(recipes.contains("\"hbm:peroxide\""));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineCrystallizerBlock.java"))
                .contains("{5, 0, 1, 1, 1, 1}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/CrystallizerBlockEntity.java"))
                .contains("DEMAND = 1_000"));
    }
}
