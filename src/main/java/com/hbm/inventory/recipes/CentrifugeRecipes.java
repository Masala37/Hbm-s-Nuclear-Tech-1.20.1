package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 1.7.10 {@code CentrifugeRecipes}. Source JSON is extracted from legacy Java — not invented.
 * Bedrock-ore loops, enum/meta stacks, IMC, and AE2 certus rows are skipped. Missing items drop at load.
 */
public final class CentrifugeRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/centrifuge.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/centrifuge.json");

    public record CentrifugeRecipe(IngredientRef input, List<IngredientRef> output) {
        public boolean existsInRegistry() {
            if (!input.existsInRegistry()) {
                return false;
            }
            for (IngredientRef out : output) {
                if (!out.existsInRegistry()) {
                    return false;
                }
            }
            return !output.isEmpty();
        }
    }

    private static List<CentrifugeRecipe> recipes = List.of();

    private CentrifugeRecipes() {
    }

    public static List<CentrifugeRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<CentrifugeRecipe> loaded = new ArrayList<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            JsonArray outArr = obj.getAsJsonArray("output");
            List<IngredientRef> outs = IngredientRef.list(outArr);
            CentrifugeRecipe recipe = new CentrifugeRecipe(IngredientRef.fromJson(obj.getAsJsonObject("input")), outs);
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
            }
        }
        recipes = Collections.unmodifiableList(loaded);
    }

    public static ItemStack[] getOutput(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        for (CentrifugeRecipe recipe : recipes) {
            if (recipe.input().matches(stack, true)) {
                ItemStack[] out = new ItemStack[recipe.output().size()];
                for (int i = 0; i < out.length; i++) {
                    out[i] = recipe.output().get(i).resultStack();
                }
                return out;
            }
        }
        return null;
    }
}
