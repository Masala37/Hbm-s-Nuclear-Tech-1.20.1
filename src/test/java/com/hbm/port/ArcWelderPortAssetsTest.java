package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArcWelderPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void arcWelderAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_arc_welder\": \"Arc Welder\""));
        assertTrue(lang.contains("\"container.machineArcWelder\": \"Arc Welder\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_arc_welder\""));
        assertTrue(ironTool.contains("\"hbm:machine_arc_welder\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_arc_welder.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/arc_welder_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/arc_welder.json"))
                .contains("arc_welder.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_arc_welder.json"))
                .contains("arc_welder.obj"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_arc_welder.json")));
        String anvil = Files.readString(ROOT.resolve("data/hbm/machine_recipes/anvil.json"));
        assertTrue(anvil.contains("\"hbm:machine_arc_welder\""));
        assertTrue(anvil.contains("\"hbm:arc_electrode\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/arc_welder.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/arc_welder.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib arc_welder.mtl"));
        assertTrue(text.contains("o Plane"));
        assertTrue(text.contains("usemtl material"));
        assertTrue(text.contains("\nf "));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/arc_welder.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_arc_welder.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_arc_welder.json")));
        String loot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/machine_arc_welder.json"));
        assertTrue(loot.contains("\"meta\": \"12\""));
        String recipes = Files.readString(ROOT.resolve("data/hbm/machine_recipes/arc_welder.json"));
        assertTrue(recipes.contains("\"hbm:motor\""));
        assertTrue(recipes.contains("\"ore\": \"wireDenseMingrade\""));
        assertTrue(recipes.contains("\"hbm:thruster_small\""));
        assertTrue(recipes.contains("\"hbm:plate_welded_steel\""));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineArcWelderBlock.java"))
                .contains("{1, 0, 1, 0, 1, 1}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/ArcWelderBlockEntity.java"))
                .contains("DEFAULT_CONSUMPTION = 100"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/ArcWelderBlockEntity.java"))
                .contains("TANK_CAPACITY = 24_000"));
    }
}
