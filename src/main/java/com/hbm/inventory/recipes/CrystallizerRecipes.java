package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 1.7.10 {@code CrystallizerRecipes}. Source JSON is copied from legacy {@code registerDefaults}.
 * Bedrock-ore loops, scrap/dye/tar enums, and missing fluids drop at load.
 */
public final class CrystallizerRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/crystallizer.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/crystallizer.json");

    public record FluidRef(String fluid, int amount) {
        public boolean existsInRegistry() {
            return GenericRecipeMatch.fluid(fluid) != null && amount > 0;
        }

        public Fluid stackFluid() {
            return GenericRecipeMatch.fluid(fluid);
        }
    }

    public record CrystallizerRecipe(IngredientRef input, FluidRef fluid, IngredientRef output, int duration,
                                     float productivity) {
        public int itemAmount() {
            return input.count();
        }

        public int acidAmount() {
            return fluid.amount();
        }

        public boolean existsInRegistry() {
            return input.existsInRegistry() && output.existsInRegistry() && fluid.existsInRegistry();
        }
    }

    private static List<CrystallizerRecipe> recipes = List.of();
    private static List<Fluid> recipeFluids = List.of();

    private CrystallizerRecipes() {
    }

    public static List<CrystallizerRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<CrystallizerRecipe> loaded = new ArrayList<>();
        Set<Fluid> fluids = new LinkedHashSet<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            JsonObject fluidObj = obj.getAsJsonObject("fluid");
            FluidRef fluid = new FluidRef(fluidObj.get("fluid").getAsString(), fluidObj.get("amount").getAsInt());
            CrystallizerRecipe recipe = new CrystallizerRecipe(
                    IngredientRef.fromJson(obj.getAsJsonObject("input")),
                    fluid,
                    IngredientRef.fromJson(obj.getAsJsonObject("output")),
                    obj.get("duration").getAsInt(),
                    obj.has("productivity") ? obj.get("productivity").getAsFloat() : 0.0F);
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
                if (filterMissing) {
                    Fluid stackFluid = recipe.fluid().stackFluid();
                    if (stackFluid != null) {
                        fluids.add(stackFluid);
                    }
                }
            }
        }
        recipes = Collections.unmodifiableList(loaded);
        recipeFluids = List.copyOf(fluids);
    }

    public static @Nullable CrystallizerRecipe getOutput(ItemStack stack, Fluid type) {
        if (stack == null || stack.isEmpty() || type == null) {
            return null;
        }
        for (CrystallizerRecipe recipe : recipes) {
            Fluid need = recipe.fluid().stackFluid();
            if (need == type && recipe.input().matches(stack, true)) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean isRecipeFluid(Fluid fluid) {
        return fluid != null && recipeFluids.contains(fluid);
    }

    public static @Nullable Fluid cycleNext(@Nullable Fluid current) {
        if (recipeFluids.isEmpty()) {
            return current;
        }
        int index = 0;
        for (int i = 0; i < recipeFluids.size(); i++) {
            if (recipeFluids.get(i) == current) {
                index = (i + 1) % recipeFluids.size();
                break;
            }
        }
        return recipeFluids.get(index);
    }

    public static boolean matchesFluid(CrystallizerRecipe recipe, FluidStack stored) {
        Fluid need = recipe.fluid().stackFluid();
        if (need == null || stored == null || stored.isEmpty()) {
            return false;
        }
        return stored.getFluid() == need && stored.getAmount() >= recipe.acidAmount();
    }
}
