package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 1.7.10 {@code MixerRecipes}. Source JSON is copied from legacy {@code registerDefaults}.
 * Rows whose fluids or solid items are missing drop at load.
 */
public final class MixerRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/mixer.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/mixer.json");

    public record FluidRef(String fluid, int amount) {
        public boolean existsInRegistry() {
            return GenericRecipeMatch.fluid(fluid) != null && amount > 0;
        }

        public Fluid stackFluid() {
            return GenericRecipeMatch.fluid(fluid);
        }
    }

    public record MixerRecipe(FluidRef output, int duration, @Nullable FluidRef input1, @Nullable FluidRef input2,
                              @Nullable IngredientRef solid) {
        public boolean existsInRegistry() {
            if (!output.existsInRegistry()) {
                return false;
            }
            if (input1 != null && !input1.existsInRegistry()) {
                return false;
            }
            if (input2 != null && !input2.existsInRegistry()) {
                return false;
            }
            return solid == null || solidItemExists(solid);
        }
    }

    private static List<MixerRecipe> recipes = List.of();
    private static List<Fluid> recipeOutputs = List.of();

    private MixerRecipes() {
    }

    public static List<MixerRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<MixerRecipe> loaded = new ArrayList<>();
        Set<Fluid> outputs = new LinkedHashSet<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            MixerRecipe recipe = new MixerRecipe(
                    fluidRef(obj.getAsJsonObject("output")),
                    obj.get("duration").getAsInt(),
                    optionalFluid(obj, "input1"),
                    optionalFluid(obj, "input2"),
                    obj.has("solid") && obj.get("solid").isJsonObject()
                            ? IngredientRef.fromJson(obj.getAsJsonObject("solid"))
                            : null);
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
                if (filterMissing) {
                    Fluid out = recipe.output().stackFluid();
                    if (out != null) {
                        outputs.add(out);
                    }
                }
            }
        }
        recipes = Collections.unmodifiableList(loaded);
        recipeOutputs = List.copyOf(outputs);
    }

    public static MixerRecipe[] getOutput(@Nullable Fluid type) {
        if (type == null) {
            return null;
        }
        List<MixerRecipe> matches = new ArrayList<>();
        for (MixerRecipe recipe : recipes) {
            if (recipe.output().stackFluid() == type) {
                matches.add(recipe);
            }
        }
        return matches.isEmpty() ? null : matches.toArray(MixerRecipe[]::new);
    }

    public static boolean isRecipeOutput(@Nullable Fluid fluid) {
        return fluid != null && recipeOutputs.contains(fluid);
    }

    public static @Nullable Fluid cycleNext(@Nullable Fluid current) {
        if (recipeOutputs.isEmpty()) {
            return current;
        }
        int index = 0;
        for (int i = 0; i < recipeOutputs.size(); i++) {
            if (recipeOutputs.get(i) == current) {
                index = (i + 1) % recipeOutputs.size();
                break;
            }
        }
        return recipeOutputs.get(index);
    }

    public static @Nullable Fluid firstOutput() {
        return recipeOutputs.isEmpty() ? null : recipeOutputs.get(0);
    }

    private static FluidRef fluidRef(JsonObject obj) {
        return new FluidRef(obj.get("fluid").getAsString(), obj.get("amount").getAsInt());
    }

    private static @Nullable FluidRef optionalFluid(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonObject()) {
            return null;
        }
        return fluidRef(obj.getAsJsonObject(key));
    }

    /**
     * Tag-only ore names must not count as present — empty {@code forge:dusts/niter} would keep
     * coolant/nitric rows that cannot actually consume a solid.
     */
    static boolean solidItemExists(IngredientRef ref) {
        if (ref.item() != null && !ref.item().isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(ref.item());
            return id != null && net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(id);
        }
        if (ref.ore() == null || ref.ore().isEmpty()) {
            return false;
        }
        for (ResourceLocation id : OreDictMatch.itemIds(ref.ore())) {
            if (net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(id)) {
                return true;
            }
        }
        return false;
    }
}
