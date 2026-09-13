package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OreSurvivalPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void overworldAndNetherOreWorldgenMatchOneSevenDefaults() throws IOException {
        String overworld = Files.readString(ROOT.resolve("data/hbm/forge/biome_modifier/add_overworld_ores.json"));
        assertTrue(overworld.contains("\"biomes\": \"#minecraft:is_overworld\"")
                || overworld.contains("\"biomes\":\"#minecraft:is_overworld\""));
        assertTrue(overworld.contains("underground_ores"));
        for (String feature : new String[]{
                "hbm:schist_stratum", "hbm:ore_gneiss_iron", "hbm:ore_gneiss_gold",
                "hbm:ore_gneiss_uranium", "hbm:ore_gneiss_copper", "hbm:ore_gneiss_asbestos",
                "hbm:ore_gneiss_lithium", "hbm:ore_gneiss_rare", "hbm:ore_gneiss_gas",
                "hbm:ore_uranium", "hbm:ore_thorium", "hbm:ore_titanium", "hbm:ore_sulfur",
                "hbm:ore_aluminium", "hbm:ore_copper", "hbm:ore_fluorite", "hbm:ore_niter",
                "hbm:ore_tungsten", "hbm:ore_lead", "hbm:ore_beryllium", "hbm:ore_rare",
                "hbm:ore_lignite", "hbm:ore_asbestos", "hbm:ore_cinnebar", "hbm:ore_cobalt",
                "hbm:cluster_iron", "hbm:cluster_titanium", "hbm:cluster_aluminium",
                "hbm:cluster_copper", "hbm:ore_alexandrite", "hbm:ore_limestone",
                "hbm:ore_layer_hematite", "hbm:ore_layer_bauxite", "hbm:ore_layer_malachite",
                "hbm:gas_flammable", "hbm:depth_iron", "hbm:depth_titanium",
                "hbm:depth_tungsten", "hbm:depth_cinnebar", "hbm:depth_zirconium",
                "hbm:depth_borax", "hbm:ore_stone_keyhole"
        }) {
            assertTrue(overworld.contains(feature), "missing overworld feature " + feature);
        }
        assertTrue(overworld.indexOf("hbm:schist_stratum") < overworld.indexOf("hbm:ore_gneiss_iron"));

        String nether = Files.readString(ROOT.resolve("data/hbm/forge/biome_modifier/add_nether_ores.json"));
        assertTrue(nether.contains("#minecraft:is_nether"));
        assertTrue(nether.contains("hbm:ore_nether_uranium"));
        assertTrue(nether.contains("hbm:ore_nether_tungsten"));
        assertTrue(nether.contains("hbm:ore_nether_sulfur"));
        assertTrue(nether.contains("hbm:ore_nether_fire"));
        assertTrue(nether.contains("hbm:ore_nether_coal"));
        assertTrue(nether.contains("hbm:ore_nether_cobalt"));
        assertTrue(nether.contains("hbm:nether_smoldering"));
        assertTrue(nether.contains("hbm:depth_nether_neodymium_floor"));
        assertTrue(nether.contains("hbm:depth_nether_neodymium_ceiling"));
        assertFalse(nether.contains("hbm:ore_nether_plutonium"));

        String end = Files.readString(ROOT.resolve("data/hbm/forge/biome_modifier/add_end_ores.json"));
        assertTrue(end.contains("#minecraft:is_end"));
        assertTrue(end.contains("hbm:ore_tikite"));

        String uranium = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_uranium.json"));
        assertTrue(uranium.contains("\"count\": 7") || uranium.contains("\"count\":7"));
        String configured = Files.readString(ROOT.resolve("data/hbm/worldgen/configured_feature/ore_uranium.json"));
        assertTrue(configured.contains("minecraft:stone_ore_replaceables"));
        assertTrue(configured.contains("minecraft:deepslate_ore_replaceables"));
        assertTrue(configured.contains("hbm:ore_uranium"));

        String alex = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_alexandrite.json"));
        assertTrue(alex.contains("\"chance\": 100") || alex.contains("\"chance\":100"));
        assertTrue(alex.contains("minecraft:rarity_filter"));
    }

    @Test
    void overworldOreHeightsReachDeepslate() throws IOException {
        String uranium = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_uranium.json"));
        assertTrue(uranium.contains("\"absolute\": -59") || uranium.contains("\"absolute\":-59"));
        assertTrue(uranium.contains("\"absolute\": 24") || uranium.contains("\"absolute\":24"));

        String lignite = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_lignite.json"));
        assertTrue(lignite.contains("\"absolute\": 35") || lignite.contains("\"absolute\":35"));

        String limestone = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_limestone.json"));
        assertTrue(limestone.contains("\"absolute\": -39") || limestone.contains("\"absolute\":-39"));
        assertTrue(limestone.contains("hbm:ore_limestone"));

        String hematite = Files.readString(ROOT.resolve("data/hbm/worldgen/configured_feature/ore_layer_hematite.json"));
        assertTrue(hematite.contains("hbm:ore_layer_hematite"));

        String ligniteCfg = Files.readString(ROOT.resolve("data/hbm/worldgen/configured_feature/ore_lignite.json"));
        assertFalse(ligniteCfg.contains("deepslate_ore_replaceables"));

        String cobaltN = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_nether_cobalt.json"));
        assertTrue(cobaltN.contains("\"absolute\": 100") || cobaltN.contains("\"absolute\":100"));
        assertTrue(cobaltN.contains("\"absolute\": 125") || cobaltN.contains("\"absolute\":125"));

        String asbestos = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_asbestos.json"));
        assertTrue(asbestos.contains("\"count\": 2") || asbestos.contains("\"count\":2"));

        String gneissU = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_gneiss_uranium.json"));
        assertTrue(gneissU.contains("\"count\": 21") || gneissU.contains("\"count\":21"));
        assertTrue(gneissU.contains("\"absolute\": 30") || gneissU.contains("\"absolute\":30"));
        String gneissCfg = Files.readString(ROOT.resolve("data/hbm/worldgen/configured_feature/ore_gneiss_uranium.json"));
        assertTrue(gneissCfg.contains("hbm:stone_gneiss"));
        assertFalse(gneissCfg.contains("deepslate_ore_replaceables"));

        String tikite = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/ore_tikite.json"));
        assertTrue(tikite.contains("\"count\": 8") || tikite.contains("\"count\":8"));
        String tikiteCfg = Files.readString(ROOT.resolve("data/hbm/worldgen/configured_feature/ore_tikite.json"));
        assertTrue(tikiteCfg.contains("minecraft:end_stone"));

        String fox = Files.readString(ROOT.resolve("data/hbm/forge/biome_modifier/add_flower_foxglove.json"));
        assertTrue(fox.contains("#hbm:ntm_flower_forest"));
        assertTrue(fox.contains("vegetal_decoration"));
        assertFalse(fox.contains("#minecraft:is_forest"));
        String night = Files.readString(ROOT.resolve("data/hbm/forge/biome_modifier/add_flower_nightshade.json"));
        assertTrue(night.contains("minecraft:dark_forest"));
        String tobacco = Files.readString(ROOT.resolve("data/hbm/forge/biome_modifier/add_flower_tobacco.json"));
        assertTrue(tobacco.contains("#minecraft:is_jungle"));
        String forestTag = Files.readString(ROOT.resolve("data/hbm/tags/worldgen/biome/ntm_flower_forest.json"));
        assertTrue(forestTag.contains("minecraft:forest"));
        assertTrue(forestTag.contains("minecraft:dark_forest"));
        assertFalse(forestTag.contains("minecraft:grove"));
        for (String flower : new String[]{"foxglove", "nightshade", "tobacco", "weed"}) {
            String placed = Files.readString(ROOT.resolve("data/hbm/worldgen/placed_feature/flower_" + flower + ".json"));
            assertTrue(placed.contains("MOTION_BLOCKING_NO_LEAVES"), flower + " must sit on ground, not canopy");
            String cfg = Files.readString(ROOT.resolve("data/hbm/worldgen/configured_feature/flower_" + flower + ".json"));
            assertTrue(cfg.contains("would_survive"));
            assertTrue(cfg.contains("\"variant\": \"" + flower + "\""));
        }
        String flowerBlock = Files.readString(Path.of("src/main/java/com/hbm/blocks/generic/PlantFlowerBlock.java"));
        assertTrue(flowerBlock.contains("BlockTags.DIRT"));
        assertTrue(flowerBlock.contains("DIRT_DEAD"));
        assertTrue(flowerBlock.contains("FARMLAND"));
        assertFalse(flowerBlock.contains("return true;"));
    }

    @Test
    void resourceStoneAssetsAndLimestoneFireclayExist() throws IOException {
        for (String id : new String[]{
                "stone_resource_hematite", "stone_resource_malachite",
                "stone_resource_bauxite", "stone_resource_limestone"
        }) {
            assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/blockstates/" + id + ".json")), id);
            assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/models/block/" + id + ".json")), id);
            assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/loot_tables/blocks/" + id + ".json")), id);
        }
        String malachiteLoot = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/stone_resource_malachite.json"));
        assertTrue(malachiteLoot.contains("hbm:chunk_ore_malachite"));
        String fireclay = Files.readString(ROOT.resolve("data/hbm/recipes/ball_fireclay_from_limestone.json"));
        assertTrue(fireclay.contains("hbm:stone_resource_limestone"));
        assertTrue(fireclay.contains("hbm:ball_fireclay"));
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.stone_resource_hematite\": \"Hematite\""));
        assertTrue(lang.contains("\"item.hbm.chunk_ore_malachite\": \"Malachite Chunk\""));
        assertTrue(lang.contains("\"item.hbm.chunk_ore_cryolite\": \"Cryolite Chunk\""));
        assertTrue(lang.contains("\"item.hbm.chunk_ore_rare\": \"Rare Earth Ore Chunk\""));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/models/item/chunk_ore_cryolite.json")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/models/item/chunk_ore_rare.json")));
        String dirt = Files.readString(ROOT.resolve("assets/hbm/blockstates/ntm_dirt.json"));
        assertTrue(dirt.contains("hbm:block/ntm_dirt"));
        assertFalse(dirt.contains("\"model\": \"hbm:block/\""), "ntm_dirt blockstate must not be truncated");
    }

    @Test
    void clusterOresDropCrystalsAndCrystalsSmelt() throws IOException {
        String iron = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/cluster_iron.json"));
        assertTrue(iron.contains("hbm:crystal_iron"));
        String copper = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/cluster_copper.json"));
        assertTrue(copper.contains("hbm:crystal_copper"));
        String smelt = Files.readString(ROOT.resolve("data/hbm/recipes/smelt_crystal_iron.json"));
        assertTrue(smelt.contains("hbm:smelting"));
        assertTrue(smelt.contains("hbm:crystal_iron"));
        assertTrue(smelt.contains("minecraft:iron_ingot"));
        assertTrue(smelt.contains("\"count\": 2") || smelt.contains("\"count\":2"));
    }

    @Test
    void oreLootDropsItemsNotBlocks() throws IOException {
        String sulfur = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_sulfur.json"));
        assertTrue(sulfur.contains("\"name\": \"hbm:sulfur\"") || sulfur.contains("\"name\":\"hbm:sulfur\""));
        assertTrue(sulfur.contains("minecraft:silk_touch"));

        String niter = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_niter.json"));
        assertTrue(niter.contains("\"name\": \"hbm:niter\"") || niter.contains("\"name\":\"hbm:niter\""));
        assertFalse(niter.contains("\"name\": \"hbm:ore_niter\"") && !niter.contains("silk_touch"));

        String netherSulfur = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_nether_sulfur.json"));
        assertTrue(netherSulfur.contains("hbm:sulfur"));
        String fire = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_nether_fire.json"));
        assertTrue(fire.contains("hbm:ingot_phosphorus"));
        assertTrue(fire.contains("hbm:powder_fire"));
        assertTrue(fire.contains("random_chance"));
        assertTrue(fire.contains("0.1"));
        String cobalt = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_cobalt.json"));
        assertTrue(cobalt.contains("hbm:fragment_cobalt"));
        String coltan = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_coltan.json"));
        assertTrue(coltan.contains("hbm:fragment_coltan"));
        assertTrue(coltan.contains("minecraft:silk_touch"));
        assertFalse(coltan.contains("\"name\": \"hbm:ore_coltan\"") && !coltan.contains("silk_touch"));

        String rare = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_rare.json"));
        assertTrue(rare.contains("hbm:chunk_ore_rare"));
        assertTrue(rare.contains("minecraft:silk_touch"));
        String gneissRare = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_gneiss_rare.json"));
        assertTrue(gneissRare.contains("hbm:chunk_ore_rare"));

        String netherCoal = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_nether_coal.json"));
        assertTrue(netherCoal.contains("hbm:coal_infernal"));
        assertTrue(netherCoal.contains("minecraft:silk_touch"));
        String smolder = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_nether_smoldering.json"));
        assertTrue(smolder.contains("hbm:powder_fire"));
        String depthCinnebar = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_depth_cinnebar.json"));
        assertTrue(depthCinnebar.contains("hbm:cinnebar"));
        assertTrue(depthCinnebar.contains("\"min\": 2"));
        assertTrue(depthCinnebar.contains("\"max\": 4"));
        String depthZr = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_depth_zirconium.json"));
        assertTrue(depthZr.contains("hbm:nugget_zirconium"));
        String depthNd = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_depth_nether_neodymium.json"));
        assertTrue(depthNd.contains("hbm:fragment_neodymium"));
        String depthBorax = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_depth_borax.json"));
        assertTrue(depthBorax.contains("hbm:powder_borax"));
        String alex = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_alexandrite.json"));
        assertTrue(alex.contains("hbm:gem_alexandrite"));
        assertTrue(alex.contains("limit_count"));
        String depthIron = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/cluster_depth_iron.json"));
        assertTrue(depthIron.contains("hbm:crystal_iron"));
        assertTrue(depthIron.contains("minecraft:silk_touch"));
        String basaltSulfur = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_basalt_sulfur.json"));
        assertTrue(basaltSulfur.contains("hbm:sulfur"));
        String basaltGem = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_basalt_gem.json"));
        assertTrue(basaltGem.contains("hbm:gem_volcanic"));
        String sellaDiamond = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_sellafield_diamond.json"));
        assertTrue(sellaDiamond.contains("minecraft:diamond"));
        String sellaRad = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/ore_sellafield_radgem.json"));
        assertTrue(sellaRad.contains("hbm:gem_rad"));
        String clusterAl = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/cluster_aluminium.json"));
        assertTrue(clusterAl.contains("hbm:crystal_aluminium"));
        assertTrue(clusterAl.contains("minecraft:silk_touch"));

        String trinitite = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/waste_trinitite.json"));
        assertTrue(trinitite.contains("hbm:trinitite"));
        assertTrue(trinitite.contains("minecraft:silk_touch"));
        String redTrinitite = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/waste_trinitite_red.json"));
        assertTrue(redTrinitite.contains("hbm:trinitite"));
        String planks = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/waste_planks.json"));
        assertTrue(planks.contains("minecraft:charcoal"));
        String frozen = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/frozen_dirt.json"));
        assertTrue(frozen.contains("minecraft:snowball"));
        String cobble = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/block_meteor_cobble.json"));
        assertTrue(cobble.contains("hbm:fragment_meteorite"));
        String broken = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/block_meteor_broken.json"));
        assertTrue(broken.contains("hbm:fragment_meteorite"));
        assertTrue(broken.contains("\"max\": 3"));
        String molten = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/block_meteor_molten.json"));
        assertTrue(molten.contains("minecraft:silk_touch"));
        assertFalse(molten.contains("hbm:fragment_meteorite"));
    }

    @Test
    void furnaceSmeltingCopiesOneSevenUraniumRow() throws IOException {
        String smelt = Files.readString(ROOT.resolve("data/hbm/recipes/smelt_ore_uranium.json"));
        assertTrue(smelt.contains("\"type\": \"minecraft:smelting\"")
                || smelt.contains("\"type\":\"minecraft:smelting\""));
        assertTrue(smelt.contains("\"item\": \"hbm:ore_uranium\"")
                || smelt.contains("\"item\":\"hbm:ore_uranium\""));
        assertTrue(smelt.contains("hbm:ingot_uranium"));
        assertFalse(smelt.contains("\"item\": \"\""));

        String aluminium = Files.readString(ROOT.resolve("data/hbm/recipes/smelt_ore_aluminium.json"));
        assertTrue(aluminium.contains("\"type\": \"minecraft:smelting\"")
                || aluminium.contains("\"type\":\"minecraft:smelting\""));
        assertTrue(aluminium.contains("hbm:ore_aluminium"));
        assertTrue(aluminium.contains("hbm:chunk_ore_cryolite"));
        assertFalse(aluminium.contains("hbm:ingot_aluminium"));

        String battery = Files.readString(ROOT.resolve("data/hbm/recipes/battery_pack_redstone.json"));
        assertTrue(battery.contains("\"P\": { \"item\": \"hbm:plate_polymer\" }"));
        assertTrue(battery.contains("\"key\": {"));
        assertFalse(battery.contains("\"P\": { \"item\": \"hbm:plate_polymer\" }\n  ],"),
                "battery pack key object must close with }");

        String glass = Files.readString(ROOT.resolve("data/hbm/recipes/smelt_waste_trinitite.json"));
        assertTrue(glass.contains("hbm:waste_trinitite"));
        assertTrue(glass.contains("hbm:glass_trinitite"));
        String lodestone = Files.readString(ROOT.resolve("data/hbm/recipes/smelt_lodestone.json"));
        assertTrue(lodestone.contains("hbm:lodestone"));
        assertTrue(lodestone.contains("hbm:crystal_iron"));
        String cobaltTiny = Files.readString(ROOT.resolve("data/hbm/recipes/powder_cobalt_from_tiny.json"));
        assertTrue(cobaltTiny.contains("hbm:powder_cobalt_tiny"));
        assertTrue(cobaltTiny.contains("hbm:powder_cobalt"));
        String lithiumTiny = Files.readString(ROOT.resolve("data/hbm/recipes/powder_lithium_from_tiny.json"));
        assertTrue(lithiumTiny.contains("hbm:powder_lithium_tiny"));
    }

    @Test
    void mineralCompressionCopiesOneSevenNineToOne() throws IOException {
        String steelPack = Files.readString(ROOT.resolve("data/hbm/recipes/block_steel_from_ingot_steel.json"));
        assertTrue(steelPack.contains("hbm:ingot_steel"));
        assertTrue(steelPack.contains("hbm:block_steel"));
        assertTrue(steelPack.contains("###"));
        String steelUnpack = Files.readString(ROOT.resolve("data/hbm/recipes/ingot_steel_from_block_steel.json"));
        assertTrue(steelUnpack.contains("hbm:block_steel"));
        assertTrue(steelUnpack.contains("hbm:ingot_steel"));
        assertTrue(steelUnpack.contains("\"count\": 9") || steelUnpack.contains("\"count\":9"));

        String coltanPack = Files.readString(ROOT.resolve("data/hbm/recipes/block_coltan_from_fragment_coltan.json"));
        assertTrue(coltanPack.contains("hbm:fragment_coltan"));
        assertTrue(coltanPack.contains("hbm:block_coltan"));
        String coltanUnpack = Files.readString(ROOT.resolve("data/hbm/recipes/fragment_coltan_from_block_coltan.json"));
        assertTrue(coltanUnpack.contains("hbm:fragment_coltan"));

        String niter = Files.readString(ROOT.resolve("data/hbm/recipes/block_niter_from_niter.json"));
        assertTrue(niter.contains("hbm:niter"));
        assertTrue(niter.contains("hbm:block_niter"));

        String scrap = Files.readString(ROOT.resolve("data/hbm/recipes/block_scrap_from_scrap.json"));
        assertTrue(scrap.contains("hbm:scrap"));
        assertTrue(scrap.contains("##"));
        String dustScrap = Files.readString(ROOT.resolve("data/hbm/recipes/block_scrap_from_dust.json"));
        assertTrue(dustScrap.contains("hbm:dust"));

        String painted = Files.readString(ROOT.resolve("data/hbm/recipes/nuclear_waste_from_block_waste_painted.json"));
        assertTrue(painted.contains("hbm:block_waste_painted"));
        assertTrue(painted.contains("hbm:nuclear_waste"));

        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/ingot_silicon_from_nugget_silicon.json")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/recipes/ingot_silicon_from_nuggets.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/powder_cobalt_from_powder_cobalt_tiny.json")));
        assertTrue(Files.isRegularFile(ROOT.resolve("data/hbm/recipes/powder_cobalt_from_tiny.json")));

        String anvil = Files.readString(ROOT.resolve("data/hbm/machine_recipes/anvil.json"));
        assertTrue(anvil.contains("\"item\": \"hbm:deco_steel\", \"count\": 4"));
        assertTrue(anvil.contains("\"item\": \"hbm:ingot_steel\", \"count\": 1"));
        assertTrue(anvil.contains("\"item\": \"hbm:deco_rusty_steel\""));
        int firebox = anvil.indexOf("\"item\": \"hbm:heater_firebox\"");
        int recycleFirebox = anvil.indexOf("\"item\": \"hbm:heater_firebox\"", firebox + 1);
        assertTrue(recycleFirebox > 0, "firebox construction and recycling must both exist");
        String recycleSlice = anvil.substring(recycleFirebox, Math.min(anvil.length(), recycleFirebox + 280));
        assertTrue(recycleSlice.contains("hbm:plate_steel"));
        assertTrue(recycleSlice.contains("hbm:ingot_copper"));
        assertTrue(recycleSlice.contains("\"count\": 8") || recycleSlice.contains("\"count\":8"));

        String earth = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/waste_earth.json"));
        assertTrue(earth.contains("minecraft:dirt"));
        assertTrue(earth.contains("minecraft:silk_touch"));
        String mycelium = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/waste_mycelium.json"));
        assertTrue(mycelium.contains("minecraft:dirt"));
        String nuka = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/block_cap_nuka.json"));
        assertTrue(nuka.contains("hbm:cap_nuka"));
        assertTrue(nuka.contains("128"));
        assertTrue(nuka.contains("minecraft:silk_touch"));
        String star = Files.readString(ROOT.resolve("data/hbm/loot_tables/blocks/block_cap_star.json"));
        assertTrue(star.contains("hbm:block_cap_star"));
        assertFalse(star.contains("128"));
    }

    @Test
    void niterItemAndOptionalJeiPluginExist() throws IOException {
        String niterModel = Files.readString(ROOT.resolve("assets/hbm/models/item/niter.json"));
        assertTrue(niterModel.contains("hbm:item/salpeter"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/item/salpeter.png")));

        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"item.hbm.niter\": \"Niter\""));
        assertTrue(lang.contains("\"jei.hbm.category.press\""));
        assertTrue(lang.contains("\"jei.hbm.category.silex\""));
        assertTrue(lang.contains("\"jei.hbm.sulfur_every\""));

        String mods = Files.readString(ROOT.resolve("META-INF/mods.toml"));
        assertTrue(mods.contains("modId=\"jei\""));
        assertTrue(mods.contains("mandatory=false"));

        assertTrue(Files.isRegularFile(Path.of("src/main/java/com/hbm/compat/jei/HbmJeiPlugin.java")));
        String plugin = Files.readString(Path.of("src/main/java/com/hbm/compat/jei/HbmJeiPlugin.java"));
        assertTrue(plugin.contains("@JeiPlugin"));
        assertTrue(plugin.contains("HbmJeiRecipes.SILEX"));
        assertTrue(plugin.contains("HbmJeiRecipes.ANVIL"));

        String skip = Files.readString(Path.of("src/main/java/com/hbm/registry/ModBulkContent.java"));
        assertTrue(skip.contains("\"niter\""));
        assertTrue(skip.contains("\"salpeter\""));
        String working = Files.readString(Path.of("src/main/java/com/hbm/port/PortContentRegistry.java"));
        assertTrue(working.contains("\"niter\""));
        assertTrue(working.contains("\"chunk_ore_cryolite\""));
        assertTrue(working.contains("\"fragment_coltan\""));
        assertTrue(working.contains("\"coal_infernal\""));
        assertTrue(working.contains("\"gem_alexandrite\""));
        assertTrue(working.contains("\"trinitite\""));
        assertTrue(working.contains("\"fragment_meteorite\""));
    }
}
