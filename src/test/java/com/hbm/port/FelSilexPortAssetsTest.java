package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FelSilexPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void felAndSilexAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_fel\": \"FEL\""));
        assertTrue(lang.contains("\"block.hbm.machine_silex\": \"Laser Isotope Separation Chamber (SILEX)\""));
        assertTrue(lang.contains("\"container.machineFEL\": \"FEL\""));
        assertTrue(lang.contains("\"container.machineSILEX\": \"SILEX\""));
        assertTrue(lang.contains("\"item.hbm.laser_crystal_co2\": \"CO2-Desh Laser Crystal\""));
        assertTrue(lang.contains("\"item.hbm.laser_crystal_bismuth\": \"BiSmUTh Laser Crystal\""));
        assertTrue(lang.contains("\"wavelengths.name.ir\": \"Infrared\""));
        assertTrue(lang.contains("\"fluid.hbm.death\": \"Osmiridic Solution\""));
        assertTrue(lang.contains("\"fluid.hbm.vitriol\": \"Vitriol\""));
        assertTrue(lang.contains("\"fluid.hbm.redmud\": \"Red Mud\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_fel\""));
        assertTrue(pickaxe.contains("\"hbm:machine_silex\""));
        assertTrue(ironTool.contains("\"hbm:machine_fel\""));
        assertTrue(ironTool.contains("\"hbm:machine_silex\""));

        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_fel.json")).contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_silex.json")).contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/fel_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/silex_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_fel.json")).contains("fel.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_silex.json")).contains("silex.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/machine_fel.json"))
                .contains("hbm:block/machine_fel"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/machine_silex.json"))
                .contains("hbm:block/machine_silex"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_fel.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_silex.json")));
        String assembler = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(assembler.contains("\"ass.fel\""));
        assertTrue(assembler.contains("\"hbm:machine_fel\""));
        assertTrue(assembler.contains("\"ass.silex\""));
        assertTrue(assembler.contains("\"hbm:machine_silex\""));

        byte[] felObj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/fel.obj"));
        assertTrue(felObj[0] != (byte) 0xEF);
        String felText = Files.readString(ROOT.resolve("assets/hbm/models/obj/fel.obj"), StandardCharsets.UTF_8);
        assertTrue(felText.startsWith("mtllib fel.mtl"));
        assertTrue(felText.contains("o Cube_Cube.001"));
        assertTrue(felText.contains("usemtl material"));
        assertTrue(felText.contains("\nf "));
        byte[] silexObj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/silex.obj"));
        assertTrue(silexObj[0] != (byte) 0xEF);
        String silexText = Files.readString(ROOT.resolve("assets/hbm/models/obj/silex.obj"), StandardCharsets.UTF_8);
        assertTrue(silexText.startsWith("mtllib silex.mtl"));
        assertTrue(silexText.contains("o Cube_Cube.001"));
        assertTrue(silexText.contains("\nf "));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/fel.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/silex.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/machine/gui_fel.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_silex.png")));
        String felLoot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/machine_fel.json"));
        assertTrue(felLoot.contains("\"meta\": \"12\""));
        String silexLoot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/machine_silex.json"));
        assertTrue(silexLoot.contains("\"meta\": \"12\""));
        String recipes = Files.readString(ROOT.resolve("data/hbm/machine_recipes/silex.json"));
        assertTrue(recipes.contains("\"ingotUranium\""));
        assertTrue(recipes.contains("\"hbm:uf6\""));
        assertTrue(recipes.contains("\"VISIBLE\""));
        assertTrue(recipes.contains("\"laser\": \"IR\""));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineFelBlock.java"))
                .contains("{2, 0, 4, 2, 1, 1}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineSilexBlock.java"))
                .contains("{2, 0, 1, 1, 1, 1}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/FelBlockEntity.java"))
                .contains("MAX_POWER = 20_000_000"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/FelBlockEntity.java"))
                .contains("POWER_REQ = 1250"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/SilexBlockEntity.java"))
                .contains("TANK_CAPACITY = 16_000"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/SilexBlockEntity.java"))
                .contains("PROCESS_TIME = 100"));
    }
}
