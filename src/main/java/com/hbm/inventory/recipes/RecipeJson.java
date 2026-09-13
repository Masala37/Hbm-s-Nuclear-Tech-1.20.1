package com.hbm.inventory.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RecipeJson {
    private RecipeJson() {
    }

    public static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    public static JsonObject readResource(String classpath) throws IOException {
        try (InputStream in = RecipeJson.class.getResourceAsStream(classpath)) {
            if (in == null) {
                throw new IOException("Missing recipe resource " + classpath);
            }
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }

    public static JsonObject readPreferSource(Path source, String classpath) throws IOException {
        if (Files.exists(source)) {
            return read(source);
        }
        return readResource(classpath);
    }
}
