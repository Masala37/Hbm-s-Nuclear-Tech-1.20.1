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
 * 1.7.10 {@code BlastFurnaceRecipesNT}. Extracted from legacy Java. Slag byproducts with
 * unported {@code ingot_raw} meta are dropped at extract time, not replaced.
 */
public final class BlastFurnaceRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/blast_furnace.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/blast_furnace.json");

    public record BlastFurnaceRecipe(String name, int duration, List<IngredientRef> inputs, List<IngredientRef> outputs) {
        public boolean existsInRegistry() {
            for (IngredientRef in : inputs) {
                if (!in.existsInRegistry()) {
                    return false;
                }
            }
            List<IngredientRef> present = presentOutputs();
            return !inputs.isEmpty() && !present.isEmpty();
        }

        public List<IngredientRef> presentOutputs() {
            List<IngredientRef> present = new ArrayList<>();
            for (IngredientRef out : outputs) {
                if (out.existsInRegistry()) {
                    present.add(out);
                }
            }
            return present;
        }

        public boolean matches(ItemStack a, ItemStack b) {
            if (inputs.size() == 1) {
                IngredientRef only = inputs.get(0);
                if (!a.isEmpty() && b.isEmpty()) {
                    return only.matches(a, false);
                }
                if (a.isEmpty() && !b.isEmpty()) {
                    return only.matches(b, false);
                }
                return false;
            }
            if (inputs.size() < 2 || a.isEmpty() || b.isEmpty()) {
                return false;
            }
            return (inputs.get(0).matches(a, true) && inputs.get(1).matches(b, false))
                    || (inputs.get(1).matches(a, true) && inputs.get(0).matches(b, false));
        }
    }

    private static List<BlastFurnaceRecipe> recipes = List.of();

    private BlastFurnaceRecipes() {
    }

    public static List<BlastFurnaceRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<BlastFurnaceRecipe> loaded = new ArrayList<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            BlastFurnaceRecipe recipe = new BlastFurnaceRecipe(
                    obj.get("name").getAsString(),
                    obj.has("duration") ? obj.get("duration").getAsInt() : 400,
                    IngredientRef.list(obj.getAsJsonArray("inputItem")),
                    IngredientRef.list(obj.getAsJsonArray("outputItem")));
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
            }
        }
        recipes = Collections.unmodifiableList(loaded);
    }

    public static BlastFurnaceRecipe getRecipe(ItemStack a, ItemStack b) {
        for (BlastFurnaceRecipe recipe : recipes) {
            if (recipe.matches(a, b)) {
                return recipe;
            }
        }
        return null;
    }
}
