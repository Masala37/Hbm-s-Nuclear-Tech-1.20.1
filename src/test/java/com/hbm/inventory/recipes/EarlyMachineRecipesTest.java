package com.hbm.inventory.recipes;

import com.hbm.blocks.DummyableMeta;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EarlyMachineRecipesTest {
    @Test
    void anvilDumpHasSmithingLoopAndConstruction() throws IOException {
        AnvilRecipes.loadFromSourceTree();
        assertTrue(AnvilRecipes.getConstruction().size() >= 80);
        assertTrue(AnvilRecipes.getSmithing().stream().anyMatch(recipe ->
                "hbm:anvil_iron".equals(recipe.left.item())
                        && "ingotSteel".equals(recipe.right.ore())
                        && "hbm:anvil_steel".equals(recipe.output.item())));
    }

    @Test
    void pressDumpPlatesIronIngots() throws IOException {
        PressRecipes.loadFromSourceTree();
        assertTrue(PressRecipes.recipes().stream().anyMatch(recipe ->
                recipe.stamp() == StampType.PLATE
                        && "ingotIron".equals(recipe.input().ore())
                        && "hbm:plate_iron".equals(recipe.output().item())));
    }

    @Test
    void diFurnaceDumpMakesSteel() throws IOException {
        DiFurnaceRecipes.loadFromSourceTree();
        assertTrue(DiFurnaceRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:ingot_steel".equals(recipe.output().item())));
    }

    @Test
    void blastFurnaceDumpKeepsSteelFromIngot() throws IOException {
        BlastFurnaceRecipes.loadFromSourceTree();
        assertTrue(BlastFurnaceRecipes.recipes().stream().anyMatch(recipe ->
                "blast.steelFromIngot".equals(recipe.name())
                        && "hbm:ingot_steel".equals(recipe.outputs().get(0).item())));
    }

    @Test
    void shredderDumpIsCopiedFromLegacy() throws IOException {
        ShredderRecipes.loadFromSourceTree();
        assertTrue(ShredderRecipes.recipes().size() >= 100);
        assertTrue(ShredderRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:ore_aluminium".equals(recipe.input().item())
                        && "hbm:chunk_ore_cryolite".equals(recipe.output().item())
                        && recipe.output().count() == 2));
        assertTrue(ShredderRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:chunk_ore_rare".equals(recipe.input().item())
                        && "hbm:powder_desh_mix".equals(recipe.output().item())));
    }

    @Test
    void centrifugeDumpSplitsIronOre() throws IOException {
        CentrifugeRecipes.loadFromSourceTree();
        assertTrue(CentrifugeRecipes.recipes().size() >= 40);
        assertTrue(CentrifugeRecipes.recipes().stream().anyMatch(recipe ->
                "oreIron".equals(recipe.input().ore())
                        && recipe.output().stream().anyMatch(out -> "hbm:powder_iron".equals(out.item()))));
        assertTrue(CentrifugeRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:crystal_iron".equals(recipe.input().item())));
        assertTrue(CentrifugeRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:chunk_ore_rare".equals(recipe.input().item())
                        && recipe.output().stream().anyMatch(out -> "hbm:powder_cobalt_tiny".equals(out.item()))));
        assertTrue(CentrifugeRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:crystal_aluminium".equals(recipe.input().item())
                        && recipe.output().stream().anyMatch(out -> "hbm:chunk_ore_cryolite".equals(out.item()))));
        assertTrue(CentrifugeRecipes.recipes().stream().anyMatch(recipe ->
                "oreAluminum".equals(recipe.input().ore())
                        && recipe.output().stream().anyMatch(out ->
                        "hbm:chunk_ore_cryolite".equals(out.item()) && out.count() == 2)));
    }

    @Test
    void gasCentrifugeDumpHasNuf6AndUf6Conversion() throws IOException {
        GasCentrifugeRecipes.loadFromSourceTree();
        assertTrue(GasCentrifugeRecipes.types().containsKey("NUF6"));
        assertEquals(400, GasCentrifugeRecipes.byName("NUF6").fluidConsumed());
        assertEquals(300, GasCentrifugeRecipes.byName("NUF6").fluidProduced());
        assertEquals("LEUF6", GasCentrifugeRecipes.byName("NUF6").outputType().name());
        assertTrue(GasCentrifugeRecipes.byName("HEUF6").highSpeed());
        assertTrue(GasCentrifugeRecipes.types().containsKey("PF6"));
        assertTrue(GasCentrifugeRecipes.types().containsKey("MUD"));
    }

    @Test
    void silexDumpHasUraniumVisibleAndUf6Translation() throws IOException {
        SILEXRecipes.loadFromSourceTree();
        assertTrue(SILEXRecipes.recipes().stream().anyMatch(recipe ->
                "ingotUranium".equals(recipe.input().ore())
                        && recipe.fluidProduced() == 900
                        && recipe.fluidConsumed() == 100
                        && recipe.laser() == EnumWavelengths.VISIBLE));
        assertTrue(SILEXRecipes.recipes().stream().anyMatch(recipe ->
                "minecraft:lapis_lazuli".equals(recipe.input().item())
                        && recipe.laser() == EnumWavelengths.IR
                        && recipe.fluidProduced() == 100
                        && recipe.fluidConsumed() == 100));
        String json = Files.readString(SILEXRecipes.SOURCE_PATH);
        assertTrue(json.contains("\"fromFluid\": \"hbm:uf6\""));
        assertTrue(json.contains("\"item\": \"hbm:ingot_uranium\""));
        assertTrue(json.contains("\"fluid\": \"hbm:death\""));
    }

    @Test
    void oreDictSplitMapsIronIngot() {
        OreDictMatch.Split split = OreDictMatch.split("ingotIron");
        assertEquals("ingot", split.prefix());
        assertEquals("Iron", split.material());
        assertTrue(OreDictMatch.candidateIdStrings("ingotIron").contains("minecraft:iron_ingot"));
        assertTrue(OreDictMatch.candidateIdStrings("ingotSteel").contains("hbm:ingot_steel"));
        assertTrue(OreDictMatch.candidateIdStrings("shellSteel").contains("hbm:shell"));
        assertTrue(OreDictMatch.candidateIdStrings("wireFineLead").contains("hbm:wire_lead"));
        assertTrue(OreDictMatch.candidateIdStrings("plateCastSteel").contains("hbm:plate_cast_steel"));
        assertTrue(OreDictMatch.candidateIdStrings("plateSextupleSteel").contains("hbm:plate_welded"));
        assertTrue(OreDictMatch.candidateIdStrings("KEY_GREEN").contains("minecraft:green_dye"));
        assertTrue(OreDictMatch.candidateIdStrings("boltTungsten").contains("hbm:bolt_tungsten"));
        assertTrue(OreDictMatch.candidateIdStrings("oreRareEarth").contains("hbm:ore_rare"));
        assertTrue(OreDictMatch.candidateIdStrings("oreCinnabar").contains("hbm:ore_cinnebar"));
        assertTrue(OreDictMatch.candidateIdStrings("oreSaltpeter").contains("hbm:ore_niter"));
        assertTrue(OreDictMatch.candidateIdStrings("dustSaltpeter").contains("hbm:niter"));
        assertTrue(OreDictMatch.candidateIdStrings("oreThorium232").contains("hbm:ore_thorium"));
        assertTrue(OreDictMatch.candidateIdStrings("blockRedstone").contains("minecraft:redstone_block"));
        assertTrue(OreDictMatch.candidateIdStrings("dustSulfur").contains("hbm:sulfur"));
        assertTrue(OreDictMatch.candidateIdStrings("wireDenseMingrade").contains("hbm:wire_dense_red_copper"));
        assertTrue(OreDictMatch.candidateIdStrings("wireDenseMingrade").contains("hbm:wire_dense"));
        assertTrue(OreDictMatch.candidateIdStrings("ingotAnyHighexplosive").contains("hbm:ball_tnt"));
        assertTrue(OreDictMatch.candidateIdStrings("nuggetThorium232").contains("hbm:nugget_th232"));
        assertTrue(OreDictMatch.candidateIdStrings("billetGold198").contains("hbm:billet_au198"));
        assertTrue(OreDictMatch.candidateIdStrings("oreLimestone").contains("hbm:stone_resource_limestone"));
        assertTrue(OreDictMatch.candidateIdStrings("oreHematite").contains("hbm:stone_resource_hematite"));
        assertTrue(OreDictMatch.candidateIdStrings("ingotMalachite").contains("hbm:chunk_ore_malachite"));
        assertTrue(OreDictMatch.candidateIdStrings("ingotRareEarth").contains("hbm:chunk_ore_rare"));
        assertTrue(OreDictMatch.candidateIdStrings("crystalCryolite").contains("hbm:chunk_ore_cryolite"));
        assertTrue(OreDictMatch.candidateIdStrings("oreRareEarth").contains("hbm:ore_gneiss_rare"));
        assertTrue(OreDictMatch.candidateIdStrings("oreAluminum").contains("hbm:ore_aluminium"));
        assertTrue(OreDictMatch.candidateIdStrings("gemBauxite").contains("hbm:stone_resource_bauxite"));
    }

    @Test
    void pressAndBlastDummyCountsMatchOneSeven() {
        assertEquals(2, MultiblockHandlerXR.cellCount(new int[]{2, 0, 0, 0, 0, 0}, DummyableMeta.SOUTH));
        assertEquals(3, MultiblockHandlerXR.cellCount(new int[]{3, 0, 0, 0, 0, 0}, DummyableMeta.SOUTH));
        assertEquals(53, MultiblockHandlerXR.cellCount(new int[]{5, 0, 1, 1, 1, 1}, DummyableMeta.SOUTH));
        assertEquals(62, MultiblockHandlerXR.cellCount(new int[]{6, 0, 1, 1, 1, 1}, DummyableMeta.SOUTH));
        assertEquals(26, MultiblockHandlerXR.cellCount(new int[]{2, 0, 1, 1, 1, 1}, DummyableMeta.SOUTH));
        assertEquals(8, MultiblockHandlerXR.cellCount(new int[]{0, 0, 1, 1, 1, 1}, DummyableMeta.SOUTH));
        assertEquals(7, MultiblockHandlerXR.cellCount(new int[]{1, 0, 1, 0, 1, 0}, DummyableMeta.SOUTH));
        assertEquals(27, MultiblockHandlerXR.cellCount(new int[]{3, 0, 0, 0, 0, 6}, DummyableMeta.SOUTH));
        assertEquals(80, MultiblockHandlerXR.cellCount(new int[]{8, 0, 1, 1, 1, 1}, DummyableMeta.SOUTH));
        assertEquals(41, MultiblockHandlerXR.cellCount(new int[]{0, 0, 3, 3, 2, 3}, DummyableMeta.SOUTH));
        assertEquals(3, MultiblockHandlerXR.cellCount(new int[]{0, 0, 1, 0, 1, 0}, DummyableMeta.SOUTH));
        assertEquals(11, MultiblockHandlerXR.cellCount(new int[]{1, 0, 1, 0, 1, 1}, DummyableMeta.SOUTH));
        assertEquals(124, MultiblockHandlerXR.cellCount(new int[]{4, 0, 2, 2, 2, 2}, DummyableMeta.SOUTH));
    }

    @Test
    void solderingDumpMakesAnalog() throws IOException {
        SolderingRecipes.loadFromSourceTree();
        assertTrue(SolderingRecipes.recipes().size() >= 20);
        assertTrue(SolderingRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:circuit_analog".equals(recipe.output().item())
                        && recipe.duration() == 100
                        && recipe.consumption() == 100
                        && recipe.solder().stream().anyMatch(in -> "wireFineLead".equals(in.ore()))));
        assertTrue(OreDictMatch.candidateIdStrings("wireFineLead").contains("hbm:wire_lead"));
        AnvilRecipes.loadFromSourceTree();
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.output.stream().anyMatch(out -> "hbm:machine_soldering_station".equals(out.item()))));
    }

    @Test
    void crystallizerDumpMakesIronCrystal() throws IOException {
        CrystallizerRecipes.loadFromSourceTree();
        assertTrue(CrystallizerRecipes.recipes().size() >= 30);
        assertTrue(CrystallizerRecipes.recipes().stream().anyMatch(recipe ->
                "oreIron".equals(recipe.input().ore())
                        && "hbm:crystal_iron".equals(recipe.output().item())
                        && recipe.duration() == 600
                        && recipe.acidAmount() == 500
                        && "hbm:peroxide".equals(recipe.fluid().fluid())));
    }

    @Test
    void mixerDumpMakesSulfuricAndKeroseneReform() throws IOException {
        MixerRecipes.loadFromSourceTree();
        assertTrue(MixerRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:sulfuric_acid".equals(recipe.output().fluid())
                        && recipe.output().amount() == 500
                        && recipe.duration() == 50
                        && recipe.input1() != null
                        && "hbm:peroxide".equals(recipe.input1().fluid())
                        && recipe.input1().amount() == 800
                        && recipe.solid() != null
                        && "dustSulfur".equals(recipe.solid().ore())));
        assertTrue(MixerRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:kerosene_reform".equals(recipe.output().fluid())
                        && recipe.output().amount() == 1000
                        && recipe.input1() != null
                        && "hbm:kerosene".equals(recipe.input1().fluid())
                        && recipe.input2() != null
                        && "hbm:reformate".equals(recipe.input2().fluid())));
    }

    @Test
    void arcWelderDumpMakesMotors() throws IOException {
        ArcWelderRecipes.loadFromSourceTree();
        assertTrue(ArcWelderRecipes.recipes().stream().anyMatch(recipe ->
                "hbm:motor".equals(recipe.output().item())
                        && recipe.output().count() == 2
                        && recipe.duration() == 100
                        && recipe.consumption() == 400
                        && recipe.inputs().stream().anyMatch(in -> "plateSteel".equals(in.ore()))
                        && recipe.inputs().stream().anyMatch(in -> "wireDenseMingrade".equals(in.ore()))));
        AnvilRecipes.loadFromSourceTree();
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.output.stream().anyMatch(out -> "hbm:machine_arc_welder".equals(out.item()))));
    }

    @Test
    void anvilDumpHasAssemblyMachineConstruction() throws IOException {
        AnvilRecipes.loadFromSourceTree();
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.output.stream().anyMatch(out -> "hbm:machine_assembly_machine".equals(out.item()))));
    }

    @Test
    void chemplantDumpHasHydrogenFromCoal() throws IOException {
        ChemicalPlantRecipes.loadFromSourceTree();
        assertTrue(ChemicalPlantRecipes.recipes().stream().anyMatch(recipe ->
                "chem.hydrogen".equals(recipe.name())
                        && recipe.inputItem().stream().anyMatch(in -> "gemCoal".equals(in.ore()))
                        && recipe.outputFluid().stream().anyMatch(out -> "hbm:hydrogen".equals(out.fluid()))));
    }

    @Test
    void purexDumpHasUzhAndPileRecycle() throws IOException {
        PUREXRecipes.loadFromSourceTree();
        assertTrue(PUREXRecipes.recipes().stream().anyMatch(recipe ->
                "purex.uzh".equals(recipe.name())
                        && recipe.duration() == 600
                        && recipe.power() == 1000
                        && recipe.inputItem().stream().anyMatch(in -> "hbm:billet_uranium_fuel".equals(in.item()))
                        && recipe.inputItem().stream().anyMatch(in -> "billetZirconium".equals(in.ore()))
                        && recipe.outputItem().stream().anyMatch(out -> "hbm:billet_uzh".equals(out.item()) && out.count() == 4)));
        assertTrue(PUREXRecipes.recipes().stream().anyMatch(recipe ->
                "purex.pilepu".equals(recipe.name())
                        && recipe.inputItem().stream().anyMatch(in -> "hbm:pile_rod_plutonium".equals(in.item()))
                        && recipe.inputFluid().stream().anyMatch(in -> "hbm:sulfuric_acid".equals(in.fluid()))));
        assertTrue(PUREXRecipes.recipes().stream().anyMatch(recipe ->
                "purex.thoriumsalt".equals(recipe.name())
                        && recipe.outputItem().stream().anyMatch(out -> out.chance() == 0.5F)));
    }

    @Test
    void assemblerDumpIdsMapOntoPortBlocks() {
        assertEquals("hbm:diesel_generator", GenericRecipeMatch.portId("hbm:machine_diesel"));
        assertEquals("hbm:combustion_generator", GenericRecipeMatch.portId("hbm:machine_combustion_engine"));
        assertEquals("hbm:electric_furnace", GenericRecipeMatch.portId("hbm:machine_electric_furnace_off"));
        assertEquals("hbm:machine_rtg", GenericRecipeMatch.portId("hbm:machine_rtg"));
    }

    @Test
    void oreDictMapsSandPipesAndConcrete() {
        assertTrue(OreDictMatch.candidateIdStrings("KEY_SAND").contains("minecraft:sand"));
        assertTrue(OreDictMatch.candidateIdStrings("ntmpipeSteel").contains("hbm:pipe_steel"));
        assertTrue(OreDictMatch.candidateIdStrings("ntmpipeSteel").contains("hbm:pipe"));
        assertTrue(OreDictMatch.candidateIdStrings("anyConcrete").contains("hbm:concrete"));
        assertTrue(OreDictMatch.candidateIdStrings("anyConcrete").contains("hbm:concrete_smooth"));
        assertTrue(OreDictMatch.candidateIdStrings("shellSteel").contains("hbm:shell_steel"));
    }

    @Test
    void pressDumpHasCircuitSiliconAndSteelWire() throws IOException {
        PressRecipes.loadFromSourceTree();
        assertTrue(PressRecipes.recipes().stream().anyMatch(recipe ->
                recipe.stamp() == StampType.CIRCUIT
                        && "billetSilicon".equals(recipe.input().ore())
                        && "hbm:circuit_silicon".equals(recipe.output().item())));
        assertTrue(PressRecipes.recipes().stream().anyMatch(recipe ->
                recipe.stamp() == StampType.WIRE
                        && "ingotSteel".equals(recipe.input().ore())
                        && "hbm:wire_steel".equals(recipe.output().item())
                        && recipe.output().count() == 8));
    }

    @Test
    void anvilDumpHasSteelPipeAndShell() throws IOException {
        AnvilRecipes.loadFromSourceTree();
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.tierLower == 2
                        && recipe.overlay == AnvilRecipes.OverlayType.RECYCLING
                        && recipe.input.stream().anyMatch(in -> "hbm:chunk_ore_rare".equals(in.item()))
                        && recipe.output.stream().anyMatch(out ->
                        "hbm:fragment_boron".equals(out.item()) && out.chance() == 0.5F)
                        && recipe.output.stream().anyMatch(out ->
                        "hbm:fragment_lanthanium".equals(out.item()) && out.chance() == 0.1F)));
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.output.stream().anyMatch(out -> "hbm:pipe_steel".equals(out.item()))
                        && recipe.input.stream().anyMatch(in -> "plateSteel".equals(in.ore()) && in.count() == 3)));
        assertTrue(AnvilRecipes.getConstruction().stream().anyMatch(recipe ->
                recipe.output.stream().anyMatch(out -> "hbm:shell_steel".equals(out.item()))
                        && recipe.input.stream().anyMatch(in -> "plateSteel".equals(in.ore()) && in.count() == 4)));
    }

    @Test
    void workbenchCopiesOneSevenMissileBombCrafts() {
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/stick_dynamite.json")));
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/struct_launcher.json")));
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/struct_launcher_core.json")));
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/seg_10.json")));
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/plate_polymer_from_wool.json")));
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/safety_fuse.json")));
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/machine_missile_assembly.json")));
        assertTrue(Files.exists(java.nio.file.Path.of("src/main/resources/data/hbm/recipes/billet_silicon_from_nuggets.json")));
    }

    @Test
    void workbenchCopiesOneSevenSurvivalProgressionCrafts() throws IOException {
        Path recipes = Path.of("src/main/resources/data/hbm/recipes");
        String fireclay = Files.readString(recipes.resolve("ball_fireclay_from_dust.json"));
        assertTrue(fireclay.contains("hbm:powder_aluminium"));
        assertTrue(fireclay.contains("hbm:ball_fireclay"));
        String fireclayOre = Files.readString(recipes.resolve("ball_fireclay_from_ore.json"));
        assertTrue(fireclayOre.contains("hbm:ore_aluminium"));
        String fireclayLimestone = Files.readString(recipes.resolve("ball_fireclay_from_limestone.json"));
        assertTrue(fireclayLimestone.contains("hbm:stone_resource_limestone"));
        String firebrick = Files.readString(recipes.resolve("smelt_ball_fireclay.json"));
        assertTrue(firebrick.contains("hbm:ball_fireclay"));
        assertTrue(firebrick.contains("hbm:ingot_firebrick"));
        String coil = Files.readString(recipes.resolve("coil_copper_from_iron.json"));
        assertTrue(coil.contains("hbm:wire_red_copper"));
        assertTrue(coil.contains("hbm:coil_copper"));
        String motor = Files.readString(recipes.resolve("motor_from_iron.json"));
        assertTrue(motor.contains("hbm:coil_copper_torus"));
        assertTrue(motor.contains("\"count\": 2"));
        String battery = Files.readString(recipes.resolve("machine_battery.json"));
        assertTrue(battery.contains("hbm:plate_polymer"));
        assertTrue(battery.contains("hbm:coil_copper"));
        String crowbar = Files.readString(recipes.resolve("crowbar.json"));
        assertTrue(crowbar.contains("hbm:ingot_steel"));
        String steelPick = Files.readString(recipes.resolve("steel_pickaxe.json"));
        assertTrue(steelPick.contains("hbm:ingot_steel"));
        assertTrue(steelPick.contains("XXX"));
        String dwarven = Files.readString(recipes.resolve("dwarven_pickaxe.json"));
        assertTrue(dwarven.contains("hbm:ingot_copper"));
        assertTrue(dwarven.contains("minecraft:iron_ingot"));
        assertTrue(Files.exists(recipes.resolve("coil_copper_from_steel.json")));
        assertTrue(Files.exists(recipes.resolve("coil_gold_from_iron.json")));
        assertTrue(Files.exists(recipes.resolve("coil_tungsten_from_steel.json")));
        assertTrue(Files.exists(recipes.resolve("coil_magnetized_tungsten_from_iron.json")));
        assertTrue(Files.exists(recipes.resolve("coil_copper_torus_from_iron.json")));
        assertTrue(Files.exists(recipes.resolve("coil_gold_torus_from_steel.json")));
        assertTrue(Files.exists(recipes.resolve("motor_from_steel.json")));
        assertTrue(Files.exists(recipes.resolve("machine_battery_from_steel.json")));
        assertTrue(Files.exists(recipes.resolve("titanium_sword.json")));
        assertTrue(Files.exists(recipes.resolve("titanium_hoe.json")));
        String cobaltPick = Files.readString(recipes.resolve("cobalt_pickaxe.json"));
        assertTrue(cobaltPick.contains("hbm:ingot_cobalt"));
        assertTrue(cobaltPick.contains("XXX"));
        String cmbSword = Files.readString(recipes.resolve("cmb_sword.json"));
        assertTrue(cmbSword.contains("hbm:ingot_combine_steel"));
        String deshAxe = Files.readString(recipes.resolve("desh_axe.json"));
        assertTrue(deshAxe.contains("hbm:ingot_desh"));
        String starPick = Files.readString(recipes.resolve("starmetal_pickaxe.json"));
        assertTrue(starPick.contains("hbm:ring_starmetal"));
        assertTrue(starPick.contains("hbm:cobalt_decorated_pickaxe"));
        String schraSword = Files.readString(recipes.resolve("schrabidium_sword.json"));
        assertTrue(schraSword.contains("hbm:block_schrabidium"));
        assertTrue(schraSword.contains("hbm:desh_sword"));
        assertTrue(Files.exists(recipes.resolve("screwdriver.json")));
        assertTrue(Files.exists(recipes.resolve("hand_drill.json")));
        assertTrue(Files.exists(recipes.resolve("matchstick_from_sulfur.json")));
        assertTrue(AnvilRecipes.getSmithing().stream().anyMatch(recipe ->
                "hbm:cobalt_pickaxe".equals(recipe.left.item())
                        && "hbm:ingot_meteorite".equals(recipe.right.item())
                        && "hbm:cobalt_decorated_pickaxe".equals(recipe.output.item())));
    }
}
