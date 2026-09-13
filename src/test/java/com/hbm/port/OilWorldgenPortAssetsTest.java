package com.hbm.port;

import com.hbm.items.tool.OilDetectorScan;
import com.hbm.world.feature.OilBubbleMath;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OilWorldgenPortAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void oilWorldgenAssetsExist() throws IOException {
        String[] files = {
                "data/hbm/worldgen/configured_feature/oil_bubble.json",
                "data/hbm/worldgen/configured_feature/oil_sand_bubble.json",
                "data/hbm/worldgen/placed_feature/oil_bubble.json",
                "data/hbm/worldgen/placed_feature/oil_sand_bubble.json",
                "data/hbm/forge/biome_modifier/add_oil_bubble.json",
                "data/hbm/forge/biome_modifier/add_oil_sand_bubble.json",
                "assets/hbm/models/item/oil_detector.json",
                "assets/hbm/textures/item/oil_detector.png"
        };
        for (String file : files) {
            assertTrue(Files.isRegularFile(ROOT.resolve(file)), "missing " + file);
        }
        String bubble = Files.readString(ROOT.resolve("data/hbm/worldgen/configured_feature/oil_bubble.json"));
        assertTrue(bubble.contains("\"type\":\"hbm:oil_bubble\"") || bubble.contains("\"type\": \"hbm:oil_bubble\""));
        String modifier = Files.readString(ROOT.resolve("data/hbm/forge/biome_modifier/add_oil_bubble.json"));
        assertTrue(modifier.contains("underground_ores"));
        assertTrue(modifier.contains("hbm:oil_bubble"));
        String lang = Files.readString(ROOT.resolve("assets/hbm/lang/en_us.json"));
        assertTrue(lang.contains("\"item.hbm.oil_detector.bullseye\""));
        assertTrue(lang.contains("\"item.hbm.oil_detector.noOil\""));
        assertFalse(Files.exists(ROOT.resolve("data/hbm/recipes/oil_detector.json")));
    }

    @Test
    void oilBubbleMathMatchesOneSeven() {
        assertEquals(32.0D, OilBubbleMath.radiusSqr(8.0D), 1e-9);
        assertEquals(3, OilBubbleMath.neighborRange(16));
        assertEquals(7, OilBubbleMath.neighborRange(48));
        assertTrue(OilBubbleMath.inside(0, 0, 0, 32.0D, 0.0D));
        assertFalse(OilBubbleMath.inside(20, 0, 0, 32.0D, 0.0D));
        assertTrue(OilBubbleMath.inside(0, 2, 0, 32.0D, 0.0D));
        assertFalse(OilBubbleMath.inside(0, 4, 0, 32.0D, 0.0D));
    }

    @Test
    void oilDetectorScanMatchesOneSevenOffsets() {
        OilDetectorScan.OilProbe none = (x, y, z) -> false;
        assertEquals(OilDetectorScan.Hit.NONE, OilDetectorScan.scan(none, 0, 64, 0));

        OilDetectorScan.OilProbe underfoot = (x, y, z) -> x == 0 && z == 0 && y == 20;
        assertEquals(OilDetectorScan.Hit.DIRECT, OilDetectorScan.scan(underfoot, 0, 64, 0));

        OilDetectorScan.OilProbe nearby = (x, y, z) -> x == 5 && z == 0 && y == 20;
        assertEquals(OilDetectorScan.Hit.NEARBY, OilDetectorScan.scan(nearby, 0, 64, 0));

        OilDetectorScan.OilProbe far = (x, y, z) -> x == 10 && z == 0 && y == 8;
        assertEquals(OilDetectorScan.Hit.NEARBY, OilDetectorScan.scan(far, 0, 64, 0));

        OilDetectorScan.OilProbe deep = (x, y, z) -> x == 0 && z == 0 && y == -20;
        assertEquals(OilDetectorScan.Hit.DIRECT, OilDetectorScan.scan(deep, 0, 64, 0));

        OilDetectorScan.OilProbe farTooDeep = (x, y, z) -> x == 10 && z == 0 && y == -58;
        assertEquals(OilDetectorScan.Hit.NONE, OilDetectorScan.scan(farTooDeep, 0, 64, 0));
    }
}
