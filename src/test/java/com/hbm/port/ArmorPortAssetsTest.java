package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private static final String[] PIECES = {
            "hazmat_helmet", "hazmat_plate", "hazmat_legs", "hazmat_boots",
            "hazmat_helmet_red", "hazmat_plate_red", "hazmat_legs_red", "hazmat_boots_red",
            "hazmat_helmet_grey", "hazmat_plate_grey", "hazmat_legs_grey", "hazmat_boots_grey"
    };
    private static final String[] LAYERS = {
            "models/armor/hazmat_layer_1.png", "models/armor/hazmat_layer_2.png",
            "models/armor/hazmat_red_layer_1.png", "models/armor/hazmat_red_layer_2.png",
            "models/armor/hazmat_grey_layer_1.png", "models/armor/hazmat_grey_layer_2.png",
            "armor/hazmat_1.png", "armor/hazmat_2.png",
            "armor/hazmat_1_red.png", "armor/hazmat_2_red.png",
            "armor/hazmat_1_grey.png", "armor/hazmat_2_grey.png",
            "models/ModelHazRed.png", "models/ModelHazGrey.png",
            "misc/overlay_hazmat.png"
    };

    @Test
    void wornTexturesAndItemModelsExist() throws IOException {
        for (String id : PIECES) {
            Path texture = ROOT.resolve("assets/hbm/textures/item/" + id + ".png");
            assertTrue(Files.isRegularFile(texture), "missing item texture " + id);
            Path model = ROOT.resolve("assets/hbm/models/item/" + id + ".json");
            assertTrue(Files.isRegularFile(model), "missing model " + id);
            String json = Files.readString(model);
            assertTrue(json.contains("hbm:item/" + id), id + " should use its texture");
        }
        for (String layer : LAYERS) {
            Path path = ROOT.resolve("assets/hbm/textures/" + layer);
            assertTrue(Files.isRegularFile(path), "missing worn texture " + layer);
        }
    }

    @Test
    void copiedOneSevenCraftsExist() throws IOException {
        Path recipes = ROOT.resolve("data/hbm/recipes");
        for (String id : PIECES) {
            String json = Files.readString(recipes.resolve(id + ".json"));
            assertTrue(json.contains("\"item\": \"hbm:" + id + "\""), id + " result");
        }
        String yellow = Files.readString(recipes.resolve("hazmat_helmet.json"));
        assertTrue(yellow.contains("hbm:hazmat_cloth"));
        assertTrue(yellow.contains("minecraft:glass_panes"));
        assertTrue(yellow.contains("hbm:plate_iron"));
        String redCloth = Files.readString(recipes.resolve("hazmat_cloth_red.json"));
        assertTrue(redCloth.contains("minecraft:redstone"));
        String greyCloth = Files.readString(recipes.resolve("hazmat_cloth_grey.json"));
        assertTrue(greyCloth.contains("hbm:hazmat_cloth_red"));
        assertTrue(greyCloth.contains("hbm:ingot_rubber"));
        assertTrue(greyCloth.contains("hbm:plate_lead"));
    }

    @Test
    void materialsMatchOneSevenHazmat() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/hbm/item/HbmArmorMaterials.java"));
        assertTrue(source.contains("HAZMAT(\"hazmat\", 60, new int[]{1, 4, 5, 2}, 5"));
        assertTrue(source.contains("HAZMAT_RED(\"hazmat_red\", 60, new int[]{1, 4, 5, 2}, 5"));
        assertTrue(source.contains("HAZMAT_GREY(\"hazmat_grey\", 60, new int[]{1, 4, 5, 2}, 5"));
        assertTrue(source.contains("\"hazmat_cloth\""));
        assertTrue(source.contains("\"hazmat_cloth_red\""));
        assertTrue(source.contains("\"hazmat_cloth_grey\""));
    }

    @Test
    void resistanceAndSetChecksMatchOneSeven() throws IOException {
        String registry = Files.readString(Path.of("src/main/java/com/hbm/handler/HazmatRegistry.java"));
        assertTrue(registry.contains("double hazYellow = 0.6D"));
        assertTrue(registry.contains("double hazRed = 1.0D"));
        assertTrue(registry.contains("double hazGray = 2.0D"));
        String armor = Files.readString(Path.of("src/main/java/com/hbm/util/ArmorUtil.java"));
        assertTrue(armor.contains("HAZMAT_HELMET_RED"));
        assertTrue(armor.contains("HAZMAT_HELMET_GREY"));
        int haz2 = armor.indexOf("checkForHaz2(Player player)");
        assertTrue(haz2 >= 0);
        String haz2Method = armor.substring(haz2, Math.min(armor.length(), haz2 + 120));
        assertTrue(haz2Method.contains("return false"));
        assertFalse(haz2Method.contains("HAZMAT_HELMET_RED"));
        String skip = Files.readString(Path.of("src/main/java/com/hbm/registry/ModBulkContent.java"));
        assertTrue(skip.contains("\"hazmat_kit\""));
        String items = Files.readString(Path.of("src/main/java/com/hbm/registry/ModItems.java"));
        assertTrue(items.contains("new HazmatKitItem"));
        assertTrue(items.contains("HazmatArmorItem"));
        String working = Files.readString(Path.of("src/main/java/com/hbm/port/PortContentRegistry.java"));
        assertTrue(working.contains("\"hazmat_kit\""));
    }
}
