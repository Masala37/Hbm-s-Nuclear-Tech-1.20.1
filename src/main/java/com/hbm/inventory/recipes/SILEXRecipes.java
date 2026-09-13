package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.7.10 {@code SILEXRecipes}. Source JSON is copied from legacy {@code register()}.
 * RBMK pellet loops, waste-class meta, and fullerene enum ash are skipped (same as centrifuge
 * enum/meta). Missing items or fluids drop at load.
 */
public final class SILEXRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/silex.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/silex.json");

    public record WeightedOut(IngredientRef item, int weight) {
        public boolean existsInRegistry() {
            return item.existsInRegistry() && weight > 0;
        }
    }

    public record SILEXRecipe(IngredientRef input, @Nullable String fluid, int fluidProduced, int fluidConsumed,
                              EnumWavelengths laser, List<WeightedOut> outputs) {
        public boolean existsInRegistry() {
            if (fluid != null && !fluid.isEmpty() && GenericRecipeMatch.fluid(fluid) == null) {
                return false;
            }
            if ((fluid == null || fluid.isEmpty()) && !input.existsInRegistry()) {
                return false;
            }
            if (outputs.isEmpty()) {
                return false;
            }
            for (WeightedOut out : outputs) {
                if (!out.existsInRegistry()) {
                    return false;
                }
            }
            return true;
        }

        public int totalWeight() {
            int sum = 0;
            for (WeightedOut out : outputs) {
                sum += out.weight();
            }
            return Math.max(1, sum);
        }
    }

    private static List<SILEXRecipe> recipes = List.of();
    private static List<IngredientRef> itemTranslationsFrom = List.of();
    private static List<IngredientRef> itemTranslationsTo = List.of();
    private static List<Fluid> conversionFluids = List.of();
    private static Map<Fluid, IngredientRef> fluidTranslations = Map.of();

    private SILEXRecipes() {
    }

    public static List<SILEXRecipe> recipes() {
        return recipes;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<SILEXRecipe> loaded = new ArrayList<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            JsonObject inputObj = obj.getAsJsonObject("input");
            String fluid = inputObj.has("fluid") && !inputObj.get("fluid").isJsonNull()
                    ? inputObj.get("fluid").getAsString() : null;
            List<WeightedOut> outs = new ArrayList<>();
            for (JsonElement outEl : obj.getAsJsonArray("outputs")) {
                JsonObject out = outEl.getAsJsonObject();
                int weight = out.has("weight") ? out.get("weight").getAsInt() : 1;
                outs.add(new WeightedOut(IngredientRef.fromJson(out), weight));
            }
            SILEXRecipe recipe = new SILEXRecipe(
                    IngredientRef.fromJson(inputObj),
                    fluid,
                    obj.get("fluidProduced").getAsInt(),
                    obj.get("fluidConsumed").getAsInt(),
                    EnumWavelengths.byName(obj.get("laser").getAsString()),
                    List.copyOf(outs));
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
            }
        }
        recipes = Collections.unmodifiableList(loaded);

        List<IngredientRef> from = new ArrayList<>();
        List<IngredientRef> to = new ArrayList<>();
        if (root.has("translations")) {
            for (JsonElement el : root.getAsJsonArray("translations")) {
                JsonObject obj = el.getAsJsonObject();
                if (obj.has("fromFluid")) {
                    continue;
                }
                if (obj.has("fromOre")) {
                    from.add(new IngredientRef(obj.get("fromOre").getAsString(), null, 1, 1.0F, List.of()));
                    to.add(new IngredientRef(obj.get("toOre").getAsString(), null, 1, 1.0F, List.of()));
                    continue;
                }
                if (obj.has("from") && obj.has("to")) {
                    from.add(IngredientRef.fromJson(obj.getAsJsonObject("from")));
                    to.add(IngredientRef.fromJson(obj.getAsJsonObject("to")));
                }
            }
        }
        itemTranslationsFrom = List.copyOf(from);
        itemTranslationsTo = List.copyOf(to);

        List<Fluid> conv = new ArrayList<>();
        Map<Fluid, IngredientRef> fluids = new LinkedHashMap<>();
        if (filterMissing) {
            if (root.has("conversionFluids")) {
                for (JsonElement el : root.getAsJsonArray("conversionFluids")) {
                    Fluid fluid = GenericRecipeMatch.fluid(el.getAsString());
                    if (fluid != null) {
                        conv.add(fluid);
                    }
                }
            }
            if (root.has("translations")) {
                for (JsonElement el : root.getAsJsonArray("translations")) {
                    JsonObject obj = el.getAsJsonObject();
                    if (!obj.has("fromFluid")) {
                        continue;
                    }
                    Fluid fluid = GenericRecipeMatch.fluid(obj.get("fromFluid").getAsString());
                    if (fluid == null) {
                        continue;
                    }
                    if (!conv.contains(fluid)) {
                        conv.add(fluid);
                    }
                    if (obj.has("to")) {
                        fluids.put(fluid, IngredientRef.fromJson(obj.getAsJsonObject("to")));
                    }
                }
            }
        }
        conversionFluids = List.copyOf(conv);
        fluidTranslations = Collections.unmodifiableMap(fluids);
    }

    public static ItemStack translateItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < itemTranslationsFrom.size(); i++) {
            if (itemTranslationsFrom.get(i).matches(stack, true)) {
                return itemTranslationsTo.get(i).resultStack();
            }
        }
        return stack.copyWithCount(1);
    }

    @Nullable
    public static IngredientRef translateFluid(Fluid fluid) {
        return fluid == null ? null : fluidTranslations.get(fluid);
    }

    public static boolean isConversionFluid(Fluid fluid) {
        return fluid != null && conversionFluids.contains(fluid);
    }

    public static boolean isTankFluid(Fluid fluid) {
        if (fluid == null) {
            return false;
        }
        if (isConversionFluid(fluid)) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null) {
            return false;
        }
        String want = id.toString();
        for (SILEXRecipe recipe : recipes) {
            if (want.equals(recipe.fluid())) {
                return true;
            }
        }
        return "hbm:peroxide".equals(want);
    }

    public static Fluid cycleNext(@Nullable Fluid current) {
        List<Fluid> types = tankFluids();
        if (types.isEmpty()) {
            return current;
        }
        if (current == null) {
            return types.get(0);
        }
        int idx = types.indexOf(current);
        return types.get((idx + 1) % types.size());
    }

    public static List<Fluid> tankFluids() {
        Map<ResourceLocation, Fluid> unique = new LinkedHashMap<>();
        Fluid peroxide = GenericRecipeMatch.fluid("hbm:peroxide");
        if (peroxide != null) {
            unique.put(ForgeRegistries.FLUIDS.getKey(peroxide), peroxide);
        }
        for (Fluid fluid : conversionFluids) {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
            if (id != null) {
                unique.put(id, fluid);
            }
        }
        for (SILEXRecipe recipe : recipes) {
            if (recipe.fluid() == null || recipe.fluid().isEmpty()) {
                continue;
            }
            Fluid fluid = GenericRecipeMatch.fluid(recipe.fluid());
            if (fluid != null) {
                ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
                if (id != null) {
                    unique.put(id, fluid);
                }
            }
        }
        return List.copyOf(unique.values());
    }

    @Nullable
    public static SILEXRecipe getOutput(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ItemStack translated = translateItem(stack);
        for (SILEXRecipe recipe : recipes) {
            if (recipe.fluid() != null && !recipe.fluid().isEmpty()) {
                continue;
            }
            if (recipe.input().matches(translated, true) || recipe.input().matches(stack, true)) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public static SILEXRecipe getOutput(Fluid fluid) {
        if (fluid == null) {
            return null;
        }
        IngredientRef translated = translateFluid(fluid);
        if (translated != null) {
            ItemStack stack = translated.resultStack();
            if (!stack.isEmpty()) {
                SILEXRecipe item = getOutput(stack);
                if (item != null) {
                    return item;
                }
            }
        }
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null) {
            return null;
        }
        String want = id.toString();
        for (SILEXRecipe recipe : recipes) {
            if (want.equals(recipe.fluid())) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public static SILEXRecipe getOutput(String currentKey) {
        if (currentKey == null || currentKey.isEmpty()) {
            return null;
        }
        if (currentKey.startsWith("fluid:")) {
            Fluid fluid = GenericRecipeMatch.fluid(currentKey.substring("fluid:".length()));
            return getOutput(fluid);
        }
        ResourceLocation id = ResourceLocation.tryParse(currentKey);
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) {
            return null;
        }
        return getOutput(new ItemStack(ForgeRegistries.ITEMS.getValue(id)));
    }

    public static String currentKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ItemStack translated = translateItem(stack);
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(translated.getItem());
        return id == null ? "" : id.toString();
    }

    public static String currentKey(Fluid fluid) {
        IngredientRef translated = translateFluid(fluid);
        if (translated != null) {
            ItemStack stack = translated.resultStack();
            if (!stack.isEmpty()) {
                return currentKey(stack);
            }
        }
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        return id == null ? "" : "fluid:" + id;
    }

    public static ItemStack pickOutput(SILEXRecipe recipe, int recipeIndex) {
        int total = recipe.totalWeight();
        int index = Math.floorMod(recipeIndex, total);
        int weight = 0;
        for (WeightedOut out : recipe.outputs()) {
            weight += out.weight();
            if (index < weight) {
                return out.item().resultStack();
            }
        }
        return ItemStack.EMPTY;
    }
}
