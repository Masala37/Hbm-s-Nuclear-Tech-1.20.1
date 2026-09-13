package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalyticReformerPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void catalyticReformerAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_catalytic_reformer\": \"Catalytic Reformer\""));
        assertTrue(lang.contains("\"container.catalyticReformer\": \"Catalytic Reformer\""));
        assertTrue(lang.contains("\"fluid.hbm.reformate\": \"Reformate\""));
        assertTrue(lang.contains("\"item.hbm.reformate_bucket\": \"Reformate Bucket\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_catalytic_reformer\""));
        assertTrue(ironTool.contains("\"hbm:machine_catalytic_reformer\""));

        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_catalytic_reformer.json"))
                .contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/catalytic_reformer_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_catalytic_reformer.json"))
                .contains("catalytic_reformer.obj"));

        String dump = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(dump.contains("\"ass.reformer\""));
        assertTrue(dump.contains("\"hbm:machine_catalytic_reformer\""));
        assertTrue(dump.contains("\"ingotNiobium\""));
        assertTrue(dump.contains("\"ingotAnyResistantAlloy\""));
        assertTrue(dump.contains("\"hbm:motor\""));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/catalytic_reformer.obj"));
        assertTrue(obj[0] != (byte) 0xEF);
        String objText = Files.readString(ROOT.resolve("assets/hbm/models/obj/catalytic_reformer.obj"),
                StandardCharsets.UTF_8);
        assertTrue(objText.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(objText));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/models/machines/catalytic_reformer.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/gui/processing/gui_catalytic_reformer.png")));
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
