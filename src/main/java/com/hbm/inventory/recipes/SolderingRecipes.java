package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 1.7.10 {@code SolderingRecipes}. Source JSON is copied from legacy {@code registerDefaults}
 * (LBSM off, 528 off). Missing items and fluids drop at load.
 */
public final class SolderingRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/soldering.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/soldering.json");

    public record FluidRef(String fluid, int amount) {
        public boolean existsInRegistry() {
            return GenericRecipeMatch.fluid(fluid) != null && amount > 0;
        }

        public Fluid stackFluid() {
            return GenericRecipeMatch.fluid(fluid);
        }
    }

    public record SolderingRecipe(List<IngredientRef> toppings, List<IngredientRef> pcb, List<IngredientRef> solder,
                                  FluidRef fluid, IngredientRef output, int duration, int consumption) {
        public boolean existsInRegistry() {
            if (!output.existsInRegistry()) {
                return false;
            }
            for (IngredientRef in : toppings) {
                if (!in.existsInRegistry()) {
                    return false;
                }
            }
            for (IngredientRef in : pcb) {
                if (!in.existsInRegistry()) {
                    return false;
                }
            }
            for (IngredientRef in : solder) {
                if (!in.existsInRegistry()) {
                    return false;
                }
            }
            return fluid == null || fluid.existsInRegistry();
        }
    }

    private static List<SolderingRecipe> recipes = List.of();
    private static Set<IngredientRef> toppings = Set.of();
    private static Set<IngredientRef> pcb = Set.of();
    private static Set<IngredientRef> solder = Set.of();

    private SolderingRecipes() {
    }

    public static List<SolderingRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<SolderingRecipe> loaded = new ArrayList<>();
        Set<IngredientRef> top = new HashSet<>();
        Set<IngredientRef> boards = new HashSet<>();
        Set<IngredientRef> wires = new HashSet<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            FluidRef fluid = null;
            if (obj.has("fluid") && obj.get("fluid").isJsonObject()) {
                JsonObject f = obj.getAsJsonObject("fluid");
                fluid = new FluidRef(f.get("fluid").getAsString(), f.get("amount").getAsInt());
            }
            SolderingRecipe recipe = new SolderingRecipe(
                    IngredientRef.list(obj.getAsJsonArray("toppings")),
                    IngredientRef.list(obj.getAsJsonArray("pcb")),
                    IngredientRef.list(obj.getAsJsonArray("solder")),
                    fluid,
                    IngredientRef.fromJson(obj.getAsJsonObject("output")),
                    obj.get("duration").getAsInt(),
                    obj.get("consumption").getAsInt());
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
                top.addAll(recipe.toppings());
                boards.addAll(recipe.pcb());
                wires.addAll(recipe.solder());
            }
        }
        recipes = Collections.unmodifiableList(loaded);
        toppings = Set.copyOf(top);
        pcb = Set.copyOf(boards);
        solder = Set.copyOf(wires);
    }

    public static SolderingRecipe getRecipe(ItemStack[] inputs) {
        if (inputs == null || inputs.length < 6) {
            return null;
        }
        for (SolderingRecipe recipe : recipes) {
            if (matchesIngredients(new ItemStack[]{inputs[0], inputs[1], inputs[2]}, recipe.toppings())
                    && matchesIngredients(new ItemStack[]{inputs[3], inputs[4]}, recipe.pcb())
                    && matchesIngredients(new ItemStack[]{inputs[5]}, recipe.solder())) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean isTopping(ItemStack stack) {
        return matchesAny(stack, toppings);
    }

    public static boolean isPcb(ItemStack stack) {
        return matchesAny(stack, pcb);
    }

    public static boolean isSolder(ItemStack stack) {
        return matchesAny(stack, solder);
    }

    private static boolean matchesAny(ItemStack stack, Set<IngredientRef> set) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (IngredientRef ref : set) {
            if (ref.matches(stack, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesFluid(SolderingRecipe recipe, FluidStack stored) {
        if (recipe.fluid() == null) {
            return true;
        }
        Fluid need = recipe.fluid().stackFluid();
        if (need == null || stored == null || stored.isEmpty()) {
            return false;
        }
        return stored.getFluid() == need && stored.getAmount() >= recipe.fluid().amount();
    }

    static boolean matchesIngredients(ItemStack[] inputs, List<IngredientRef> recipe) {
        List<IngredientRef> remaining = new ArrayList<>(recipe);
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                continue;
            }
            boolean hasMatch = false;
            Iterator<IngredientRef> iterator = remaining.iterator();
            while (iterator.hasNext()) {
                IngredientRef need = iterator.next();
                if (need.matches(input, false)) {
                    hasMatch = true;
                    iterator.remove();
                    break;
                }
            }
            if (!hasMatch) {
                return false;
            }
        }
        return remaining.isEmpty();
    }
}
