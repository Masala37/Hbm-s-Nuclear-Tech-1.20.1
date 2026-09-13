package com.hbm.inventory.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.Locale;

/**
 * Vanilla 1.20.1 smelting JSON cannot set a result count. 1.7 crystal rows output 2–6 items.
 */
public final class StackedSmeltingSerializer implements RecipeSerializer<SmeltingRecipe> {
    @Override
    public SmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
        String group = GsonHelper.getAsString(json, "group", "");
        CookingBookCategory category = CookingBookCategory.MISC;
        if (json.has("category")) {
            try {
                category = CookingBookCategory.valueOf(
                        GsonHelper.getAsString(json, "category").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                category = CookingBookCategory.MISC;
            }
        }
        Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
        ItemStack result = readResult(json);
        float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
        int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);
        return new SmeltingRecipe(id, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public SmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        String group = buf.readUtf();
        CookingBookCategory category = buf.readEnum(CookingBookCategory.class);
        Ingredient ingredient = Ingredient.fromNetwork(buf);
        ItemStack result = buf.readItem();
        float experience = buf.readFloat();
        int cookingTime = buf.readVarInt();
        return new SmeltingRecipe(id, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, SmeltingRecipe recipe) {
        buf.writeUtf(recipe.getGroup());
        buf.writeEnum(recipe.category());
        recipe.getIngredients().get(0).toNetwork(buf);
        buf.writeItem(recipe.getResultItem(null));
        buf.writeFloat(recipe.getExperience());
        buf.writeVarInt(recipe.getCookingTime());
    }

    private static ItemStack readResult(JsonObject json) {
        if (GsonHelper.isStringValue(json, "result")) {
            ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(json, "result"));
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
            if (json.has("count")) {
                stack.setCount(GsonHelper.getAsInt(json, "count"));
            }
            return stack;
        }
        return ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
    }
}
