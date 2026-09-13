package com.hbm.inventory.recipes.anvil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.inventory.recipes.IngredientRef;
import com.hbm.inventory.recipes.RecipeJson;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 1.7.10 {@code AnvilRecipes}. Source JSON is extracted from legacy Java — not invented.
 */
public final class AnvilRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/anvil.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/anvil.json");

    private static List<AnvilSmithingRecipe> smithing = List.of();
    private static List<AnvilConstructionRecipe> construction = List.of();

    private AnvilRecipes() {
    }

    public static List<AnvilSmithingRecipe> getSmithing() {
        return smithing;
    }

    public static List<AnvilConstructionRecipe> getConstruction() {
        return construction;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<AnvilSmithingRecipe> smith = new ArrayList<>();
        if (root.has("smithing")) {
            for (JsonElement el : root.getAsJsonArray("smithing")) {
                JsonObject obj = el.getAsJsonObject();
                AnvilSmithingRecipe recipe = new AnvilSmithingRecipe(
                        obj.get("tier").getAsInt(),
                        IngredientRef.fromJson(obj.getAsJsonObject("left")),
                        IngredientRef.fromJson(obj.getAsJsonObject("right")),
                        IngredientRef.fromJson(obj.getAsJsonObject("output")));
                if (!filterMissing || recipe.existsInRegistry()) {
                    smith.add(recipe);
                }
            }
        }
        List<AnvilConstructionRecipe> build = new ArrayList<>();
        JsonArray array = root.getAsJsonArray("recipes");
        if (array != null) {
            for (JsonElement el : array) {
                JsonObject obj = el.getAsJsonObject();
                AnvilConstructionRecipe recipe = AnvilConstructionRecipe.fromJson(obj);
                if (!filterMissing || recipe.existsInRegistry()) {
                    build.add(recipe);
                }
            }
        }
        smithing = Collections.unmodifiableList(smith);
        construction = Collections.unmodifiableList(build);
    }

    public static final class AnvilSmithingRecipe {
        public final int tier;
        public final IngredientRef left;
        public final IngredientRef right;
        public final IngredientRef output;

        public AnvilSmithingRecipe(int tier, IngredientRef left, IngredientRef right, IngredientRef output) {
            this.tier = tier;
            this.left = left;
            this.right = right;
            this.output = output;
        }

        public boolean matches(ItemStack leftStack, ItemStack rightStack) {
            return matchesInt(leftStack, rightStack) != -1;
        }

        public int matchesInt(ItemStack leftStack, ItemStack rightStack) {
            if (left.matches(leftStack, false) && right.matches(rightStack, false)) {
                return 0;
            }
            return -1;
        }

        public int amountConsumed(int index, boolean mirrored) {
            if (index == 0) {
                return mirrored ? right.count() : left.count();
            }
            if (index == 1) {
                return mirrored ? left.count() : right.count();
            }
            return 0;
        }

        public ItemStack getOutput() {
            return output.resultStack();
        }

        public boolean existsInRegistry() {
            return left.existsInRegistry() && right.existsInRegistry() && output.existsInRegistry();
        }
    }

    public static final class AnvilConstructionRecipe {
        public final List<IngredientRef> input;
        public final List<IngredientRef> output;
        public final int tierLower;
        public final int tierUpper;
        public final OverlayType overlay;

        public AnvilConstructionRecipe(List<IngredientRef> input, List<IngredientRef> output,
                                       int tierLower, int tierUpper, OverlayType overlay) {
            this.input = List.copyOf(input);
            this.output = List.copyOf(output);
            this.tierLower = tierLower;
            this.tierUpper = tierUpper;
            this.overlay = overlay;
        }

        public static AnvilConstructionRecipe fromJson(JsonObject obj) {
            List<IngredientRef> inputs = IngredientRef.list(obj.getAsJsonArray("inputs"));
            List<IngredientRef> outputs = IngredientRef.list(obj.getAsJsonArray("outputs"));
            OverlayType overlay = OverlayType.NONE;
            if (obj.has("overlay") && !obj.get("overlay").isJsonNull()) {
                try {
                    overlay = OverlayType.valueOf(obj.get("overlay").getAsString().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                    overlay = OverlayType.NONE;
                }
            } else if (inputs.size() == 1 && outputs.size() == 1) {
                overlay = OverlayType.SMITHING;
            } else if (inputs.size() > 1 && outputs.size() == 1) {
                overlay = OverlayType.CONSTRUCTION;
            } else if (inputs.size() == 1) {
                overlay = OverlayType.RECYCLING;
            }
            return new AnvilConstructionRecipe(
                    inputs,
                    outputs,
                    obj.has("tierLower") ? obj.get("tierLower").getAsInt() : 1,
                    obj.has("tierUpper") ? obj.get("tierUpper").getAsInt() : -1,
                    overlay);
        }

        public boolean isTierValid(int tier) {
            if (tierUpper == -1) {
                return tier >= tierLower;
            }
            return tier >= tierLower && tier <= tierUpper;
        }

        public OverlayType getOverlay() {
            return overlay;
        }

        public ItemStack getDisplay() {
            if (overlay == OverlayType.RECYCLING && !input.isEmpty()) {
                ItemStack in = input.get(0).resultStack();
                if (!in.isEmpty()) {
                    return in;
                }
            }
            return output.isEmpty() ? ItemStack.EMPTY : output.get(0).resultStack();
        }

        public boolean existsInRegistry() {
            for (IngredientRef in : input) {
                if (!in.existsInRegistry()) {
                    return false;
                }
            }
            for (IngredientRef out : output) {
                if (!out.existsInRegistry()) {
                    return false;
                }
            }
            return !input.isEmpty() && !output.isEmpty();
        }
    }

    public enum OverlayType {
        NONE,
        CONSTRUCTION,
        RECYCLING,
        SMITHING
    }
}
