package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HydrotreaterPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void hydrotreaterAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_hydrotreater\": \"Hydrotreater\""));
        assertTrue(lang.contains("\"container.hydrotreater\": \"Hydrotreater\""));
        assertTrue(lang.contains("\"fluid.hbm.oil_ds\": \"Desulfurized Crude Oil\""));
        assertTrue(lang.contains("\"fluid.hbm.crackoil_ds\": \"Desulfurized Cracked Oil\""));
        assertTrue(lang.contains("\"fluid.hbm.sourgas\": \"Sour Gas\""));
        assertTrue(lang.contains("\"item.hbm.oil_ds_bucket\": \"Desulfurized Crude Oil Bucket\""));
        assertTrue(lang.contains("\"item.hbm.crackoil_ds_bucket\": \"Desulfurized Cracked Oil Bucket\""));
        assertTrue(lang.contains("\"item.hbm.sourgas_bucket\": \"Sour Gas Bucket\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_hydrotreater\""));
        assertTrue(ironTool.contains("\"hbm:machine_hydrotreater\""));

        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_hydrotreater.json"))
                .contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/hydrotreater_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_hydrotreater.json"))
                .contains("hydrotreater.obj"));

        String dump = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(dump.contains("\"ass.hydrotreater\""));
        assertTrue(dump.contains("\"hbm:machine_hydrotreater\""));
        assertTrue(dump.contains("\"ingotNiobium\""));
        assertTrue(dump.contains("\"hbm:motor_desh\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/hydrotreater.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String objText = Files.readString(ROOT.resolve("assets/hbm/models/obj/hydrotreater.obj"),
                StandardCharsets.UTF_8);
        assertTrue(objText.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(objText));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/models/machines/hydrotreater.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/gui/processing/gui_hydrotreater.png")));
    }

    private static boolean objFacesFitVertices(String obj) {
        int verts = 0;
        int maxFace = 0;
        for (String line : obj.split("\\R")) {
            if (line.startsWith("v ")) {
                verts++;
            }
            if (line.startsWith("f ")) {
                for (String token : line.substring(2).trim().split("\\s+")) {
                    String vertex = token.split("/")[0];
                    if (!vertex.isEmpty()) {
                        maxFace = Math.max(maxFace, Integer.parseInt(vertex));
                    }
                }
            }
        }
        return verts > 0 && maxFace > 0 && maxFace <= verts;
    }
}
