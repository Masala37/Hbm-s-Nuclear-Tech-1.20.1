package com.hbm.inventory.recipes;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericRecipeLoaderTest {
    @Test
    void parsesInlineRecipe() {
        String json = """
                {"recipes":[{"name":"ass.plateiron","duration":60,"power":100,
                "inputItem":[{"ore":"ingotIron","count":1}],
                "outputItem":[{"item":"hbm:plate_iron","count":1}]}]}
                """;
        List<GenericMachineRecipe> recipes = GenericRecipeLoader.read(json);
        assertEquals(1, recipes.size());
        GenericMachineRecipe plate = recipes.get(0);
        assertEquals("ass.plateiron", plate.name());
        assertEquals(60, plate.duration());
        assertEquals(100L, plate.power());
        assertEquals("ingotIron", plate.inputItem().get(0).ore());
        assertEquals("hbm:plate_iron", plate.outputItem().get(0).item());
    }

    @Test
    void assemblyDumpIsCopiedFromLegacy() throws IOException {
        AssemblyMachineRecipes.loadFromSourceTree();
        List<GenericMachineRecipe> recipes = AssemblyMachineRecipes.recipes();
        assertTrue(recipes.size() >= 100);
        GenericMachineRecipe plate = recipes.stream()
                .filter(r -> "ass.plateiron".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertEquals(60, plate.duration());
        assertEquals(100L, plate.power());
        assertEquals("ingotIron", plate.inputItem().get(0).ore());
        assertTrue(recipes.stream().noneMatch(r -> r.name().contains("OpenComputers")));
    }

    @Test
    void chemicalPlantDumpIsCopiedFromLegacy() throws IOException {
        ChemicalPlantRecipes.loadFromSourceTree();
        List<GenericMachineRecipe> recipes = ChemicalPlantRecipes.recipes();
        assertTrue(recipes.size() >= 70);
        GenericMachineRecipe hydrogen = recipes.stream()
                .filter(r -> "chem.hydrogen".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertEquals(20, hydrogen.duration());
        assertEquals(400L, hydrogen.power());
        assertEquals("hbm:water", hydrogen.inputFluid().get(0).fluid());
        assertEquals(8000, hydrogen.inputFluid().get(0).amount());
        assertEquals("hbm:hydrogen", hydrogen.outputFluid().get(0).fluid());
    }

    @Test
    void assemblyDumpHasChipOutputItem() throws IOException {
        AssemblyMachineRecipes.loadFromSourceTree();
        GenericMachineRecipe chip = AssemblyMachineRecipes.recipes().stream()
                .filter(r -> "ass.chip".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertEquals("hbm:circuit_chip", chip.outputItem().get(0).item());
        assertEquals(1, chip.outputItem().get(0).count());
        assertTrue(AssemblyMachineRecipes.recipes().stream().anyMatch(r ->
                "ass.analogAlt".equals(r.name())
                        && "hbm:circuit_analog".equals(r.outputItem().get(0).item())));
    }

    @Test
    void chemicalPlantDumpHasDynamiteSandAndRocketFuel() throws IOException {
        ChemicalPlantRecipes.loadFromSourceTree();
        GenericMachineRecipe dynamite = ChemicalPlantRecipes.recipes().stream()
                .filter(r -> "chem.dynamite".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertTrue(dynamite.inputItem().stream().anyMatch(in -> "KEY_SAND".equals(in.ore())));
        assertEquals("hbm:ball_dynamite", dynamite.outputItem().get(0).item());
        GenericMachineRecipe rocket = ChemicalPlantRecipes.recipes().stream()
                .filter(r -> "chem.rocketfuel".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertTrue(rocket.inputFluid().stream().anyMatch(in -> "hbm:petroleum".equals(in.fluid()) && in.amount() == 200));
        GenericMachineRecipe cordite = ChemicalPlantRecipes.recipes().stream()
                .filter(r -> "chem.cordite".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertTrue(cordite.inputFluid().stream().anyMatch(in -> "hbm:gas".equals(in.fluid()) && in.amount() == 200));
        GenericMachineRecipe tatb = ChemicalPlantRecipes.recipes().stream()
                .filter(r -> "chem.tatb".equals(r.name()))
                .findFirst()
                .orElseThrow();
        assertTrue(tatb.inputFluid().stream().anyMatch(in -> "hbm:sourgas".equals(in.fluid()) && in.amount() == 200));
    }
}
