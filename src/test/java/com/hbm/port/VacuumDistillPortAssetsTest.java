package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VacuumDistillPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void vacuumDistillAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_vacuum_distill\": \"Vacuum Refinery\""));
        assertTrue(lang.contains("\"container.vacuumDistill\": \"Vacuum Refinery\""));
        assertTrue(lang.contains("\"fluid.hbm.heavyoil_vacuum\": \"Vacuum Heavy Oil\""));
        assertTrue(lang.contains("\"fluid.hbm.lightoil_vacuum\": \"Vacuum Light Oil\""));
        assertTrue(lang.contains("\"fluid.hbm.heatingoil_vacuum\": \"Heavy Heating Oil\""));
        assertTrue(lang.contains("\"fluid.hbm.reformgas\": \"Reformate Gas\""));
        assertTrue(lang.contains("\"item.hbm.heavyoil_vacuum_bucket\": \"Vacuum Heavy Oil Bucket\""));
        assertTrue(lang.contains("\"item.hbm.lightoil_vacuum_bucket\": \"Vacuum Light Oil Bucket\""));
        assertTrue(lang.contains("\"item.hbm.heatingoil_vacuum_bucket\": \"Heavy Heating Oil Bucket\""));
        assertTrue(lang.contains("\"item.hbm.reformgas_bucket\": \"Reformate Gas Bucket\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_vacuum_distill\""));
        assertTrue(ironTool.contains("\"hbm:machine_vacuum_distill\""));

        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_vacuum_distill.json"))
                .contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/vacuum_distill_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_vacuum_distill.json"))
                .contains("vacuum_distill.obj"));

        String dump = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(dump.contains("\"ass.vaccumrefinery\""));
        assertTrue(dump.contains("\"hbm:machine_vacuum_distill\""));
        assertTrue(dump.contains("\"hbm:sphere_steel\""));
        assertTrue(dump.contains("\"hbm:motor_desh\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/vacuum_distill.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String objText = Files.readString(ROOT.resolve("assets/hbm/models/obj/vacuum_distill.obj"),
                StandardCharsets.UTF_8);
        assertTrue(objText.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(objText));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/models/machines/vacuum_distill.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/gui/processing/gui_vacuum_distill.png")));
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
