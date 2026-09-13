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

/**
 * 1.20 {@code ResourceLocation} paths may only contain {@code [a-z0-9/._-]}.
 * Uppercase 1.7 leftover paths crash during class init (menus) or structure bootstrap.
 */
class ResourceLocationValidityTest {
    private static final Pattern TWO_ARG = Pattern.compile(
            "new ResourceLocation\\(\\s*(?:RefStrings\\.MODID|\"[a-z0-9_.-]+\")\\s*,\\s*\"([^\"]+)\"");
    private static final Pattern ONE_ARG = Pattern.compile(
            "new ResourceLocation\\(\\s*\"([a-z0-9_.-]+):([^\"]+)\"");
    private static final Pattern VALID = Pattern.compile("[a-z0-9/._-]+");

    @Test
    void javaResourceLocationLiteralsAreValid() throws IOException {
        Path java = Path.of("src/main/java");
        List<String> bad = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(java)) {
            stream.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String text = Files.readString(path);
                    collectInvalid(bad, path, TWO_ARG.matcher(text), 1);
                    Matcher one = ONE_ARG.matcher(text);
                    while (one.find()) {
                        String locPath = one.group(2);
                        if (!VALID.matcher(locPath).matches()) {
                            bad.add(path.getFileName() + " " + one.group(1) + ":" + locPath);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        assertTrue(bad.isEmpty(), "Invalid ResourceLocation paths:\n" + String.join("\n", bad));
    }

    @Test
    void processResourcesDropsUppercaseAssetPaths() throws IOException {
        String gradle = Files.readString(Path.of("build.gradle"));
        assertTrue(gradle.contains("normalized.startsWith('assets/hbm/')"));
        assertTrue(gradle.contains("normalized =~ /[A-Z]/"));
    }

    private static void collectInvalid(List<String> bad, Path path, Matcher matcher, int group) {
        while (matcher.find()) {
            String locPath = matcher.group(group);
            if (!VALID.matcher(locPath).matches()) {
                bad.add(path.getFileName() + " " + locPath);
            }
        }
    }
}
