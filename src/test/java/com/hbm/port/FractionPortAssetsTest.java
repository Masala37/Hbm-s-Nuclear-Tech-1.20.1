package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FractionPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void fractionTowerAssetsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_fraction_tower\": \"Fractioning Tower\""));
        assertTrue(lang.contains("\"block.hbm.fraction_spacer\": \"Fraction Tower Spacer\""));
        assertTrue(lang.contains("\"fluid.hbm.bitumen\": \"Bitumen\""));
        assertTrue(lang.contains("\"fluid.hbm.smear\": \"Smear\""));
        assertTrue(lang.contains("\"fluid.hbm.heatingoil\": \"Heating Oil\""));
        assertTrue(lang.contains("\"item.hbm.bitumen_bucket\": \"Bitumen Bucket\""));
        assertTrue(lang.contains("\"item.hbm.smear_bucket\": \"Smear Bucket\""));
        assertTrue(lang.contains("\"item.hbm.heatingoil_bucket\": \"Heating Oil Bucket\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_fraction_tower\""));
        assertTrue(pickaxe.contains("\"hbm:fraction_spacer\""));
        assertTrue(ironTool.contains("\"hbm:machine_fraction_tower\""));
        assertTrue(ironTool.contains("\"hbm:fraction_spacer\""));

        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_fraction_tower.json"))
                .contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/fraction_spacer.json"))
                .contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/fraction_tower_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_fraction_tower.json"))
                .contains("fraction_tower.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/fraction_spacer.json"))
                .contains("fraction_spacer.obj"));

        String towerCraft = Files.readString(ROOT.resolve("data/hbm/recipes/machine_fraction_tower.json"));
        assertTrue(towerCraft.contains("hbm:plate_welded"));
        assertTrue(towerCraft.contains("hbm:steel_grate"));
        String spacerCraft = Files.readString(ROOT.resolve("data/hbm/recipes/fraction_spacer.json"));
        assertTrue(spacerCraft.contains("minecraft:iron_bars"));
        assertTrue(spacerCraft.contains("hbm:shell"));

        byte[] tower = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/fraction_tower.obj"));
        byte[] spacer = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/fraction_spacer.obj"));
        assertTrue(tower[0] != (byte) 0xEF);
        assertTrue(spacer[0] != (byte) 0xEF);
        String towerObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/fraction_tower.obj"),
                StandardCharsets.UTF_8);
        String spacerObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/fraction_spacer.obj"),
                StandardCharsets.UTF_8);
        assertTrue(towerObj.startsWith("mtllib "));
        assertTrue(spacerObj.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(towerObj));
        assertTrue(objFacesFitVertices(spacerObj));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/models/machines/fraction_tower.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/models/machines/fraction_spacer.png")));
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
