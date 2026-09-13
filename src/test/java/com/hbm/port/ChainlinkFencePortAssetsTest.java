package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainlinkFencePortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void chainlinkAssetsExist() {
        String[] files = {
                "assets/hbm/blockstates/fence_metal.json",
                "assets/hbm/models/block/fence_metal_post.json",
                "assets/hbm/models/block/fence_metal_side.json",
                "assets/hbm/models/item/fence_metal.json",
                "assets/hbm/models/item/fence_metal_post.json",
                "assets/hbm/textures/block/fence_metal.png",
                "assets/hbm/textures/block/fence_metal_post.png",
                "data/hbm/loot_tables/blocks/fence_metal.json",
                "data/hbm/recipes/fence_metal.json",
                "data/hbm/recipes/fence_metal_post.json",
                "data/hbm/recipes/fence_metal_from_post.json"
        };
        for (String file : files) {
            assertTrue(Files.isRegularFile(ROOT.resolve(file)), "missing " + file);
        }
    }
}
