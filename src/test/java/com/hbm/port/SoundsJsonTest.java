package com.hbm.port;

import com.hbm.registry.SoundEventCatalog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundsJsonTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hbm");
    private static final Pattern EVENT_KEY = Pattern.compile("^\\s*\"([^\"]+)\":", Pattern.MULTILINE);
    private static final Pattern SOUND_NAME = Pattern.compile("\"name\":\\s*\"hbm:([^\"]+)\"");
    private static final Pattern VALID_PATH = Pattern.compile("[a-z0-9/._-]+");

    @Test
    void soundsJsonPathsAreValidAndFilesExist() throws IOException {
        Path jsonPath = ASSETS.resolve("sounds.json");
        assertTrue(Files.isRegularFile(jsonPath), "sounds.json missing");
        String json = Files.readString(jsonPath);
        Set<String> keys = new LinkedHashSet<>();
        Matcher keyMatcher = EVENT_KEY.matcher(json);
        while (keyMatcher.find()) {
            String key = keyMatcher.group(1);
            if (key.equals("sounds") || key.equals("name") || key.equals("stream")) {
                continue;
            }
            assertTrue(VALID_PATH.matcher(key).matches(), "Invalid event key: " + key);
            keys.add(key);
        }
        assertTrue(keys.size() >= 300, "Expected the 1.7 sound catalog, got " + keys.size());

        List<String> missingFiles = new ArrayList<>();
        Matcher nameMatcher = SOUND_NAME.matcher(json);
        while (nameMatcher.find()) {
            String rel = nameMatcher.group(1);
            assertTrue(VALID_PATH.matcher(rel).matches(), "Invalid sound file path: " + rel);
            Path ogg = ASSETS.resolve("sounds/" + rel + ".ogg");
            if (!Files.isRegularFile(ogg)) {
                missingFiles.add(rel + ".ogg");
            }
        }
        assertTrue(missingFiles.isEmpty(), "Missing sound files: " + missingFiles);

        Set<String> catalog = Set.of(SoundEventCatalog.PATHS);
        List<String> missingKeys = new ArrayList<>();
        for (String key : keys) {
            if (!catalog.contains(key)) {
                missingKeys.add(key);
            }
        }
        assertTrue(missingKeys.isEmpty(), "Catalog missing sounds.json keys: " + missingKeys);
    }

    @Test
    void namedModSoundsExistInCatalog() {
        String[] required = {
                "tool.tech_bleep",
                "item.tech_boop",
                "weapon.missile_takeoff",
                "block.sonar_ping",
                "block.engine",
                "block.diesel_operate",
                "block.crate_open",
                "block.crate_close"
        };
        Set<String> catalog = Set.of(SoundEventCatalog.PATHS);
        List<String> missing = new ArrayList<>();
        for (String path : required) {
            if (!catalog.contains(path)) {
                missing.add(path);
            }
        }
        assertTrue(missing.isEmpty(), "Named sounds missing from catalog: " + missing);
    }
}
