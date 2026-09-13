package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 1.7.10 {@code BlastFurnaceRecipes} (alloy furnace / di-furnace). Extracted from legacy Java.
 */
public final class DiFurnaceRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/di_furnace.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/di_furnace.json");

    public record DiFurnaceRecipe(IngredientRef inputA, IngredientRef inputB, IngredientRef output) {
        public boolean existsInRegistry() {
            return inputA.existsInRegistry() && inputB.existsInRegistry() && output.existsInRegistry();
        }
    }

    private static List<DiFurnaceRecipe> recipes = List.of();

    private DiFurnaceRecipes() {
    }

    public static List<DiFurnaceRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<DiFurnaceRecipe> loaded = new ArrayList<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            DiFurnaceRecipe recipe = new DiFurnaceRecipe(
                    IngredientRef.fromJson(obj.getAsJsonObject("inputA")),
                    IngredientRef.fromJson(obj.getAsJsonObject("inputB")),
                    IngredientRef.fromJson(obj.getAsJsonObject("output")));
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
            }
        }
        recipes = Collections.unmodifiableList(loaded);
    }

    public static ItemStack getOutput(ItemStack a, ItemStack b) {
        if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (DiFurnaceRecipe recipe : recipes) {
            if ((recipe.inputA().matches(a, false) && recipe.inputB().matches(b, false))
                    || (recipe.inputA().matches(b, false) && recipe.inputB().matches(a, false))) {
                return recipe.output().resultStack();
            }
        }
        return ItemStack.EMPTY;
    }
}
