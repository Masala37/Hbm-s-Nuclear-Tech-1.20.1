package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixerPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void mixerAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_mixer\": \"Industrial Mixer\""));
        assertTrue(lang.contains("\"container.machineMixer\": \"Industrial Mixer\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_mixer\""));
        assertTrue(ironTool.contains("\"hbm:machine_mixer\""));

        String state = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_mixer.json"));
        assertTrue(state.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/mixer_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_mixer.json"))
                .contains("mixer.obj"));

        String craft = Files.readString(ROOT.resolve("data/hbm/recipes/machine_mixer.json"));
        assertTrue(craft.contains("\"PIP\""));
        assertTrue(craft.contains("\"hbm:circuit_vacuum_tube\""));
        assertTrue(craft.contains("\"hbm:motor\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/mixer.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String text = Files.readString(ROOT.resolve("assets/hbm/models/obj/mixer.obj"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("mtllib mixer.mtl"));
        assertTrue(text.contains("o Main"));
        assertTrue(text.contains("o Mixer"));
        assertFalse(text.contains("o Fluid"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/mixer_main.obj"))
                .contains("o Main"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/mixer_blades.obj"))
                .contains("o Mixer"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/obj/mixer_fluid.obj"))
                .contains("o Fluid"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/machines/mixer.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/processing/gui_mixer.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/machine_mixer.json")));
        String loot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/machine_mixer.json"));
        assertTrue(loot.contains("\"meta\": \"12\""));
        String recipes = Files.readString(ROOT.resolve("data/hbm/machine_recipes/mixer.json"));
        assertTrue(recipes.contains("\"hbm:sulfuric_acid\""));
        assertTrue(recipes.contains("\"hbm:peroxide\""));
        assertTrue(recipes.contains("\"ore\": \"dustSulfur\""));
        assertTrue(recipes.contains("\"hbm:kerosene_reform\""));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/machine/MachineMixerBlock.java"))
                .contains("{2, 0, 0, 0, 0, 0}"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/MixerBlockEntity.java"))
                .contains("CONSUMPTION = 50"));
    }
}
