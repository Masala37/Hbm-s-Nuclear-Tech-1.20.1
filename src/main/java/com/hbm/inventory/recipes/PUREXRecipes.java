package com.hbm.inventory.recipes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.7.10 {@code PUREXRecipes} defaults. Source file
 * {@code data/hbm/machine_recipes/purex.json} is extracted from legacy Java — not invented.
 */
public final class PUREXRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/purex.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/purex.json");

    private static List<GenericMachineRecipe> recipes = List.of();
    private static Map<String, GenericMachineRecipe> byName = Map.of();

    private PUREXRecipes() {
    }

    public static List<GenericMachineRecipe> recipes() {
        return recipes;
    }

    public static GenericMachineRecipe byName(String name) {
        if (name == null || name.isEmpty() || "null".equals(name)) {
            return null;
        }
        return byName.get(name);
    }

    public static boolean isPooled(GenericMachineRecipe recipe) {
        return recipe != null && !recipe.pools().isEmpty();
    }

    public static boolean isPartOfPool(GenericMachineRecipe recipe, String pool) {
        if (!isPooled(recipe) || pool == null) {
            return false;
        }
        return recipe.pools().contains(pool);
    }

    /** 1.7 selector: unpooled recipes, plus pooled ones that match the installed blueprint. */
    public static List<GenericMachineRecipe> visible(String installedPool) {
        List<GenericMachineRecipe> visible = new ArrayList<>();
        for (GenericMachineRecipe recipe : recipes) {
            if (!isPooled(recipe) || isPartOfPool(recipe, installedPool)) {
                visible.add(recipe);
            }
        }
        return visible;
    }

    public static List<GenericMachineRecipe> visible(String installedPool, String query) {
        if (query == null || query.isBlank()) {
            return visible(installedPool);
        }
        List<GenericMachineRecipe> visible = new ArrayList<>();
        for (GenericMachineRecipe recipe : recipes) {
            if ((!isPooled(recipe) || isPartOfPool(recipe, installedPool)) && recipe.matchesSearch(query)) {
                visible.add(recipe);
            }
        }
        return visible;
    }

    public static void load(List<GenericMachineRecipe> loaded) {
        recipes = Collections.unmodifiableList(loaded);
        Map<String, GenericMachineRecipe> map = new LinkedHashMap<>();
        for (GenericMachineRecipe recipe : loaded) {
            map.put(recipe.name(), recipe);
        }
        byName = Collections.unmodifiableMap(map);
    }

    public static void loadFromSourceTree() throws IOException {
        apply(readAll(), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        apply(readAll(), true);
    }

    private static void apply(List<GenericMachineRecipe> loaded, boolean filterMissing) {
        if (!filterMissing) {
            load(loaded);
            return;
        }
        List<GenericMachineRecipe> kept = new ArrayList<>();
        for (GenericMachineRecipe recipe : loaded) {
            if (GenericRecipeMatch.exists(recipe)) {
                kept.add(recipe);
            }
        }
        load(kept);
    }

    private static List<GenericMachineRecipe> readAll() throws IOException {
        if (Files.exists(SOURCE_PATH)) {
            return GenericRecipeLoader.read(SOURCE_PATH);
        }
        return GenericRecipeLoader.readResource(RESOURCE);
    }
}
