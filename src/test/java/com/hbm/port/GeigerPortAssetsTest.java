package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeigerPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void geigerAssetsExist() {
        String[] files = {
                "assets/hbm/models/obj/geiger.obj",
                "assets/hbm/models/obj/geiger.mtl",
                "assets/hbm/models/block/geiger.json",
                "assets/hbm/models/item/geiger.json",
                "assets/hbm/models/item/geiger_counter.json",
                "assets/hbm/blockstates/geiger.json",
                "assets/hbm/textures/block/geiger.png",
                "assets/hbm/textures/item/geiger_counter.png",
                "assets/hbm/textures/misc/overlay_misc.png",
                "assets/hbm/sounds/tool/geiger1.ogg",
                "data/hbm/loot_tables/blocks/geiger.json",
                "data/hbm/recipes/geiger.json",
                "data/hbm/recipes/geiger_counter.json",
                "data/hbm/recipes/geiger_counter_biorubber.json",
                "data/hbm/recipes/geiger_counter_from_block.json"
        };
        for (String file : files) {
            assertTrue(Files.isRegularFile(ROOT.resolve(file)), "missing " + file);
        }
    }
}
