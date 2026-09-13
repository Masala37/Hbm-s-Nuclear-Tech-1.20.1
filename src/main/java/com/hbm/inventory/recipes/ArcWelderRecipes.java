package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 1.7.10 {@code ArcWelderRecipes}. Source JSON is copied from legacy {@code registerDefaults}.
 * Missing items and fluids drop at load. Mats plate/wire siblings are explicit ids so they skip
 * instead of collapsing onto generic {@code plate_welded} / {@code wire_dense}.
 */
public final class ArcWelderRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/arc_welder.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/arc_welder.json");

    public record FluidRef(String fluid, int amount) {
        public boolean existsInRegistry() {
            return GenericRecipeMatch.fluid(fluid) != null && amount > 0;
        }

        public Fluid stackFluid() {
            return GenericRecipeMatch.fluid(fluid);
        }
    }

    public record ArcWelderRecipe(List<IngredientRef> inputs, @Nullable FluidRef fluid, IngredientRef output,
                                  int duration, int consumption) {
        public boolean existsInRegistry() {
            if (!solidItemExists(output)) {
                return false;
            }
            for (IngredientRef in : inputs) {
                if (!solidItemExists(in)) {
                    return false;
                }
            }
            return fluid == null || fluid.existsInRegistry();
        }
    }

    private static List<ArcWelderRecipe> recipes = List.of();

    private ArcWelderRecipes() {
    }

    public static List<ArcWelderRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<ArcWelderRecipe> loaded = new ArrayList<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            FluidRef fluid = null;
            if (obj.has("fluid") && obj.get("fluid").isJsonObject()) {
                JsonObject f = obj.getAsJsonObject("fluid");
                fluid = new FluidRef(f.get("fluid").getAsString(), f.get("amount").getAsInt());
            }
            ArcWelderRecipe recipe = new ArcWelderRecipe(
                    IngredientRef.list(obj.getAsJsonArray("inputs")),
                    fluid,
                    IngredientRef.fromJson(obj.getAsJsonObject("output")),
                    obj.get("duration").getAsInt(),
                    obj.get("consumption").getAsInt());
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
            }
        }
        recipes = Collections.unmodifiableList(loaded);
    }

    public static @Nullable ArcWelderRecipe getRecipe(ItemStack... inputs) {
        if (inputs == null) {
            return null;
        }
        for (ArcWelderRecipe recipe : recipes) {
            if (matchesIngredients(inputs, recipe.inputs())) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean isIngredient(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (ArcWelderRecipe recipe : recipes) {
            for (IngredientRef in : recipe.inputs()) {
                if (in.matches(stack, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matchesFluid(ArcWelderRecipe recipe, FluidStack stored) {
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

    /**
     * Tag-only ore names must not count as present — empty Forge tags would keep rows that
     * cannot actually consume a solid.
     */
    static boolean solidItemExists(IngredientRef ref) {
        if (!ref.anyOf().isEmpty()) {
            for (IngredientRef option : ref.anyOf()) {
                if (solidItemExists(option)) {
                    return true;
                }
            }
            return false;
        }
        if (ref.item() != null && !ref.item().isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(ref.item());
            return id != null && ForgeRegistries.ITEMS.containsKey(id);
        }
        if (ref.ore() == null || ref.ore().isEmpty()) {
            return false;
        }
        for (ResourceLocation id : OreDictMatch.itemIds(ref.ore())) {
            if (ForgeRegistries.ITEMS.containsKey(id)) {
                return true;
            }
        }
        return false;
    }
}
