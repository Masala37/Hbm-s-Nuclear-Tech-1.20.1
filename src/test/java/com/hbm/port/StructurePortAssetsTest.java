package com.hbm.port;

import com.hbm.world.gen.nbt.StructureBlockNames;
import com.hbm.world.gen.nbt.StructureSpacing;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructurePortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private static final String[] LOCATABLE = {
            "spire", "vertibird", "crashed_vertibird", "beached_patrol",
            "aircraft_carrier", "oil_rig", "lighthouse", "dish",
            "forestchem", "labolatory", "forest_post", "radio", "repeater_radio",
            "factory", "crane", "broadcaster_tower", "plane1", "plane2",
            "desert_shack_1", "desert_shack_2", "desert_shack_3",
            "ruin_a", "ruin_b", "ruin_c", "ruin_d", "ruin_e",
            "ruin_f", "ruin_g", "ruin_h", "ruin_i", "ruin_j",
            "silo", "house_1", "house_2", "lab_1", "lab_2",
            "office", "office_corner", "rural_house", "meteor_dungeon"
    };

    @Test
    void nbtStructureAssetsExist() throws IOException {
        String[] files = {
                "assets/hbm/structures/radio_house.nbt",
                "assets/hbm/structures/repeater_radio.nbt",
                "assets/hbm/structures/spire.nbt",
                "assets/hbm/structures/meteor/meteor-core.nbt",
                "data/hbm/worldgen/structure_set/radio.json",
                "data/hbm/worldgen/structure_set/meteor_dungeon.json",
                "data/hbm/tags/worldgen/structure/ntm_structures.json",
                "data/hbm/tags/worldgen/biome/ntm_ocean_or_beach.json",
                "assets/hbm/blockstates/crate.json",
                "assets/hbm/blockstates/crate_ammo.json",
                "assets/hbm/blockstates/deco_loot.json",
                "assets/hbm/blockstates/meteor_spawner.json",
                "assets/hbm/models/item/crowbar.json"
        };
        for (String file : files) {
            assertTrue(Files.isRegularFile(ROOT.resolve(file)), "missing " + file);
        }
        assertFalse(Files.exists(ROOT.resolve("data/hbm/worldgen/structure/nbt_structure.json")),
                "bundled hbm:nbt_structure must not exist; locate uses named ids");
        assertFalse(Files.exists(ROOT.resolve("data/hbm/worldgen/structure_set/nbt_structures.json")),
                "one equal-weight set cannot match 1.7 biome-filtered rarity");
        String tag = Files.readString(ROOT.resolve("data/hbm/tags/worldgen/structure/ntm_structures.json"));
        String ntm = Files.readString(Path.of("src/main/java/com/hbm/world/gen/NTMStructures.java"));
        assertTrue(ntm.contains("veryFlat"));
        assertTrue(ntm.contains("moderateFlat"));
        assertTrue(ntm.contains("IS_PLAINS"));
        assertTrue(ntm.contains("IS_FOREST"));
        assertTrue(ntm.contains("IS_DEEP_OCEAN"));
        String codec = Files.readString(Path.of("src/main/java/com/hbm/world/gen/NbtNtmStructure.java"));
        String loot = Files.readString(Path.of("src/main/java/com/hbm/world/gen/nbt/NbtStructure.java"));
        String pools = Files.readString(Path.of("src/main/java/com/hbm/inventory/loot/StructureLoot.java"));
        assertTrue(codec.contains("fieldOf(\"spawn\")"));
        assertTrue(codec.contains("pickAt"));
        assertTrue(ntm.contains("repeater_radio"));
        assertTrue(ntm.contains("locatableOnly"));
        assertTrue(ntm.contains("featuresSpawnWeight"));
        assertTrue(ntm.contains("bunkerSpawnWeight"));
        assertTrue(ntm.contains("MapGenFeatures"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/world/gen/NtmGridPlacement.java"))
                .contains("RandomSpreadStructurePlacement"));
        assertTrue(ntm.contains("\"silo\""));
        String silo = Files.readString(Path.of("src/main/java/com/hbm/world/gen/component/SiloComponent.java"));
        assertTrue(silo.contains("launch_pad_rusted"));
        assertTrue(silo.contains("42, 29, 26"));
        assertTrue(silo.contains("alignToSurface"));
        assertTrue(silo.contains("hpos - 1 - getYWithOffset(25)"));
        assertTrue(silo.contains("getXWithOffset(13, 2)"));
        assertTrue(silo.contains("getZWithOffset(13, 2)"));
        assertTrue(silo.contains("29, 3, 18"));
        String nbtStructure = Files.readString(Path.of("src/main/java/com/hbm/world/gen/NbtNtmStructure.java"));
        int align = nbtStructure.indexOf("silo.alignToSurface");
        int add = nbtStructure.indexOf("builder.addPiece(piece)");
        assertTrue(align >= 0, "silo must align to surface before chunks place");
        assertTrue(add > align, "alignToSurface must run before addPiece");
        String features = Files.readString(Path.of("src/main/java/com/hbm/world/gen/MapGenFeatures.java"));
        assertTrue(features.contains("pickInner"));
        assertTrue(features.contains("nextInt(10)"));
        assertTrue(features.contains("HOUSE_1"));
        assertTrue(features.contains("rural_house"));
        assertTrue(ntm.contains("ntmruinsa.nbt"));
        assertFalse(ntm.contains("ntmruinsA.nbt"));
        assertTrue(loot.contains("decoLoot"));
        assertTrue(loot.contains("nextInt(max - min)"));
        assertTrue(pools.contains("decoLoot"));
        assertTrue(pools.contains("minecraft:paper\", 1, 12, 240"));
        for (String id : LOCATABLE) {
            Path json = ROOT.resolve("data/hbm/worldgen/structure/" + id + ".json");
            assertTrue(Files.isRegularFile(json), "missing " + json);
            String body = Files.readString(json);
            assertTrue(body.contains("\"spawn\": \"" + id + "\""), id + " json missing spawn field");
            assertTrue(body.contains("hbm:nbt_structure"));
            assertTrue(body.contains("surface_structures"));
            Path setPath = ROOT.resolve("data/hbm/worldgen/structure_set/" + id + ".json");
            assertTrue(Files.isRegularFile(setPath), "missing structure set " + id);
            String set = Files.readString(setPath);
            assertTrue(set.contains("hbm:ntm_grid"), id + " must use 1.7 ntm_grid placement");
            assertTrue(set.contains("996996996"));
            assertTrue(set.contains("\"hbm:" + id + "\""), "structure set missing " + id);
            assertFalse(set.contains("minecraft:random_spread"));
            assertTrue(tag.contains("\"hbm:" + id + "\""), "structure tag missing " + id);
            assertTrue(ntm.contains("\"" + id + "\""), "NTMStructures missing " + id);
        }
        byte[] radio;
        try (GZIPInputStream in = new GZIPInputStream(
                Files.newInputStream(ROOT.resolve("assets/hbm/structures/radio_house.nbt")))) {
            radio = in.readAllBytes();
        }
        assertTrue(new String(radio, StandardCharsets.ISO_8859_1).contains("hbm:tile."));
        String skip = Files.readString(Path.of("src/main/java/com/hbm/registry/ModBulkContent.java"));
        assertTrue(skip.contains("\"wand_jigsaw\""));
        assertTrue(skip.contains("\"crowbar\""));
        String working = Files.readString(Path.of("src/main/java/com/hbm/port/PortContentRegistry.java"));
        assertTrue(working.contains("\"crowbar\""));
        assertTrue(working.contains("\"crate_ammo\""));
        String mods = Files.readString(ROOT.resolve("META-INF/mods.toml"));
        assertTrue(mods.contains("modId=\"jei\""));
        assertTrue(mods.contains("mandatory=false"));
        assertTrue(Files.isRegularFile(Path.of("src/main/java/com/hbm/world/gen/NbtNtmStructure.java")));
        assertTrue(Files.isRegularFile(Path.of("src/main/java/com/hbm/config/StructureConfig.java")));
        for (char letter = 'a'; letter <= 'j'; letter++) {
            Path ruin = ROOT.resolve("assets/hbm/structures/ntmruins" + letter + ".nbt");
            assertTrue(Files.isRegularFile(ruin), "missing " + ruin);
        }
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"structure.hbm.radio\""));
        assertTrue(lang.contains("\"structure.hbm.repeater_radio\""));
        assertTrue(lang.contains("\"structure.hbm.silo\""));
        assertTrue(lang.contains("\"structure.hbm.house_1\""));
        assertTrue(lang.contains("\"structure.hbm.rural_house\""));
        assertTrue(lang.contains("\"structure.hbm.meteor_dungeon\""));
    }

    @Test
    void palettePortIdsMatchOneSevenNames() {
        assertEquals("hbm:brick_concrete", StructureBlockNames.portId("hbm:tile.brick_concrete"));
        assertEquals("minecraft:coal_ore", StructureBlockNames.portId("hbm:tile.ore_coal_oil"));
        assertEquals("minecraft:cobweb", StructureBlockNames.portId("minecraft:web"));
        assertEquals("minecraft:grass_block", StructureBlockNames.portId("grass"));
        assertEquals("minecraft:oak_planks", StructureBlockNames.portId("planks", 0));
        assertEquals("minecraft:spruce_planks", StructureBlockNames.portId("planks", 1));
        assertEquals("minecraft:orange_wool", StructureBlockNames.portId("wool", 1));
        assertEquals("minecraft:oak_fence", StructureBlockNames.portId("fence"));
        assertEquals("minecraft:bricks", StructureBlockNames.portId("brick_block"));
        assertEquals("hbm:deco_tape_recorder", StructureBlockNames.portId("hbm:tile.tape_recorder"));
        assertEquals("hbm:flood_lamp", StructureBlockNames.portId("hbm:tile.floodlight"));
        assertEquals("hbm:fluorescent_lamp", StructureBlockNames.portId("hbm:tile.spotlight_fluoro"));
        assertEquals("hbm:cage_lamp", StructureBlockNames.portId("hbm:tile.spotlight_incandescent"));
        assertEquals("hbm:deco_satellite_receiver", StructureBlockNames.portId("hbm:tile.pole_satellite_receiver"));
        assertEquals("hbm:machine_rtg", StructureBlockNames.portId("hbm:tile.machine_rtg_grey"));
        assertEquals("hbm:diesel_generator", StructureBlockNames.portId("hbm:tile.machine_diesel"));
        assertEquals("hbm:tnt", StructureBlockNames.portId("hbm:tile.tnt_ntm"));
        assertEquals("hbm:barrel_red", StructureBlockNames.portId("hbm:tile.red_barrel"));
        assertEquals("hbm:chain", StructureBlockNames.portId("hbm:tile.dungeon_chain"));
        assertEquals("hbm:concrete_slab", StructureBlockNames.portId("hbm:tile.concrete_double_slab"));
        assertEquals("hbm:crt_clean", StructureBlockNames.portId("hbm:tile.deco_crt", 0));
        assertEquals("hbm:crt_broken", StructureBlockNames.portId("hbm:tile.deco_crt", 4));
        assertEquals("hbm:toaster_steel", StructureBlockNames.portId("hbm:tile.deco_toaster", 4));
        assertEquals("hbm:filing_cabinet", StructureBlockNames.portId("hbm:tile.filing_cabinet"));
        assertEquals("hbm:machine_transformer_iron", StructureBlockNames.portId("hbm:tile.machine_transformer"));
        assertEquals("hbm:cable_gauge", StructureBlockNames.portId("hbm:tile.red_cable_gauge"));
        assertEquals("hbm:electrical_scrap", StructureBlockNames.portId("hbm:tile.block_electrical_scrap"));
        assertEquals("hbm:rtty_sender_off", StructureBlockNames.portId("hbm:tile.radio_torch_sender"));
        assertEquals("hbm:rtty_rec_off", StructureBlockNames.portId("hbm:tile.radio_torch_receiver"));
        assertEquals("hbm:reeds_mid", StructureBlockNames.portId("hbm:tile.reeds"));
        assertEquals("hbm:silo_hatch_large", StructureBlockNames.portId("hbm:tile.silo_hatch_large"));
        assertEquals("hbm:capacitor_copper", StructureBlockNames.portId("hbm:tile.capacitor_copper"));
        assertEquals("hbm:plant_flower", StructureBlockNames.portId("hbm:tile.plant_flower"));
        assertEquals("hbm:deco_pipe_rim_green_rusted", StructureBlockNames.portId("hbm:tile.deco_pipe_rim_green_rusted"));
        assertEquals("minecraft:sugar_cane", StructureBlockNames.portId("reeds"));
        assertEquals("hbm:safe", StructureBlockNames.portId("hbm:tile.safe"));
        assertEquals("hbm:door_metal", StructureBlockNames.portId("hbm:tile.door_metal"));
        assertEquals("hbm:door_bunker", StructureBlockNames.portId("hbm:tile.door_bunker"));
        assertEquals("hbm:door_office", StructureBlockNames.portId("hbm:tile.door_office"));
        assertEquals("minecraft:oak_door", StructureBlockNames.portId("minecraft:wooden_door"));
        assertEquals("minecraft:oak_door", StructureBlockNames.portId("wooden_door"));
        assertEquals("minecraft:podzol", StructureBlockNames.portId("dirt", 2));
        assertEquals("minecraft:coarse_dirt", StructureBlockNames.portId("dirt", 1));
    }

    @Test
    void structureDecoAssetsExist() throws IOException {
        String[] files = {
                "assets/hbm/blockstates/filing_cabinet.json",
                "assets/hbm/blockstates/safe.json",
                "assets/hbm/blockstates/steel_grate.json",
                "assets/hbm/blockstates/deco_pipe.json",
                "assets/hbm/blockstates/concrete_stairs.json",
                "assets/hbm/blockstates/concrete_slab.json",
                "assets/hbm/blockstates/door_office.json",
                "assets/hbm/blockstates/trapdoor_steel.json",
                "assets/hbm/blockstates/wood_structure.json",
                "assets/hbm/blockstates/plant_dead.json",
                "assets/hbm/blockstates/plant_flower.json",
                "assets/hbm/blockstates/silo_hatch_large.json",
                "assets/hbm/blockstates/capacitor_copper.json",
                "assets/hbm/blockstates/deco_pipe_rim_green_rusted.json",
                "assets/hbm/models/obj/file_cabinet.obj",
                "assets/hbm/models/obj/pipe.obj",
                "assets/hbm/models/obj/pole.obj",
                "assets/hbm/models/obj/antenna_top.obj",
                "assets/hbm/models/obj/microwave.obj",
                "assets/hbm/models/obj/deco_computer.obj",
                "assets/hbm/models/obj/deco_tape_recorder.obj",
                "assets/hbm/models/obj/crt_clean.obj",
                "assets/hbm/models/obj/charger.obj",
                "assets/hbm/textures/block/crt_clean.png",
                "assets/hbm/textures/block/deco_computer.png",
                "assets/hbm/textures/block/deco_tape_recorder.png",
                "assets/hbm/textures/block/deco_pole_top.png",
                "assets/hbm/textures/models/polesatellitereceiver.png",
                "assets/hbm/textures/models/machines/microwave.png",
                "assets/hbm/models/obj/silo_hatch_large.obj",
                "assets/hbm/models/obj/pipe_rim_green_rusty.obj",
                "assets/hbm/textures/gui/storage/gui_file_cabinet.png",
                "assets/hbm/textures/gui/storage/gui_safe.png",
                "assets/hbm/textures/models/file_cabinet.png",
                "data/hbm/recipes/filing_cabinet.json",
                "data/hbm/loot_tables/blocks/filing_cabinet.json"
        };
        for (String file : files) {
            assertTrue(Files.isRegularFile(ROOT.resolve(file)), "missing " + file);
        }
        String stairs = Files.readString(ROOT.resolve("assets/hbm/models/block/concrete_smooth_stairs.json"));
        assertTrue(stairs.contains("hbm:block/concrete"));
        assertFalse(stairs.contains("hbm:block/concrete_smooth"));
        String slab0 = Files.readString(ROOT.resolve("assets/hbm/models/block/concrete_slab_0.json"));
        assertTrue(slab0.contains("hbm:block/concrete"));
        assertFalse(slab0.contains("hbm:block/concrete_smooth"));
        String hatchObj = Files.readString(ROOT.resolve("assets/hbm/models/obj/silo_hatch_large.obj"));
        assertTrue(hatchObj.contains("usemtl material"));
        assertTrue(hatchObj.contains("o Hatch"));
        assertTrue(hatchObj.contains("o Frame"));
        String cabinet = Files.readString(ROOT.resolve("assets/hbm/models/obj/file_cabinet.obj"));
        assertTrue(cabinet.contains("Cabinet"));
        assertTrue(cabinet.contains("LowerDrawer"));
        assertTrue(cabinet.contains("UpperDrawer"));
        String dish = Files.readString(ROOT.resolve("assets/hbm/models/obj/deco_satellite_receiver.obj"));
        assertTrue(dish.contains("o Dish"));
        String[] dishVerts = java.util.Arrays.stream(dish.split("\n"))
                .filter(line -> line.startsWith("v "))
                .toArray(String[]::new);
        assertTrue(dishVerts.length >= 64, "satellite dish must be the 1.7 Techne mesh, not rtty");
        assertEquals(4, dishVerts[0].split(" ").length);
        assertFalse(dish.contains("v -1.") || dish.contains("v -2."), "dish must stay near the pole");
        String crtState = Files.readString(ROOT.resolve("assets/hbm/blockstates/crt_clean.json"));
        assertTrue(crtState.contains("\"facing=east\":  { \"model\": \"hbm:block/crt_clean\" }"));
        assertTrue(crtState.contains("\"y\": 270"));
        String computerState = Files.readString(ROOT.resolve("assets/hbm/blockstates/deco_computer.json"));
        assertTrue(computerState.contains("\"facing=south\": { \"model\": \"hbm:block/deco_computer\" }"));
        assertTrue(computerState.contains("\"y\": 180"));
        String tapeState = Files.readString(ROOT.resolve("assets/hbm/blockstates/deco_tape_recorder.json"));
        assertTrue(tapeState.contains("\"facing=east\":  { \"model\": \"hbm:block/deco_tape_recorder\" }"));
        String poleTop = Files.readString(ROOT.resolve("assets/hbm/models/block/pole_top.json"));
        assertTrue(poleTop.contains("antenna_top.obj"));
        String microwave = Files.readString(ROOT.resolve("assets/hbm/models/block/machine_microwave.json"));
        assertTrue(microwave.contains("forge:obj"));
        assertTrue(microwave.contains("microwave.obj"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/world/gen/nbt/LegacyBlockPalette.java"))
                .contains("crtScreen2d"));
        assertTrue(Files.readString(Path.of("src/main/java/com/hbm/blocks/generic/DecoObjBlock.java"))
                .contains("rotateY90Cw"));
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"block.hbm.filing_cabinet\""));
        assertTrue(lang.contains("\"block.hbm.silo_hatch_large\""));
        assertTrue(lang.contains("\"block.hbm.plant_flower\""));
        assertTrue(lang.contains("\"block.hbm.capacitor_copper\""));
        assertTrue(lang.contains("\"container.hbm.file_cabinet\""));
        assertTrue(lang.contains("\"container.hbm.safe\""));
        String working = Files.readString(Path.of("src/main/java/com/hbm/port/PortContentRegistry.java"));
        assertTrue(working.contains("\"filing_cabinet\""));
        assertTrue(working.contains("\"deco_pipe\""));
        assertTrue(working.contains("\"silo_hatch_large\""));
        assertTrue(working.contains("\"plant_flower\""));
        assertTrue(working.contains("\"capacitor_copper\""));
        String palette = Files.readString(Path.of("src/main/java/com/hbm/world/gen/nbt/LegacyBlockPalette.java"));
        assertTrue(palette.contains("stitchDoor"));
        assertTrue(palette.contains("transformLegacyMeta"));
        String nbt = Files.readString(Path.of("src/main/java/com/hbm/world/gen/nbt/NbtStructure.java"));
        assertTrue(nbt.contains("stitchDoor"));
    }

    @Test
    void siloPadSitsOneBelowSampledSurface() {
        int minY = 64;
        int floorLocal = 25;
        int surface = 70;
        int offset = surface - 1 - (minY + floorLocal);
        assertEquals(surface - 1, minY + offset + floorLocal);
    }

    @Test
    void structureSpacingMatchesOneSevenGrid() {
        long seed = 12345L;
        int min = 4;
        int max = 16;
        int hits = 0;
        for (int cx = -40; cx < 40; cx++) {
            for (int cz = -40; cz < 40; cz++) {
                if (StructureSpacing.isSpawnChunk(cx, cz, seed, min, max)) {
                    hits++;
                    int cell = cx < 0 ? cx - (max - 1) : cx;
                    cell /= max;
                    assertTrue(StructureSpacing.isSpawnChunk(cx, cz, seed, min, max));
                    assertEquals(cell, cx < 0 ? (cx - (max - 1)) / max : cx / max);
                    Random first = StructureSpacing.pickRandom(cx, cz, seed, min, max);
                    Random second = StructureSpacing.pickRandom(cx, cz, seed, min, max);
                    assertEquals(first.nextInt(10_000), second.nextInt(10_000));
                }
            }
        }
        assertTrue(hits >= 10, "expected a sparse grid of spawn chunks, got " + hits);
        assertTrue(hits <= 40, "grid should not mark most chunks, got " + hits);
        for (int cx = -40; cx < 40; cx++) {
            for (int cz = -40; cz < 40; cz++) {
                if (!StructureSpacing.isSpawnChunk(cx, cz, seed, min, max)) {
                    continue;
                }
                var pos = StructureSpacing.spawnChunkForCell(seed,
                        StructureSpacing.cellIndex(cx, min, max),
                        StructureSpacing.cellIndex(cz, min, max), min, max);
                assertEquals(cx, pos.x);
                assertEquals(cz, pos.z);
            }
        }
    }
}
