package com.hbm.port;

import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.OreDictMatch;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiloHatchPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void hatchAssetsHavePartsSoundsAndAssemblyRecipes() throws IOException {
        String small = Files.readString(ROOT.resolve("assets/hbm/models/obj/silo_hatch.obj"));
        String large = Files.readString(ROOT.resolve("assets/hbm/models/obj/silo_hatch_large.obj"));
        assertTrue(small.contains("o Hatch"));
        assertTrue(small.contains("o Frame"));
        assertTrue(large.contains("o Hatch"));
        assertTrue(large.contains("o Frame"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/doors/silo_hatch.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/doors/silo_hatch_large.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/sounds/block/door/door_wgh_big_start.ogg")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/sounds/block/door/door_wgh_big_stop.ogg")));
        String sounds = Files.readString(ROOT.resolve("assets/hbm/sounds.json"));
        assertTrue(sounds.contains("\"door.wgh_big_start\""));
        assertTrue(sounds.contains("\"door.wgh_big_stop\""));
        String catalog = Files.readString(Path.of("src/main/java/com/hbm/registry/SoundEventCatalog.java"));
        assertTrue(catalog.contains("\"door.wgh_big_start\""));
        String block = Files.readString(Path.of("src/main/java/com/hbm/blocks/generic/SiloHatchBlock.java"));
        assertTrue(block.contains("tryToggle"));
        assertTrue(block.contains("IBomb"));
        String be = Files.readString(Path.of("src/main/java/com/hbm/blockentity/machine/SiloHatchBlockEntity.java"));
        assertTrue(be.contains("STATE_OPENING"));
        String renderer = Files.readString(Path.of("src/main/java/com/hbm/client/render/blockentity/RenderSiloHatch.java"));
        assertTrue(renderer.contains("hatchPitch"));
        assertTrue(renderer.contains("\"Hatch\""));
        assertTrue(renderer.contains("\"Frame\""));
        String soundClient = Files.readString(Path.of("src/main/java/com/hbm/client/sound/ClientSiloHatchSounds.java"));
        assertTrue(soundClient.contains("door.wgh_big_start"));
        assertTrue(soundClient.contains("door.wgh_big_stop"));
    }

    @Test
    void assemblyDumpKeepsOneSevenSiloHatchRecipes() throws IOException {
        AssemblyMachineRecipes.loadFromSourceTree();
        GenericMachineRecipe small = AssemblyMachineRecipes.byName("ass.silohatch");
        GenericMachineRecipe large = AssemblyMachineRecipes.byName("ass.silohatchlarge");
        assertEquals("hbm:silo_hatch", small.outputItem().get(0).item());
        assertEquals(200, small.duration());
        assertEquals(100L, small.power());
        assertEquals("plateSextupleSteel", small.inputItem().get(0).ore());
        assertEquals("hbm:plate_polymer", small.inputItem().get(1).item());
        assertEquals("hbm:motor", small.inputItem().get(2).item());
        assertEquals("boltSteel", small.inputItem().get(3).ore());
        assertEquals("KEY_GREEN", small.inputItem().get(4).ore());
        assertEquals("hbm:silo_hatch_large", large.outputItem().get(0).item());
        assertEquals(300, large.duration());
        assertTrue(OreDictMatch.candidateIdStrings("plateSextupleSteel").contains("hbm:plate_welded_steel"));
        assertTrue(OreDictMatch.candidateIdStrings("plateSextupleSteel").contains("hbm:plate_welded"));
        assertTrue(OreDictMatch.candidateIdStrings("boltSteel").contains("hbm:bolt_steel"));
        assertTrue(OreDictMatch.candidateIdStrings("boltSteel").contains("hbm:bolt"));
        assertTrue(OreDictMatch.candidateIdStrings("KEY_GREEN").contains("minecraft:green_dye"));
        assertTrue(OreDictMatch.candidateIdStrings("dyeGreen").contains("minecraft:green_dye"));
        String match = Files.readString(Path.of("src/main/java/com/hbm/inventory/recipes/OreDictMatch.java"));
        assertTrue(match.contains("dyes/green"));
        assertTrue(match.contains("plateSextuple"));
    }
}
