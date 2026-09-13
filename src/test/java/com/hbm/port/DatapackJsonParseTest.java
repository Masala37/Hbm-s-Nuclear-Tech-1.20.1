package com.hbm.port;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Player logs showed truncated {@code ntm_dirt} and unescaped recipe NBT aborting datapack parse.
 */
class DatapackJsonParseTest {
    @Test
    void recipesBlockstatesAndLootParse() throws IOException {
        List<String> bad = new ArrayList<>();
        parseTree(Path.of("src/main/resources/data/hbm/recipes"), bad);
        parseTree(Path.of("src/main/resources/data/hbm/machine_recipes"), bad);
        parseTree(Path.of("src/main/resources/assets/hbm/blockstates"), bad);
        parseTree(Path.of("src/main/resources/data/hbm/loot_tables"), bad);
        assertTrue(bad.isEmpty(), "Invalid JSON:\n" + String.join("\n", bad));
    }

    private static void parseTree(Path root, List<String> bad) throws IOException {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try {
                    JsonParser.parseString(Files.readString(path));
                } catch (Exception e) {
                    bad.add(root.relativize(path) + ": " + e.getMessage());
                }
            });
        }
    }
}
