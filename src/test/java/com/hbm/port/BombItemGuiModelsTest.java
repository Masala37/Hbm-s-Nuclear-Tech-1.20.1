package com.hbm.port;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BombItemGuiModelsTest {
    private static final Path ITEMS = Path.of("src/main/resources/assets/hbm/models/item");

    /**
     * OBJ bombs that used 1.7 {@code ItemRenderBase} inventory poses. JSON display
     * must stay identity so {@code BombItemRenderer} owns scale and centering.
     */
    static final String[] ISTER_BOMBS = {
            "nuke_boy", "nuke_man", "nuke_gadget", "nuke_mike", "nuke_tsar",
            "nuke_fleija", "nuke_solinium", "nuke_fstbmb", "nuke_n2",
            "nuke_prototype", "nuke_custom", "bomb_multi",
            "mine_ap", "mine_he", "mine_shrap", "mine_naval", "mine_fat",
            "crashed_bomb"
    };

    @Test
    void objBombItemModelsUseIdentityBewlr() throws IOException {
        for (String id : ISTER_BOMBS) {
            Path json = ITEMS.resolve(id + ".json");
            assertTrue(Files.isRegularFile(json), "missing item model " + id);
            JsonObject root = JsonParser.parseString(Files.readString(json)).getAsJsonObject();
            assertTrue(root.has("parent"), id + " missing parent");
            String parent = root.get("parent").getAsString();
            assertTrue("hbm:item/missile_bewlr".equals(parent)
                            || "minecraft:builtin/entity".equals(parent),
                    id + " must be builtin/entity ISTER, was " + parent);
        }
    }

    @Test
    void leftoverJsonGuiScalesDoNotOverflowSlot() throws IOException {
        String[] cubes = {
                "tnt", "c4", "dynamite", "semtex", "det_charge", "det_nuke", "det_miner"
        };
        for (String id : cubes) {
            Path json = ITEMS.resolve(id + ".json");
            assertTrue(Files.isRegularFile(json), "missing item model " + id);
            JsonObject root = JsonParser.parseString(Files.readString(json)).getAsJsonObject();
            if (!root.has("display")) {
                continue;
            }
            JsonObject gui = root.getAsJsonObject("display").getAsJsonObject("gui");
            if (gui == null || !gui.has("scale")) {
                continue;
            }
            double scale = gui.getAsJsonArray("scale").get(0).getAsDouble();
            assertTrue(scale <= 0.7, id + " cube gui scale " + scale + " overflows a slot");
        }
    }

    @Test
    void missileBewlrIsIdentity() throws IOException {
        JsonObject root = JsonParser.parseString(
                Files.readString(ITEMS.resolve("missile_bewlr.json"))).getAsJsonObject();
        assertEquals("minecraft:builtin/entity", root.get("parent").getAsString());
        JsonObject gui = root.getAsJsonObject("display").getAsJsonObject("gui");
        assertEquals(1.0, gui.getAsJsonArray("scale").get(0).getAsDouble(), 0.001);
        assertEquals(0.0, gui.getAsJsonArray("translation").get(0).getAsDouble(), 0.001);
        assertEquals(0.0, gui.getAsJsonArray("rotation").get(0).getAsDouble(), 0.001);
    }
}
