package com.hbm.inventory.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.7.10 {@code ShredderRecipes}: explicit dump plus ore-dict style generation from registered items.
 */
public final class ShredderRecipes {
    public static final String RESOURCE = "/data/hbm/machine_recipes/shredder.json";
    public static final Path SOURCE_PATH = Path.of("src/main/resources/data/hbm/machine_recipes/shredder.json");

    public record ShredderRecipe(IngredientRef input, IngredientRef output) {
        public boolean existsInRegistry() {
            return input.existsInRegistry() && output.existsInRegistry();
        }
    }

    private static List<ShredderRecipe> recipes = List.of();
    private static Map<ResourceLocation, ItemStack> generated = Map.of();

    private ShredderRecipes() {
    }

    public static List<ShredderRecipe> recipes() {
        return recipes;
    }

    public static Map<ResourceLocation, ItemStack> generated() {
        return generated;
    }

    public static void loadFromSourceTree() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), false);
    }

    public static void loadAndFilterMissing() throws IOException {
        load(RecipeJson.readPreferSource(SOURCE_PATH, RESOURCE), true);
        generateFromRegistry();
    }

    public static void load(JsonObject root, boolean filterMissing) {
        List<ShredderRecipe> loaded = new ArrayList<>();
        for (JsonElement el : root.getAsJsonArray("recipes")) {
            JsonObject obj = el.getAsJsonObject();
            ShredderRecipe recipe = new ShredderRecipe(
                    IngredientRef.fromJson(obj.getAsJsonObject("input")),
                    IngredientRef.fromJson(obj.getAsJsonObject("output")));
            if (!filterMissing || recipe.existsInRegistry()) {
                loaded.add(recipe);
            }
        }
        recipes = Collections.unmodifiableList(loaded);
    }

    public static void generateFromRegistry() {
        Map<ResourceLocation, ItemStack> map = new HashMap<>();
        for (var entry : ForgeRegistries.ITEMS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (!"hbm".equals(id.getNamespace())) {
                continue;
            }
            String path = id.getPath();
            String material = null;
            int outCount = 1;
            if (path.startsWith("ingot_")) {
                material = path.substring("ingot_".length());
            } else if (path.startsWith("plate_")) {
                material = path.substring("plate_".length());
            } else if (path.startsWith("gem_")) {
                material = path.substring("gem_".length());
            } else if (path.startsWith("ore_")) {
                material = path.substring("ore_".length());
                outCount = 2;
            }
            if (material == null || material.isEmpty()) {
                continue;
            }
            ResourceLocation dustId = new ResourceLocation("hbm", "powder_" + material);
            if (!ForgeRegistries.ITEMS.containsKey(dustId)) {
                continue;
            }
            map.putIfAbsent(id, new ItemStack(ForgeRegistries.ITEMS.getValue(dustId), outCount));
        }
        putGenerated(map, "minecraft:iron_ingot", "hbm:powder_iron", 1);
        putGenerated(map, "minecraft:gold_ingot", "hbm:powder_gold", 1);
        putGenerated(map, "minecraft:copper_ingot", "hbm:powder_copper", 1);
        putGenerated(map, "minecraft:iron_ore", "hbm:powder_iron", 2);
        putGenerated(map, "minecraft:deepslate_iron_ore", "hbm:powder_iron", 2);
        putGenerated(map, "minecraft:gold_ore", "hbm:powder_gold", 2);
        putGenerated(map, "minecraft:deepslate_gold_ore", "hbm:powder_gold", 2);
        putGenerated(map, "minecraft:copper_ore", "hbm:powder_copper", 2);
        putGenerated(map, "minecraft:deepslate_copper_ore", "hbm:powder_copper", 2);
        generated = Collections.unmodifiableMap(map);
    }

    private static void putGenerated(Map<ResourceLocation, ItemStack> map, String in, String out, int count) {
        ResourceLocation inId = ResourceLocation.tryParse(in);
        ResourceLocation outId = ResourceLocation.tryParse(out);
        if (inId == null || outId == null) {
            return;
        }
        if (!ForgeRegistries.ITEMS.containsKey(inId) || !ForgeRegistries.ITEMS.containsKey(outId)) {
            return;
        }
        map.putIfAbsent(inId, new ItemStack(ForgeRegistries.ITEMS.getValue(outId), count));
    }

    public static ItemStack getShredderResult(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return scrap();
        }
        for (ShredderRecipe recipe : recipes) {
            if (recipe.input().matches(stack, true)) {
                ItemStack out = recipe.output().resultStack();
                if (!out.isEmpty()) {
                    return out;
                }
            }
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key != null) {
            ItemStack generatedOut = generated.get(key);
            if (generatedOut != null) {
                return generatedOut.copy();
            }
        }
        return scrap();
    }

    private static ItemStack scrap() {
        ResourceLocation id = new ResourceLocation("hbm", "scrap");
        if (ForgeRegistries.ITEMS.containsKey(id)) {
            return new ItemStack(ForgeRegistries.ITEMS.getValue(id));
        }
        return ItemStack.EMPTY;
    }
}
