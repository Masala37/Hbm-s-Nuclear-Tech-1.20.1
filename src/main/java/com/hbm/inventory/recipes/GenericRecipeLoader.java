package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads 1.7.10 {@code GenericRecipes} dumps. Does not invent recipes.
 */
public final class GenericRecipeLoader {
    private GenericRecipeLoader() {
    }

    public static List<GenericMachineRecipe> read(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray array = root.getAsJsonArray("recipes");
        List<GenericMachineRecipe> recipes = new ArrayList<>();
        if (array == null) {
            return recipes;
        }
        for (JsonElement element : array) {
            recipes.add(readOne(element.getAsJsonObject()));
        }
        return recipes;
    }

    public static List<GenericMachineRecipe> read(Path path) throws IOException {
        return read(Files.readString(path));
    }

    public static List<GenericMachineRecipe> readResource(String classpath) throws IOException {
        try (InputStream in = GenericRecipeLoader.class.getResourceAsStream(classpath)) {
            if (in == null) {
                throw new IOException("Missing recipe resource " + classpath);
            }
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray array = root.getAsJsonArray("recipes");
                List<GenericMachineRecipe> recipes = new ArrayList<>();
                if (array == null) {
                    return recipes;
                }
                for (JsonElement element : array) {
                    recipes.add(readOne(element.getAsJsonObject()));
                }
                return recipes;
            }
        }
    }

    private static GenericMachineRecipe readOne(JsonObject obj) {
        List<String> pools = new ArrayList<>();
        if (obj.has("pools")) {
            for (JsonElement el : obj.getAsJsonArray("pools")) {
                pools.add(el.getAsString());
            }
        }
        return new GenericMachineRecipe(
                obj.get("name").getAsString(),
                obj.has("duration") ? obj.get("duration").getAsInt() : 0,
                obj.has("power") ? obj.get("power").getAsLong() : 0L,
                obj.has("named") && obj.get("named").getAsBoolean(),
                pools,
                obj.has("autoSwitchGroup") ? obj.get("autoSwitchGroup").getAsString() : null,
                readItems(obj, "inputItem"),
                readItems(obj, "outputItem"),
                readFluids(obj, "inputFluid"),
                readFluids(obj, "outputFluid"));
    }

    private static List<GenericMachineRecipe.ItemInput> readItems(JsonObject obj, String key) {
        List<GenericMachineRecipe.ItemInput> list = new ArrayList<>();
        if (!obj.has(key)) {
            return list;
        }
        for (JsonElement el : obj.getAsJsonArray(key)) {
            JsonObject item = el.getAsJsonObject();
            if (item.has("raw") && !item.has("item") && !item.has("ore")) {
                continue;
            }
            String ore = item.has("ore") && !item.get("ore").isJsonNull() ? item.get("ore").getAsString() : null;
            String id = item.has("item") && !item.get("item").isJsonNull() ? item.get("item").getAsString() : null;
            int count = item.has("count") && !item.get("count").isJsonNull() ? item.get("count").getAsInt() : 1;
            String meta = item.has("meta") && !item.get("meta").isJsonNull() ? item.get("meta").getAsString() : null;
            float chance = item.has("chance") && !item.get("chance").isJsonNull()
                    ? item.get("chance").getAsFloat() : 1.0F;
            list.add(new GenericMachineRecipe.ItemInput(ore, id, count, meta, chance));
        }
        return list;
    }

    private static List<GenericMachineRecipe.FluidInput> readFluids(JsonObject obj, String key) {
        List<GenericMachineRecipe.FluidInput> list = new ArrayList<>();
        if (!obj.has(key)) {
            return list;
        }
        for (JsonElement el : obj.getAsJsonArray(key)) {
            JsonObject fluid = el.getAsJsonObject();
            if (fluid.has("raw") && !fluid.has("fluid")) {
                continue;
            }
            if (!fluid.has("fluid") || fluid.get("fluid").isJsonNull()) {
                continue;
            }
            int amount = fluid.has("amount") && !fluid.get("amount").isJsonNull()
                    ? fluid.get("amount").getAsInt() : 0;
            list.add(new GenericMachineRecipe.FluidInput(fluid.get("fluid").getAsString(), amount));
        }
        return list;
    }
}
