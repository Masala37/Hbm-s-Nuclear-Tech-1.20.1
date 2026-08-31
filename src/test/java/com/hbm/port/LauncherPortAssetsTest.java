package com.hbm.port;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LauncherPortAssetsTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hbm");
    private static final Path DATA = Path.of("src/main/resources/data");
    private static final Pattern HBM_PATH = Pattern.compile("\"hbm:([^\"]+)\"");

    @Test
    void launcherAndDesignatorAssetsExist() throws IOException {
        String[] files = {
                "models/obj/compact_launcher.obj",
                "textures/models/compact_launcher.png",
                "textures/block/compact_launcher.png",
                "textures/block/launch_table.png",
                "models/obj/launch_table/launch_table_base.obj",
                "models/obj/launch_table/launch_table_small_pad.obj",
                "models/obj/launch_table/launch_table_large_pad.obj",
                "models/obj/launch_table/launch_table_large_scaffold_base.obj",
                "models/obj/launch_table/launch_table_large_scaffold_connector.obj",
                "models/obj/launch_table/launch_table_large_scaffold_empty.obj",
                "models/obj/launch_table/launch_table_small_scaffold_base.obj",
                "models/obj/launch_table/launch_table_small_scaffold_connector.obj",
                "models/obj/launch_table/launch_table_small_scaffold_empty.obj",
                "textures/models/missile_parts/launch_table.png",
                "textures/models/missile_parts/launch_table_small_pad.png",
                "textures/models/missile_parts/launch_table_large_pad.png",
                "textures/models/missile_parts/launch_table_large_scaffold_base.png",
                "textures/models/missile_parts/launch_table_large_scaffold_connector.png",
                "textures/models/missile_parts/launch_table_small_scaffold_base.png",
                "textures/models/missile_parts/launch_table_small_scaffold_connector.png",
                "textures/gui/weapon/gui_launch_table_small.png",
                "textures/gui/weapon/gui_launch_table.png",
                "textures/gui/gui_utility.png",
                "textures/gui/gui_designator.png",
                "textures/item/designator_manual.png",
                "textures/item/diesel_bucket.png",
                "textures/item/rocket_fuel.png",
                "textures/particle/contrail.png",
                "models/item/compact_launcher.json",
                "models/item/launch_table.json",
                "models/item/designator_manual.json",
                "models/item/hydrogen_bucket.json",
                "models/item/xenon_bucket.json",
                "models/item/balefire_bucket.json",
                "models/item/missile_custom.json",
                "sounds/tool/tech_bleep.ogg",
                "sounds/weapon/missile_takeoff.ogg",
                "sounds/weapon/missile_takeoff_alt.ogg"
        };
        List<String> missing = new ArrayList<>();
        for (String file : files) {
            if (!Files.isRegularFile(ASSETS.resolve(file))) {
                missing.add(file);
            }
        }
        assertTrue(missing.isEmpty(), "Missing launcher/designator assets: " + missing);
        assertHasVertices(ASSETS.resolve("models/obj/compact_launcher.obj"));
        assertHasVertices(ASSETS.resolve("models/obj/launch_table/launch_table_base.obj"));
    }

    @Test
    void lootLangAndChipIconsExist() throws IOException {
        assertTrue(Files.isRegularFile(DATA.resolve("hbm/loot_tables/blocks/compact_launcher.json")));
        assertTrue(Files.isRegularFile(DATA.resolve("hbm/loot_tables/blocks/launch_table.json")));
        String lang = Files.readString(ASSETS.resolve("lang/en_us.json"));
        for (String key : List.of(
                "\"block.hbm.compact_launcher\"",
                "\"block.hbm.launch_table\"",
                "\"item.hbm.designator_manual\"",
                "\"fluid.hbm.hydrogen\"",
                "\"fluid.hbm.xenon\"",
                "\"fluid.hbm.balefire\"")) {
            assertTrue(lang.contains(key), "Missing lang key " + key);
        }
        for (int i = 1; i <= 5; i++) {
            assertTrue(Files.isRegularFile(ASSETS.resolve("textures/item/mp_c_" + i + ".png")),
                    "Missing targeting chip texture mp_c_" + i);
            assertTrue(Files.isRegularFile(ASSETS.resolve("models/item/mp_c_" + i + ".json")),
                    "Missing targeting chip item model mp_c_" + i);
        }
    }

    @Test
    void customMissilePartModelsHaveObjMtlAndTextures() throws IOException {
        Path partDir = ASSETS.resolve("models/block/missile_part");
        assertTrue(Files.isDirectory(partDir));
        List<String> missing = new ArrayList<>();
        int models = 0;
        try (Stream<Path> walk = Files.list(partDir)) {
            for (Path json : walk.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                models++;
                String id = json.getFileName().toString().replace(".json", "");
                if (!Files.isRegularFile(ASSETS.resolve("models/item/" + id + ".json"))) {
                    missing.add("item model " + id);
                }
                String text = Files.readString(json);
                Matcher matcher = HBM_PATH.matcher(text);
                while (matcher.find()) {
                    String rel = matcher.group(1);
                    Path resolved = resolveHbmAsset(rel);
                    if (resolved == null) {
                        continue;
                    }
                    if (!Files.isRegularFile(resolved)) {
                        missing.add(id + " -> " + rel);
                    } else if (rel.endsWith(".obj")) {
                        assertHasVertices(resolved);
                    }
                }
            }
        }
        assertTrue(models >= 117, "Expected 117 missile-part block models, got " + models);
        assertTrue(missing.isEmpty(), "Missing missile-part files: " + missing);
    }

    private static Path resolveHbmAsset(String rel) {
        if (rel.startsWith("models/")) {
            return ASSETS.resolve(rel);
        }
        if (rel.startsWith("block/") || rel.startsWith("item/") || rel.startsWith("entity/")) {
            return ASSETS.resolve("textures/" + rel + ".png");
        }
        return null;
    }

    private static void assertHasVertices(Path obj) throws IOException {
        boolean verts = Files.lines(obj).anyMatch(line -> line.startsWith("v "));
        assertTrue(verts, "OBJ has no vertices: " + obj);
    }
}
