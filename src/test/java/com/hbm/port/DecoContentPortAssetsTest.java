package com.hbm.port;

import com.hbm.util.NoteBuilder;
import com.hbm.util.NoteBuilder.Instrument;
import com.hbm.util.NoteBuilder.Note;
import com.hbm.util.NoteBuilder.Octave;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecoContentPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void doorRecipesAndSoundMatchOneSeven() throws IOException {
        String metal = Files.readString(ROOT.resolve("data/hbm/recipes/door_metal.json"));
        assertTrue(metal.contains("\"II\""));
        assertTrue(metal.contains("\"SS\""));
        assertTrue(metal.contains("hbm:plate_iron"));
        assertTrue(metal.contains("hbm:plate_steel"));
        assertTrue(metal.contains("hbm:door_metal"));

        String office = Files.readString(ROOT.resolve("data/hbm/recipes/door_office.json"));
        assertTrue(office.contains("minecraft:planks"));
        assertTrue(office.contains("hbm:plate_iron"));
        assertTrue(office.contains("hbm:door_office"));

        String bunker = Files.readString(ROOT.resolve("data/hbm/recipes/door_bunker.json"));
        assertTrue(bunker.contains("hbm:plate_steel"));
        assertTrue(bunker.contains("hbm:plate_lead"));
        assertTrue(bunker.contains("hbm:door_bunker"));

        String sounds = Files.readString(ROOT.resolve("assets/hbm/sounds.json"));
        assertTrue(sounds.contains("\"block.open_door\""));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/sounds/block/door_open_1.ogg")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/sounds/block/door_open_2.ogg")));
    }

    @Test
    void ntmDoorBlockstatesSwingOpenLikeVanilla() throws IOException {
        for (String door : new String[] {"door_metal", "door_office", "door_bunker"}) {
            String json = Files.readString(ROOT.resolve("assets/hbm/blockstates/" + door + ".json"))
                    .replaceAll("\\s+", "");
            assertDoorYaw(json, "facing=east,half=lower,hinge=left,open=false", 0);
            assertDoorYaw(json, "facing=east,half=lower,hinge=left,open=true", 90);
            assertDoorYaw(json, "facing=south,half=lower,hinge=left,open=true", 180);
            assertDoorYaw(json, "facing=west,half=lower,hinge=left,open=true", 270);
            assertDoorYaw(json, "facing=north,half=lower,hinge=left,open=true", 0);
            assertDoorYaw(json, "facing=east,half=lower,hinge=right,open=true", 270);
            assertDoorYaw(json, "facing=south,half=lower,hinge=right,open=true", 0);
            assertDoorYaw(json, "facing=west,half=lower,hinge=right,open=true", 90);
            assertDoorYaw(json, "facing=north,half=lower,hinge=right,open=true", 180);
            assertDoorYaw(json, "facing=east,half=upper,hinge=left,open=true", 90);
        }
    }

    private static void assertDoorYaw(String compactJson, String variantPrefix, int y) {
        String key = "\"" + variantPrefix + ",powered=false\"";
        int at = compactJson.indexOf(key);
        assertTrue(at >= 0, variantPrefix);
        int start = compactJson.indexOf('{', at);
        int end = compactJson.indexOf('}', start);
        String obj = compactJson.substring(start, end + 1);
        if (y == 0) {
            assertFalse(obj.contains("\"y\":"), variantPrefix + " should omit y, got " + obj);
        } else {
            assertTrue(obj.contains("\"y\":" + y), variantPrefix + " expected y=" + y + " got " + obj);
        }
    }

    @Test
    void radioRecipeAndAssetsMatchOneSeven() throws IOException {
        String recipe = Files.readString(ROOT.resolve("data/hbm/recipes/radiorec.json"));
        assertTrue(recipe.contains("\"  W\""));
        assertTrue(recipe.contains("\"PCP\""));
        assertTrue(recipe.contains("\"PIP\""));
        assertTrue(recipe.contains("hbm:wire_copper"));
        assertTrue(recipe.contains("hbm:plate_steel"));
        assertTrue(recipe.contains("hbm:circuit_vacuum_tube"));
        assertTrue(recipe.contains("hbm:ingot_polymer"));
        assertTrue(recipe.contains("hbm:ingot_bakelite"));
        assertTrue(recipe.contains("hbm:radiorec"));

        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.radiorec\": \"FM Radio\""));
        assertTrue(lang.contains("\"container.radiorec\": \"FM Radio\""));

        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/radio_receiver.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/gui/machine/gui_radio.png")));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/radiorec.json")).contains("radio_receiver"));
    }

    @Test
    void steelRoofRecipeIsThreeIngotsForTwo() throws IOException {
        String recipe = Files.readString(ROOT.resolve("data/hbm/recipes/steel_roof.json"));
        assertTrue(recipe.contains("\"SSS\""));
        assertTrue(recipe.contains("hbm:ingot_steel"));
        assertTrue(recipe.contains("hbm:steel_roof"));
        assertTrue(recipe.contains("\"count\": 2"));
        String model = Files.readString(ROOT.resolve("assets/hbm/models/block/template_steel_roof.json"));
        assertTrue(model.contains("[0, 0, 0]"));
        assertTrue(model.contains("[16, 1, 16]"));
    }

    @Test
    void lootPileUsesInvisibleModel() throws IOException {
        String model = Files.readString(ROOT.resolve("assets/hbm/models/block/deco_loot.json"));
        assertTrue(model.contains("\"elements\": []"));
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.deco_loot\": \"Loot Pile\""));
    }

    @Test
    void noteBuilderRoundTripsOneSevenEncoding() {
        String beat = NoteBuilder.start()
                .add(Instrument.BASSGUITAR, Note.C, Octave.LOW)
                .add(Instrument.PIANO, Note.D, Octave.MID)
                .end();
        assertEquals("4:6:0-0:8:1", beat);
        NoteBuilder.Hit[] hits = NoteBuilder.translate(beat);
        assertEquals(2, hits.length);
        assertEquals(Instrument.BASSGUITAR, hits[0].instrument());
        assertEquals(Note.C, hits[0].note());
        assertEquals(Octave.LOW, hits[0].octave());
        assertEquals(Instrument.PIANO, hits[1].instrument());
        assertEquals(Note.D, hits[1].note());
        assertEquals(Octave.MID, hits[1].octave());
    }
}
