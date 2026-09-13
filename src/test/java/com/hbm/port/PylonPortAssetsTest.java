package com.hbm.port;

import com.hbm.blocks.DummyableMeta;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PylonPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void pylonAssetsAndCraftsMatchOneSeven() throws IOException {
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.red_connector\": \"Electricity Connector\""));
        assertTrue(lang.contains("\"block.hbm.red_pylon\": \"Electricity Pylon\""));
        assertTrue(lang.contains("\"block.hbm.red_pylon_large\": \"Large Electricity Pylon\""));
        assertTrue(lang.contains("\"block.hbm.red_pylon_medium_wood\": \"Medium Wooden Electricity Pylon\""));
        assertTrue(lang.contains("\"block.hbm.red_pylon_medium_wood_transformer\": \"Medium Wooden Electricity Pylon with Transformer\""));
        assertTrue(lang.contains("\"block.hbm.red_pylon_medium_steel\": \"Medium Steel Electricity Pylon\""));
        assertTrue(lang.contains("\"block.hbm.red_pylon_medium_steel_transformer\": \"Medium Steel Electricity Pylon with Transformer\""));
        assertTrue(lang.contains("\"item.hbm.wiring_red_copper\": \"Cable Drum\""));

        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        for (String id : new String[]{
                "hbm:red_connector", "hbm:red_pylon", "hbm:red_pylon_large", "hbm:substation",
                "hbm:red_pylon_medium_wood", "hbm:red_pylon_medium_wood_transformer",
                "hbm:red_pylon_medium_steel", "hbm:red_pylon_medium_steel_transformer"
        }) {
            assertTrue(pickaxe.contains("\"" + id + "\""), id);
            assertTrue(ironTool.contains("\"" + id + "\""), id);
        }

        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/red_connector.json")).contains("hbm:coil_copper"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/red_cable.json")).contains("hbm:wire_red_copper"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/red_wire_coated.json")).contains("hbm:ingot_red_copper"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/red_pylon.json")).contains("hbm:red_wire_coated"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/wiring_red_copper.json")).contains("hbm:plate_steel"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/red_pylon_medium_wood.json")).contains("minecraft:planks"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/red_pylon_medium_wood_transformer.json"))
                .contains("hbm:red_pylon_medium_wood"));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/red_pylon_medium_steel.json")));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/red_pylon_medium_steel_transformer.json")));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/red_connector_super.json")));

        AnvilRecipes.loadFromSourceTree();
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.output.stream().anyMatch(out -> "hbm:red_pylon_large".equals(out.item()))));
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.output.stream().anyMatch(out -> "hbm:substation".equals(out.item()))));

        assertEquals(6, MultiblockHandlerXR.cellCount(new int[]{6, 0, 0, 0, 0, 0}, DummyableMeta.SOUTH));
        assertEquals(125, MultiblockHandlerXR.cellCount(new int[]{13, 0, 1, 1, 1, 1}, DummyableMeta.SOUTH));
        assertEquals(74, MultiblockHandlerXR.cellCount(new int[]{4, 0, 1, 1, 2, 2}, DummyableMeta.SOUTH));

        String[] objs = {
                "assets/hbm/models/obj/network/connector.obj",
                "assets/hbm/models/obj/network/pylon_medium.obj",
                "assets/hbm/models/obj/network/pylon_medium_full.obj",
                "assets/hbm/models/obj/network/pylon_large.obj",
                "assets/hbm/models/obj/network/substation.obj"
        };
        for (String path : objs) {
            byte[] bytes = Files.readAllBytes(ROOT.resolve(path));
            assertTrue(bytes[0] != (byte) 0xEF, path + " must not have a UTF-8 BOM");
            String text = Files.readString(ROOT.resolve(path));
            assertTrue(text.startsWith("mtllib "), path);
            assertTrue(objFacesFitVertices(text), path);
        }
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/red_connector.json")).contains("facing=up"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/red_pylon_medium_wood.json")).contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/blockstates/red_pylon_large.json")).contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/red_connector.json")).contains("connector.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/red_pylon_medium_steel.json"))
                .contains("pylon_medium_steel"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/red_pylon_large.json")).contains("meta"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/substation.json")).contains("meta"));
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
