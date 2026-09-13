package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayableTextureModelFixesTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void cableModelsUseVanillaFaceTextureKeys() throws IOException {
        for (String name : List.of(
                "cable_switch_on", "cable_switch_off",
                "cable_detector_on", "cable_detector_off", "cable_diode")) {
            Path json = ROOT.resolve("assets/hbm/models/block/" + name + ".json");
            String text = Files.readString(json);
            assertTrue(text.contains("\"textures\""), name + " missing textures object");
            assertTrue(text.contains("\"texture\": \"#all\""), name + " missing vanilla face texture key");
            assertFalse(text.contains("\"hbm:block/" + name + "s\""), name + " still has mangled textures key");
            assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/block/" + name + ".png")),
                    "missing " + name + " png");
        }
    }

    @Test
    void oilAndSteamBucketsUseFluidContainerModels() throws IOException {
        for (String id : List.of(
                "oil_bucket", "naphtha_bucket", "hotoil_bucket", "gas_bucket",
                "bitumen_bucket", "smear_bucket", "heatingoil_bucket", "crackoil_bucket",
                "oil_ds_bucket", "crackoil_ds_bucket", "sourgas_bucket", "reformate_bucket",
                "heavyoil_vacuum_bucket", "lightoil_vacuum_bucket", "heatingoil_vacuum_bucket",
                "reformgas_bucket", "diesel_bucket", "hydrogen_bucket", "bucket_toxic",
                "uf6_bucket", "puf6_bucket", "vitriol_bucket", "redmud_bucket", "watz_bucket")) {
            Path json = ROOT.resolve("assets/hbm/models/item/" + id + ".json");
            assertTrue(Files.isRegularFile(json), "missing " + id);
            String text = Files.readString(json);
            assertTrue(text.contains("\"loader\": \"forge:fluid_container\""), id + " not a fluid container");
            assertTrue(text.contains("\"parent\": \"forge:item/bucket\""), id + " missing bucket parent");
        }
        String naphtha = Files.readString(ROOT.resolve("assets/hbm/models/item/naphtha_bucket.json"));
        assertTrue(naphtha.contains("\"fluid\": \"hbm:naphtha\""));
        String toxic = Files.readString(ROOT.resolve("assets/hbm/models/item/bucket_toxic.json"));
        assertTrue(toxic.contains("\"fluid\": \"hbm:toxic\""));
        for (String fluid : List.of("crackoil", "oil_ds", "crackoil_ds")) {
            String states = Files.readString(ROOT.resolve("assets/hbm/blockstates/" + fluid + ".json"));
            assertTrue(states.contains("\"level=0\""), fluid + " missing level=0");
            assertTrue(states.contains("\"level=15\""), fluid + " missing level=15");
            assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/models/block/" + fluid + ".json")),
                    "missing " + fluid + " block model");
        }
    }

    @Test
    void blockAtlasIncludesObjModelTextures() throws IOException {
        String atlas = Files.readString(ROOT.resolve("assets/minecraft/atlases/blocks.json"));
        assertTrue(atlas.contains("\"source\": \"models\""));
        assertTrue(atlas.contains("\"prefix\": \"models/\""));
    }

    @Test
    void smallPylonAndGeigerKitHaveValidResourcePaths() throws IOException {
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/model_pylon.png")));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/item/geiger_kit.png")));
        String kit = Files.readString(ROOT.resolve("assets/hbm/models/item/geiger_kit.json"));
        assertTrue(kit.contains("hbm:item/geiger_kit"));
        String render = Files.readString(Path.of("src/main/java/com/hbm/client/render/blockentity/RenderPylon.java"));
        assertTrue(render.contains("textures/models/model_pylon.png"));
        assertFalse(render.contains("ModelPylon.png"));
    }

    @Test
    void satelliteReceiverUsesLowercaseTexturePath() throws IOException {
        String model = Files.readString(ROOT.resolve("assets/hbm/models/block/deco_satellite_receiver.json"));
        assertTrue(model.contains("hbm:models/polesatellitereceiver"));
        assertFalse(model.contains("PoleSatelliteReceiver"));
        String mtl = Files.readString(ROOT.resolve("assets/hbm/models/obj/deco_satellite_receiver.mtl"));
        assertTrue(mtl.contains("hbm:models/polesatellitereceiver"));
        assertTrue(Files.isRegularFile(ROOT.resolve("assets/hbm/textures/models/polesatellitereceiver.png")));
        String concrete = Files.readString(ROOT.resolve("assets/hbm/blockstates/concrete_colored_ext.json"));
        assertTrue(concrete.contains("\"variant=7\""));
        assertFalse(concrete.contains("\"variant=8\""));
        String treasure = Files.readString(Path.of("src/main/java/com/hbm/inventory/loot/StructureLoot.java"));
        assertTrue(treasure.contains("hbm:cobalt_pickaxe"));
        assertTrue(treasure.contains("hbm:ingot_zirconium"));
        String molten = Files.readString(Path.of("src/main/java/com/hbm/blocks/generic/MeteorMoltenBlock.java"));
        assertTrue(molten.contains("Blocks.LAVA"));
        assertTrue(molten.contains("BLOCK_METEOR_COBBLE"));
    }
}
