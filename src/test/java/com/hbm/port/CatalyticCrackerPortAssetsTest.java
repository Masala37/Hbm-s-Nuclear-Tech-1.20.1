package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalyticCrackerPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void catalyticCrackerAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_catalytic_cracker\": \"Catalytic Cracking Tower\""));
        assertTrue(lang.contains("\"fluid.hbm.crackoil\": \"Cracked Oil\""));
        assertTrue(lang.contains("\"item.hbm.crackoil_bucket\": \"Cracked Oil Bucket\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_catalytic_cracker\""));
        assertTrue(ironTool.contains("\"hbm:machine_catalytic_cracker\""));

        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_catalytic_cracker.json"))
                .contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/catalytic_cracker_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_catalytic_cracker.json"))
                .contains("catalytic_cracker.obj"));

        String dump = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(dump.contains("\"ass.crackingtower\""));
        assertTrue(dump.contains("\"hbm:machine_catalytic_cracker\""));
        assertTrue(dump.contains("\"hbm:steel_scaffold\""));
        assertTrue(dump.contains("\"ingotWorkersAlloy\""));
        assertTrue(dump.contains("\"ingotNiobium\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/catalytic_cracker.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String objText = Files.readString(ROOT.resolve("assets/hbm/models/obj/catalytic_cracker.obj"),
                StandardCharsets.UTF_8);
        assertTrue(objText.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(objText));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/models/machines/catalytic_cracker.png")));
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
