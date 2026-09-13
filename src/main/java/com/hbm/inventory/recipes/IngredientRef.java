package com.hbm.inventory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * One 1.7.10 {@code AStack} row: item id, ore-dict name, or a DictFrame any-of list.
 */
public final class IngredientRef {
    private final String ore;
    private final String item;
    private final int count;
    private final float chance;
    private final List<IngredientRef> anyOf;

    public IngredientRef(String ore, String item, int count, float chance, List<IngredientRef> anyOf) {
        this.ore = ore;
        this.item = item;
        this.count = Math.max(1, count);
        this.chance = chance <= 0 ? 1.0F : chance;
        this.anyOf = anyOf == null ? List.of() : List.copyOf(anyOf);
    }

    public static IngredientRef fromJson(JsonObject obj) {
        List<IngredientRef> any = new ArrayList<>();
        if (obj.has("anyOf")) {
            for (JsonElement el : obj.getAsJsonArray("anyOf")) {
                any.add(fromJson(el.getAsJsonObject()));
            }
        }
        return new IngredientRef(
                obj.has("ore") && !obj.get("ore").isJsonNull() ? obj.get("ore").getAsString() : null,
                obj.has("item") && !obj.get("item").isJsonNull() ? obj.get("item").getAsString() : null,
                obj.has("count") && !obj.get("count").isJsonNull() ? obj.get("count").getAsInt() : 1,
                obj.has("chance") && !obj.get("chance").isJsonNull() ? obj.get("chance").getAsFloat() : 1.0F,
                any);
    }

    public static List<IngredientRef> list(JsonArray array) {
        List<IngredientRef> list = new ArrayList<>();
        if (array == null) {
            return list;
        }
        for (JsonElement el : array) {
            list.add(fromJson(el.getAsJsonObject()));
        }
        return list;
    }

    public String ore() {
        return ore;
    }

    public String item() {
        return item;
    }

    public int count() {
        return count;
    }

    public float chance() {
        return chance;
    }

    public List<IngredientRef> anyOf() {
        return anyOf;
    }

    public boolean matches(ItemStack stack, boolean ignoreCount) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!anyOf.isEmpty()) {
            for (IngredientRef option : anyOf) {
                if (option.matches(stack, true)) {
                    return ignoreCount || stack.getCount() >= count;
                }
            }
            return false;
        }
        if (!OreDictMatch.matches(stack, this)) {
            return false;
        }
        return ignoreCount || stack.getCount() >= count;
    }

    public boolean existsInRegistry() {
        if (!anyOf.isEmpty()) {
            for (IngredientRef option : anyOf) {
                if (option.existsInRegistry()) {
                    return true;
                }
            }
            return false;
        }
        return OreDictMatch.exists(this);
    }

    public ItemStack resultStack() {
        return OreDictMatch.toStack(this);
    }

    public String searchName() {
        if (item != null && !item.isEmpty()) {
            return item;
        }
        if (ore != null && !ore.isEmpty()) {
            return ore;
        }
        if (!anyOf.isEmpty()) {
            return anyOf.get(0).searchName();
        }
        return "";
    }
}
