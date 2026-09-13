package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcasterPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void broadcasterAssetsExist() throws IOException {
        String[] files = {
                "assets/hbm/textures/models/broadcaster.png",
                "assets/hbm/models/block/broadcaster_pc.json",
                "assets/hbm/models/item/broadcaster_pc.json",
                "assets/hbm/blockstates/broadcaster_pc.json",
                "assets/hbm/sounds/block/broadcast1.ogg",
                "assets/hbm/sounds/block/broadcast2.ogg",
                "assets/hbm/sounds/block/broadcast3.ogg",
                "data/hbm/damage_type/broadcast.json",
                "data/hbm/worldgen/configured_feature/broadcaster.json",
                "data/hbm/worldgen/placed_feature/broadcaster.json",
                "data/hbm/forge/biome_modifier/add_broadcaster.json",
                "data/hbm/loot_tables/blocks/broadcaster_pc.json"
        };
        for (String file : files) {
            Path path = ROOT.resolve(file);
            assertTrue(Files.isRegularFile(path), "missing " + file);
        }
    }
}
