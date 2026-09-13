package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OilPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void pumpjackAndRefineryAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_pumpjack\": \"Pumpjack\""));
        assertTrue(lang.contains("\"block.hbm.machine_refinery\": \"Oil Refinery\""));
        assertTrue(lang.contains("\"container.pumpjack\": \"Pumpjack\""));
        assertTrue(lang.contains("\"container.machineRefinery\": \"Oil Refinery\""));
        assertTrue(lang.contains("\"fluid.hbm.hotoil\": \"Hot Crude Oil\""));
        assertTrue(lang.contains("\"fluid.hbm.naphtha\": \"Naphtha\""));
        assertTrue(lang.contains("\"fluid.hbm.gas\": \"Natural Gas\""));
        assertTrue(lang.contains("\"item.hbm.hotoil_bucket\": \"Hot Crude Oil Bucket\""));
        assertTrue(lang.contains("\"item.hbm.naphtha_bucket\": \"Naphtha Bucket\""));
        assertTrue(lang.contains("\"item.hbm.gas_bucket\": \"Natural Gas Bucket\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_pumpjack\""));
        assertTrue(pickaxe.contains("\"hbm:machine_refinery\""));
        assertTrue(ironTool.contains("\"hbm:machine_pumpjack\""));
        assertTrue(ironTool.contains("\"hbm:machine_refinery\""));

        String pumpState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_pumpjack.json"));
        String refState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_refinery.json"));
        assertTrue(pumpState.contains("\"meta=15\""));
        assertTrue(refState.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/pumpjack_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/refinery_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_pumpjack.json"))
                .contains("pumpjack_item.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_refinery.json"))
                .contains("refinery.obj"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_pumpjack.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/machine_refinery.json")));

        String assembler = Files.readString(ROOT.resolve("data/hbm/machine_recipes/assembly_machine.json"));
        assertTrue(assembler.contains("\"ass.pumpjack\""));
        assertTrue(assembler.contains("\"ass.refinery\""));
        assertTrue(assembler.contains("\"hbm:machine_pumpjack\""));
        assertTrue(assembler.contains("\"hbm:machine_refinery\""));

        byte[] base = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/pumpjack_base.obj"));
        byte[] item = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/pumpjack_item.obj"));
        byte[] refinery = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/refinery.obj"));
        assertTrue(base[0] != (byte) 0xEF);
        assertTrue(item[0] != (byte) 0xEF);
        assertTrue(refinery[0] != (byte) 0xEF);
        String baseObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/pumpjack_base.obj"), StandardCharsets.UTF_8);
        String rotorObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/pumpjack_rotor.obj"), StandardCharsets.UTF_8);
        String refObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/refinery.obj"), StandardCharsets.UTF_8);
        assertTrue(baseObj.startsWith("mtllib "));
        assertTrue(rotorObj.startsWith("mtllib "));
        assertTrue(refObj.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(baseObj));
        assertTrue(objFacesFitVertices(rotorObj));
        assertTrue(objFacesFitVertices(refObj));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/oil_pipe.png")));
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
