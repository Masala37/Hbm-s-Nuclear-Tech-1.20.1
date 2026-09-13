package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiFurnaceN9PortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void extensionAndRtgMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.machine_difurnace_extension\": \"Blast Furnace Extension (LEGACY)\""));
        assertTrue(lang.contains("\"block.hbm.machine_difurnace_rtg_off\": \"Nuclear Blast Furnace (LEGACY)\""));
        assertTrue(lang.contains("\"container.diFurnaceRTG\": \"Nuclear Blast Furnace\""));
        assertTrue(lang.contains("\"desc.gui.rtgBFurnace.desc\""));
        assertTrue(lang.contains("\"desc.gui.rtg.pelletHeat\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        for (String id : new String[]{"hbm:machine_difurnace_extension", "hbm:machine_difurnace_rtg_off"}) {
            assertTrue(pickaxe.contains("\"" + id + "\""), id);
            assertTrue(ironTool.contains("\"" + id + "\""), id);
        }

        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/machine_difurnace_extension.json")));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/machine_difurnace_rtg_off.json")));

        byte[] obj = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/difurnace_extension.obj"));
        assertTrue(obj[0] != (byte) 0xEF, "difurnace_extension.obj must not have a UTF-8 BOM");
        String objText = Files.readString(ROOT.resolve("assets/hbm/models/obj/difurnace_extension.obj"));
        assertTrue(objText.startsWith("mtllib "));
        assertTrue(objText.contains("usemtl top"));
        assertTrue(objText.contains("usemtl bottom"));
        assertTrue(objText.contains("usemtl side"));
        assertTrue(objText.contains("v -0.250000 1.000000 0.250000"));
        assertTrue(objFacesFitVertices(objText));

        String mtl = Files.readString(ROOT.resolve("assets/hbm/models/obj/difurnace_extension.mtl"));
        assertTrue(mtl.contains("hbm:block/difurnace_top_off_alt"));
        assertTrue(mtl.contains("hbm:block/difurnace_side_alt"));

        String extModel = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_difurnace_extension.json"));
        assertTrue(extModel.contains("difurnace_extension.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_difurnace_rtg_off.json"))
                .contains("lit=true"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_difurnace_rtg_on.json"))
                .contains("rtg_difurnace_front_on"));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/gui/processing/gui_rtg_difurnace.png")));
        assertTrue(Files.exists(ROOT.resolve("data/hbm/loot_tables/blocks/machine_difurnace_extension.json")));
        assertTrue(Files.exists(ROOT.resolve("data/hbm/loot_tables/blocks/machine_difurnace_rtg_off.json")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/rtg_difurnace_front_off.png")));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_difurnace.json"))
                .contains("covered=true"));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/models/block/machine_difurnace_covered.json")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/difurnace_front_off_tall.png")));
        assertTrue(Files.exists(ROOT.resolve("assets/hbm/textures/block/difurnace_front_on_tall.png")));
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
