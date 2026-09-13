package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EarlyMachinePortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void earlyMachineAssetsExist() throws IOException {
        String[] files = {
                "assets/hbm/models/obj/press_body.obj",
                "assets/hbm/models/obj/press_body.mtl",
                "assets/hbm/models/obj/press_head.obj",
                "assets/hbm/models/obj/press_head.mtl",
                "assets/hbm/models/obj/epress_body.obj",
                "assets/hbm/models/obj/epress_body.mtl",
                "assets/hbm/models/obj/epress_head.obj",
                "assets/hbm/models/obj/epress_head.mtl",
                "assets/hbm/models/obj/blast_furnace.obj",
                "assets/hbm/models/obj/blast_furnace.mtl",
                "assets/hbm/models/block/press_body.json",
                "assets/hbm/models/block/press_head.json",
                "assets/hbm/models/block/press_dummy.json",
                "assets/hbm/models/block/epress_body.json",
                "assets/hbm/models/block/epress_head.json",
                "assets/hbm/models/block/epress_dummy.json",
                "assets/hbm/models/block/blast_furnace.json",
                "assets/hbm/models/block/blast_furnace_dummy.json",
                "assets/hbm/models/block/machine_shredder.json",
                "assets/hbm/models/block/machine_difurnace.json",
                "assets/hbm/models/block/machine_difurnace_on.json",
                "assets/hbm/blockstates/machine_press.json",
                "assets/hbm/blockstates/machine_epress.json",
                "assets/hbm/blockstates/machine_shredder.json",
                "assets/hbm/blockstates/machine_difurnace.json",
                "assets/hbm/blockstates/machine_blast_furnace.json",
                "assets/hbm/models/item/machine_press.json",
                "assets/hbm/models/item/machine_epress.json",
                "assets/hbm/models/item/machine_shredder.json",
                "assets/hbm/models/item/machine_difurnace.json",
                "assets/hbm/models/item/machine_blast_furnace.json",
                "assets/hbm/textures/gui/processing/gui_anvil.png",
                "assets/hbm/textures/gui/gui_press.png",
                "assets/hbm/textures/gui/gui_epress.png",
                "assets/hbm/textures/gui/gui_shredder.png",
                "assets/hbm/textures/gui/gui_di_furnace.png",
                "assets/hbm/textures/gui/processing/gui_blast_furnace.png",
                "data/hbm/loot_tables/blocks/machine_press.json",
                "data/hbm/loot_tables/blocks/machine_epress.json",
                "data/hbm/loot_tables/blocks/machine_shredder.json",
                "data/hbm/loot_tables/blocks/machine_difurnace.json",
                "data/hbm/loot_tables/blocks/machine_blast_furnace.json",
                "data/hbm/loot_tables/blocks/machine_rtg.json",
                "assets/hbm/models/obj/rtg_gen.obj",
                "assets/hbm/models/obj/rtg_gen.mtl",
                "assets/hbm/models/obj/rtg_gen_item.obj",
                "assets/hbm/models/obj/rtg_gen_item.mtl",
                "assets/hbm/models/obj/rtg_connector.obj",
                "assets/hbm/models/obj/rtg_connector.mtl",
                "assets/hbm/models/block/rtg_gen.json",
                "assets/hbm/models/block/rtg_connector.json",
                "assets/hbm/blockstates/machine_rtg.json",
                "assets/hbm/models/item/machine_rtg.json",
                "assets/hbm/models/item/pellet_rtg_depleted.json",
                "assets/hbm/textures/gui/gui_rtg.png",
                "assets/hbm/textures/gui/gui_utility.png",
                "assets/hbm/textures/models/machines/rtg.png",
                "data/hbm/recipes/pellet_rtg.json",
                "data/hbm/recipes/pellet_rtg_weak.json",
                "data/hbm/recipes/pellet_rtg_radium.json",
                "data/hbm/recipes/pellet_rtg_strontium.json",
                "data/hbm/recipes/pellet_rtg_cobalt.json",
                "data/hbm/recipes/pellet_rtg_actinium.json",
                "data/hbm/recipes/pellet_rtg_polonium.json",
                "data/hbm/recipes/pellet_rtg_gold.json",
                "data/hbm/recipes/pellet_rtg_lead.json",
                "data/hbm/recipes/pellet_rtg_americium.json",
                "data/hbm/recipes/pellet_rtg_depleted_lead.json",
                "data/hbm/recipes/pellet_rtg_depleted_bismuth.json",
                "data/hbm/recipes/pellet_rtg_depleted_mercury.json",
                "data/hbm/recipes/pellet_rtg_depleted_neptunium.json",
                "data/hbm/recipes/pellet_rtg_depleted_zirconium.json",
                "data/hbm/recipes/machine_press.json",
                "data/hbm/recipes/battery_potato.json",
                "data/hbm/recipes/battery_potatos.json",
                "data/hbm/recipes/battery_pack_redstone.json",
                "data/hbm/recipes/battery_pack_capacitor_copper.json",
                "assets/hbm/models/item/battery_pack.json",
                "data/hbm/recipes/anvil_iron.json",
                "data/hbm/recipes/anvil_lead.json",
                "data/hbm/recipes/stamp_stone_flat.json",
                "data/hbm/recipes/stamp_iron_flat.json",
                "data/hbm/recipes/blades_steel.json",
                "data/hbm/recipes/blades_titanium.json",
                "data/hbm/machine_recipes/anvil.json",
                "data/hbm/machine_recipes/press.json",
                "data/hbm/machine_recipes/shredder.json",
                "data/hbm/machine_recipes/di_furnace.json",
                "data/hbm/machine_recipes/blast_furnace.json",
                "data/hbm/machine_recipes/assembly_machine.json",
                "data/hbm/machine_recipes/chemical_plant.json",
                "assets/hbm/models/obj/assembly_machine_base.obj",
                "assets/hbm/models/obj/assembly_machine_base.mtl",
                "assets/hbm/models/obj/assembly_machine_item.obj",
                "assets/hbm/models/obj/assembly_machine_item.mtl",
                "assets/hbm/models/block/assembly_machine_base.json",
                "assets/hbm/models/block/assembly_machine_dummy.json",
                "assets/hbm/models/block/machine_assembly_machine.json",
                "assets/hbm/blockstates/machine_assembly_machine.json",
                "assets/hbm/models/item/machine_assembly_machine.json",
                "assets/hbm/textures/gui/processing/gui_assembler.png",
                "assets/hbm/textures/gui/processing/gui_recipe_selector.png",
                "data/hbm/loot_tables/blocks/machine_assembly_machine.json",
                "assets/hbm/models/obj/chemical_plant_base.obj",
                "assets/hbm/models/obj/chemical_plant_base.mtl",
                "assets/hbm/models/obj/chemical_plant_item.obj",
                "assets/hbm/models/obj/chemical_plant_item.mtl",
                "assets/hbm/models/obj/chemical_plant_fluid.mtl",
                "assets/hbm/models/block/chemical_plant_base.json",
                "assets/hbm/models/block/chemical_plant_dummy.json",
                "assets/hbm/models/block/machine_chemical_plant.json",
                "assets/hbm/blockstates/machine_chemical_plant.json",
                "assets/hbm/models/item/machine_chemical_plant.json",
                "assets/hbm/textures/gui/processing/gui_chemplant.png",
                "data/hbm/loot_tables/blocks/machine_chemical_plant.json",
                "assets/hbm/models/obj/firebox_main.obj",
                "assets/hbm/models/obj/firebox_main.mtl",
                "assets/hbm/models/obj/firebox_door.obj",
                "assets/hbm/models/obj/firebox_inner_burning.obj",
                "assets/hbm/models/obj/firebox_inner_empty.obj",
                "assets/hbm/models/obj/firebox_item.obj",
                "assets/hbm/models/obj/firebox_item.mtl",
                "assets/hbm/models/obj/boiler.obj",
                "assets/hbm/models/obj/boiler.mtl",
                "assets/hbm/models/block/firebox_main.json",
                "assets/hbm/models/block/firebox_dummy.json",
                "assets/hbm/models/block/heater_firebox.json",
                "assets/hbm/models/block/boiler.json",
                "assets/hbm/models/block/boiler_dummy.json",
                "assets/hbm/models/block/machine_boiler.json",
                "assets/hbm/models/block/machine_turbine.json",
                "assets/hbm/models/block/machine_condenser.json",
                "assets/hbm/blockstates/heater_firebox.json",
                "assets/hbm/blockstates/machine_boiler.json",
                "assets/hbm/blockstates/machine_turbine.json",
                "assets/hbm/blockstates/machine_condenser.json",
                "assets/hbm/models/item/heater_firebox.json",
                "assets/hbm/models/item/machine_boiler.json",
                "assets/hbm/models/item/machine_turbine.json",
                "assets/hbm/models/item/machine_condenser.json",
                "assets/hbm/textures/gui/machine/gui_firebox.png",
                "assets/hbm/textures/gui/gui_turbine.png",
                "data/hbm/loot_tables/blocks/heater_firebox.json",
                "data/hbm/loot_tables/blocks/machine_boiler.json",
                "data/hbm/loot_tables/blocks/machine_turbine.json",
                "data/hbm/loot_tables/blocks/machine_condenser.json",
                "data/hbm/recipes/machine_condenser.json",
                "assets/hbm/models/item/spentsteam_bucket.json",
                "assets/hbm/models/obj/dieselgen_generator.obj",
                "assets/hbm/models/obj/dieselgen_engine.obj",
                "assets/hbm/models/obj/dieselgen_item.obj",
                "assets/hbm/models/block/dieselgen_generator.json",
                "assets/hbm/models/block/dieselgen_engine.json",
                "assets/hbm/textures/models/machines/dieselgen.png",
                "data/hbm/recipes/red_cable.json",
                "data/hbm/recipes/red_wire_coated.json",
                "data/hbm/recipes/machine_siren.json",
                "data/hbm/recipes/crate_iron.json",
                "data/hbm/recipes/crate_steel.json",
                "data/hbm/recipes/electric_furnace.json",
                "data/hbm/recipes/barrel_steel.json",
                "data/hbm/recipes/fluid_duct_paintable.json",
                "assets/hbm/models/obj/wood_burner.obj",
                "assets/hbm/models/obj/wood_burner.mtl",
                "assets/hbm/models/block/wood_burner.json",
                "assets/hbm/models/block/wood_burner_dummy.json",
                "assets/hbm/models/block/machine_wood_burner.json",
                "assets/hbm/blockstates/machine_wood_burner.json",
                "assets/hbm/models/item/machine_wood_burner.json",
                "assets/hbm/models/item/woodoil_bucket.json",
                "assets/hbm/textures/gui/generators/gui_wood_burner_alt.png",
                "assets/hbm/textures/models/machines/wood_burner.png",
                "data/hbm/loot_tables/blocks/machine_wood_burner.json",
                "data/hbm/recipes/machine_wood_burner.json"
        };
        for (String file : files) {
            Path path = ROOT.resolve(file);
            assertTrue(Files.isRegularFile(path), "missing " + file);
        }
        String pressState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_press.json"));
        assertTrue(pressState.contains("\"meta=15\""));
        String epressState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_epress.json"));
        assertTrue(epressState.contains("\"meta=15\""));
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"container.anvil\": \"Tier %s Anvil\""));
        assertTrue(lang.contains("\"container.press\": \"Burner Press\""));
        assertTrue(lang.contains("\"container.rtg\": \"RT Generator\""));
        assertTrue(lang.contains("\"block.hbm.machine_rtg\": \"RT Generator\""));
        assertTrue(lang.contains("\"item.hbm.pellet_rtg\": \"Plutonium-238 RTG Pellet\""));
        assertTrue(lang.contains("\"item.hbm.pellet_rtg_depleted.lead\": \"Decayed Lead RTG Pellet\""));
        assertTrue(lang.contains("\"desc.item.rtgHeat\": \"Power Level: %s\""));
        String rtgBlock = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_rtg.json"));
        assertTrue(rtgBlock.contains("rtg_gen_item.obj"));
        String tesrGen = Files.readString(ROOT.resolve("assets/hbm/models/obj/rtg_gen.obj"));
        assertTrue(tesrGen.contains("-0.500000"));
        assertTrue(objFacesFitVertices(tesrGen));
        String itemGen = Files.readString(ROOT.resolve("assets/hbm/models/obj/rtg_gen_item.obj"));
        assertTrue(itemGen.contains("v 0.000000 0.000000 1.000000"));
        assertTrue(objFacesFitVertices(itemGen));
        String tesrConnector = Files.readString(ROOT.resolve("assets/hbm/models/obj/rtg_connector.obj"));
        assertTrue(tesrConnector.contains("-0.500000"));
        assertTrue(objFacesFitVertices(tesrConnector));
        String genMtl = Files.readString(ROOT.resolve("assets/hbm/models/obj/rtg_gen.mtl"));
        assertTrue(genMtl.contains("hbm:models/machines/rtg"));
        String decoMtl = Files.readString(ROOT.resolve("assets/hbm/models/obj/rtg.mtl"));
        assertTrue(decoMtl.contains("hbm:block/rtg"));
        String pickaxe = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));
        assertTrue(pickaxe.contains("\"hbm:machine_rtg\""));
        String ironTool = Files.readString(ROOT.resolve("data/minecraft/tags/blocks/needs_iron_tool.json"));
        assertTrue(ironTool.contains("\"hbm:machine_rtg\""));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/machine_rtg.json")));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/pellet_rtg_depleted_nickel.json")));
        assertTrue(lang.contains("\"container.epress\": \"Electric Press\""));
        assertTrue(lang.contains("\"block.hbm.machine_press\": \"Burner Press\""));
        assertTrue(lang.contains("\"block.hbm.machine_assembly_machine\": \"Assembly Machine\""));
        assertTrue(lang.contains("\"container.machineAssemblyMachine\": \"Assembly Machine\""));
        assertTrue(lang.contains("\"gui.recipe.setRecipe\": \"Click to set recipe\""));
        String assemblerState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_assembly_machine.json"));
        assertTrue(assemblerState.contains("\"meta=15\""));
        String assemblerItem = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_assembly_machine.json"));
        assertTrue(assemblerItem.contains("assembly_machine_item.obj"));
        assertTrue(assemblerItem.contains("\"#material\": \"hbm:models/machines/assembly_machine\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/press_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/assembly_machine_dummy.json"))
                .contains("minecraft:builtin/entity"));
        byte[] itemObjBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/assembly_machine_item.obj"));
        byte[] baseObjBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/assembly_machine_base.obj"));
        byte[] baseJsonBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/block/assembly_machine_base.json"));
        assertTrue(itemObjBytes[0] != (byte) 0xEF, "assembly_machine_item.obj must not have a UTF-8 BOM");
        assertTrue(baseObjBytes[0] != (byte) 0xEF, "assembly_machine_base.obj must not have a UTF-8 BOM");
        assertTrue(baseJsonBytes[0] != (byte) 0xEF, "assembly_machine_base.json must not have a UTF-8 BOM");
        String tesrBase = Files.readString(ROOT.resolve("assets/hbm/models/obj/assembly_machine_base.obj"));
        String itemObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/assembly_machine_item.obj"));
        assertTrue(tesrBase.startsWith("mtllib "));
        assertTrue(itemObj.startsWith("mtllib "));
        assertTrue(itemObj.contains("v -1.437500 1.250000 0.093750"));
        assertTrue(tesrBase.contains("v 1.500000 0.000000 -1.250000"));
        assertTrue(objFacesFitVertices(tesrBase));
        assertTrue(objFacesFitVertices(itemObj));
        assertTrue(pickaxe.contains("\"hbm:machine_assembly_machine\""));
        assertTrue(ironTool.contains("\"hbm:machine_assembly_machine\""));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/machine_assembly_machine.json")));
        assertTrue(lang.contains("\"block.hbm.machine_chemical_plant\": \"Chemical Plant\""));
        assertTrue(lang.contains("\"container.machineChemicalPlant\": \"Chemical Plant\""));
        String chemState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_chemical_plant.json"));
        assertTrue(chemState.contains("\"meta=15\""));
        String chemItem = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_chemical_plant.json"));
        assertTrue(chemItem.contains("chemical_plant_item.obj"));
        assertTrue(chemItem.contains("\"#material\": \"hbm:models/machines/chemical_plant\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/chemical_plant_dummy.json"))
                .contains("minecraft:builtin/entity"));
        byte[] chemItemBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/chemical_plant_item.obj"));
        byte[] chemBaseBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/chemical_plant_base.obj"));
        byte[] chemBaseJsonBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/block/chemical_plant_base.json"));
        assertTrue(chemItemBytes[0] != (byte) 0xEF, "chemical_plant_item.obj must not have a UTF-8 BOM");
        assertTrue(chemBaseBytes[0] != (byte) 0xEF, "chemical_plant_base.obj must not have a UTF-8 BOM");
        assertTrue(chemBaseJsonBytes[0] != (byte) 0xEF, "chemical_plant_base.json must not have a UTF-8 BOM");
        String chemTesrBase = Files.readString(ROOT.resolve("assets/hbm/models/obj/chemical_plant_base.obj"));
        String chemItemObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/chemical_plant_item.obj"));
        String chemFluidMtl = Files.readString(ROOT.resolve("assets/hbm/models/obj/chemical_plant_fluid.mtl"));
        assertTrue(chemTesrBase.startsWith("mtllib "));
        assertTrue(chemItemObj.startsWith("mtllib "));
        assertTrue(chemTesrBase.contains("v 1.500000 0.125000 1.250000"));
        assertTrue(chemItemObj.contains("v 0.500000 0.875000 0.250000"));
        assertTrue(chemFluidMtl.contains("hbm:models/machines/chemical_plant_fluid"));
        assertTrue(objFacesFitVertices(chemTesrBase));
        assertTrue(objFacesFitVertices(chemItemObj));
        assertTrue(pickaxe.contains("\"hbm:machine_chemical_plant\""));
        assertTrue(ironTool.contains("\"hbm:machine_chemical_plant\""));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/machine_chemical_plant.json")));
        assertTrue(lang.contains("\"block.hbm.machine_epress\": \"Electric Press\""));
        assertTrue(lang.contains("\"item.hbm.battery_pack.battery_redstone\": \"Redstone Battery\""));
        assertTrue(lang.contains("\"item.hbm.battery_potato\": \"Potato Battery\""));
        assertTrue(lang.contains("\"item.hbm.battery_potatos\": \"PotatOS\""));
        assertTrue(lang.contains("\"battery.priority.low\": \"Charge Priority: Low\""));
        assertTrue(lang.contains("\"block.hbm.heater_firebox\": \"Firebox\""));
        assertTrue(lang.contains("\"block.hbm.machine_boiler\": \"Boiler\""));
        assertTrue(lang.contains("\"block.hbm.machine_turbine\": \"Steam Turbine\""));
        assertTrue(lang.contains("\"block.hbm.machine_condenser\": \"Steam Condenser\""));
        assertTrue(lang.contains("\"container.heaterFirebox\": \"Firebox\""));
        assertTrue(lang.contains("\"container.machineTurbine\": \"Steam Turbine\""));
        assertTrue(lang.contains("\"container.machineWoodBurner\": \"Wood-Burner\""));
        assertTrue(lang.contains("\"block.hbm.machine_wood_burner\": \"Wood-Burning Generator\""));
        assertTrue(lang.contains("\"fluid.hbm.woodoil\": \"Wood Oil\""));
        assertTrue(lang.contains("\"item.hbm.woodoil_bucket\": \"Wood Oil Bucket\""));
        assertTrue(lang.contains("\"fluid.hbm.spentsteam\": \"Spent Steam\""));
        assertTrue(lang.contains("\"item.hbm.spentsteam_bucket\": \"Spent Steam Bucket\""));
        String fireboxState = Files.readString(ROOT.resolve("assets/hbm/blockstates/heater_firebox.json"));
        assertTrue(fireboxState.contains("\"meta=15\""));
        String boilerState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_boiler.json"));
        assertTrue(boilerState.contains("\"meta=15\""));
        String fireboxItem = Files.readString(ROOT.resolve("assets/hbm/models/block/heater_firebox.json"));
        assertTrue(fireboxItem.contains("firebox_item.obj"));
        assertTrue(fireboxItem.contains("\"#material\": \"hbm:models/machines/firebox\""));
        String boilerItem = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_boiler.json"));
        assertTrue(boilerItem.contains("boiler.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/firebox_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/boiler_dummy.json"))
                .contains("minecraft:builtin/entity"));
        byte[] fireboxItemBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/firebox_item.obj"));
        byte[] fireboxMainBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/firebox_main.obj"));
        byte[] boilerObjBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/boiler.obj"));
        assertTrue(fireboxItemBytes[0] != (byte) 0xEF, "firebox_item.obj must not have a UTF-8 BOM");
        assertTrue(fireboxMainBytes[0] != (byte) 0xEF, "firebox_main.obj must not have a UTF-8 BOM");
        assertTrue(boilerObjBytes[0] != (byte) 0xEF, "boiler.obj must not have a UTF-8 BOM");
        String fireboxMain = Files.readString(ROOT.resolve("assets/hbm/models/obj/firebox_main.obj"));
        String fireboxItemObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/firebox_item.obj"));
        String boilerObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/boiler.obj"));
        assertTrue(fireboxMain.startsWith("mtllib "));
        assertTrue(fireboxItemObj.startsWith("mtllib "));
        assertTrue(boilerObj.startsWith("mtllib "));
        assertTrue(fireboxMain.contains("v -1.500000 0.000000 1.500000"));
        assertTrue(fireboxItemObj.contains("v 1.375000 0.750000 0.125000"));
        assertTrue(boilerObj.contains("v -1.500000 0.000000 1.500000"));
        assertTrue(objFacesFitVertices(fireboxMain));
        assertTrue(objFacesFitVertices(fireboxItemObj));
        assertTrue(objFacesFitVertices(boilerObj));
        assertTrue(pickaxe.contains("\"hbm:heater_firebox\""));
        assertTrue(pickaxe.contains("\"hbm:machine_boiler\""));
        assertTrue(pickaxe.contains("\"hbm:machine_turbine\""));
        assertTrue(pickaxe.contains("\"hbm:machine_condenser\""));
        assertTrue(pickaxe.contains("\"hbm:machine_wood_burner\""));
        assertTrue(ironTool.contains("\"hbm:heater_firebox\""));
        assertTrue(ironTool.contains("\"hbm:machine_boiler\""));
        assertTrue(ironTool.contains("\"hbm:machine_turbine\""));
        assertTrue(ironTool.contains("\"hbm:machine_condenser\""));
        assertTrue(ironTool.contains("\"hbm:machine_wood_burner\""));
        String condenserRecipe = Files.readString(ROOT.resolve("data/hbm/recipes/machine_condenser.json"));
        assertTrue(condenserRecipe.contains("hbm:plate_cast"));
        assertTrue(condenserRecipe.contains("hbm:machine_condenser"));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/heater_firebox.json")));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/machine_boiler.json")));
        assertTrue(Files.notExists(ROOT.resolve("data/hbm/recipes/machine_turbine.json")));
        String dieselItem = Files.readString(ROOT.resolve("assets/hbm/models/block/diesel_generator.json"));
        assertTrue(dieselItem.contains("dieselgen_item.obj"));
        assertTrue(dieselItem.contains("hbm:models/machines/dieselgen"));
        String woodRecipe = Files.readString(ROOT.resolve("data/hbm/recipes/machine_wood_burner.json"));
        assertTrue(woodRecipe.contains("hbm:plate_steel"));
        assertTrue(woodRecipe.contains("minecraft:furnace"));
        assertTrue(woodRecipe.contains("hbm:machine_wood_burner"));
        byte[] woodObjBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/wood_burner.obj"));
        assertTrue(woodObjBytes[0] != (byte) 0xEF, "wood_burner.obj must not have a UTF-8 BOM");
        String woodObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/wood_burner.obj"));
        assertTrue(woodObj.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(woodObj));
        String woodState = Files.readString(ROOT.resolve("assets/hbm/blockstates/machine_wood_burner.json"));
        assertTrue(woodState.contains("\"meta=15\""));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/wood_burner_dummy.json"))
                .contains("minecraft:builtin/entity"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/block/machine_wood_burner.json"))
                .contains("wood_burner.obj"));
        assertTrue(Files.readString(ROOT.resolve("assets/hbm/models/item/machine_press.json")).contains("press_body"));
        byte[] dieselItemBytes = Files.readAllBytes(ROOT.resolve("assets/hbm/models/obj/dieselgen_item.obj"));
        assertTrue(dieselItemBytes[0] != (byte) 0xEF, "dieselgen_item.obj must not have a UTF-8 BOM");
        String dieselGen = Files.readString(ROOT.resolve("assets/hbm/models/obj/dieselgen_generator.obj"));
        String dieselEngine = Files.readString(ROOT.resolve("assets/hbm/models/obj/dieselgen_engine.obj"));
        assertTrue(dieselGen.startsWith("mtllib "));
        assertTrue(dieselEngine.startsWith("mtllib "));
        assertTrue(objFacesFitVertices(dieselGen));
        assertTrue(objFacesFitVertices(dieselEngine));
        assertTrue(objFacesFitVertices(Files.readString(ROOT.resolve("assets/hbm/models/obj/dieselgen_item.obj"))));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/red_cable.json")).contains("hbm:wire_red_copper"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/electric_furnace.json")).contains("hbm:ingot_beryllium"));
        assertTrue(Files.readString(ROOT.resolve("data/hbm/recipes/fluid_duct_paintable.json")).contains("hbm:plate_aluminium"));
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
