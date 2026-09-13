package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 1.7.10 {@code PressRecipes}. Source JSON is extracted from legacy Java — not invented.
 */
public final class PressRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/press.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/press.json");

    public record PressRecipe(StampType stamp, IngredientRef input, IngredientRef output) {
        public boolean existsInRegistry() {
            return input.existsInRegistry() && output.existsInRegistry();
        }
    }

    private static List<PressRecipe> recipes = List.of();

    private PressRecipes() {
    }

    public static List<PressRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<PressRecipe> loaded = new ArrayList<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            StampType stamp;
            try {
                stamp = StampType.valueOf(obj.get("stamp").getAsString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                continue;
            }
            PressRecipe recipe = new PressRecipe(
                    stamp,
                    IngredientRef.fromJson(obj.getAsJsonObject("input")),
                    IngredientRef.fromJson(obj.getAsJsonObject("output")));
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
            }
        }
        recipes = Collections.unmodifiableList(loaded);
    }

    public static ItemStack getOutput(ItemStack ingredient, StampType type) {
        if (ingredient == null || ingredient.isEmpty() || type == null) {
            return ItemStack.EMPTY;
        }
        for (PressRecipe recipe : recipes) {
            if (recipe.stamp() == type && recipe.input().matches(ingredient, true)) {
                return recipe.output().resultStack();
            }
        }
        return ItemStack.EMPTY;
    }
}
