package com.hbm.inventory.recipes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code InventoryUtil.doesPlayerHaveAStacks} / {@code giveChanceStacksToPlayer}.
 */
public final class PlayerIngredientStore {
    private PlayerIngredientStore() {
    }

    public static boolean consume(Player player, List<IngredientRef> inputs, boolean remove) {
        List<ItemStack> original = player.getInventory().items;
        ItemStack[] copy = new ItemStack[original.size()];
        boolean[] modified = new boolean[original.size()];
        IngredientRef[] remaining = new IngredientRef[inputs.size()];
        int[] left = new int[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            remaining[i] = inputs.get(i);
            left[i] = inputs.get(i).count();
        }
        for (int i = 0; i < original.size(); i++) {
            copy[i] = original.get(i).copy();
        }
        for (int i = 0; i < remaining.length; i++) {
            IngredientRef need = remaining[i];
            for (int j = 0; j < copy.length && left[i] > 0; j++) {
                ItemStack inv = copy[j];
                if (inv.isEmpty() || !need.matches(inv, true)) {
                    continue;
                }
                int take = Math.min(left[i], inv.getCount());
                left[i] -= take;
                inv.shrink(take);
                modified[j] = true;
                if (inv.isEmpty()) {
                    copy[j] = ItemStack.EMPTY;
                }
            }
        }
        for (int amount : left) {
            if (amount > 0) {
                return false;
            }
        }
        if (remove) {
            for (int i = 0; i < original.size(); i++) {
                if (modified[i]) {
                    original.set(i, copy[i]);
                }
            }
            player.getInventory().setChanged();
        }
        return true;
    }

    public static void give(Player player, List<IngredientRef> outputs) {
        for (IngredientRef output : outputs) {
            if (output.chance() < 1.0F && player.getRandom().nextFloat() >= output.chance()) {
                continue;
            }
            ItemStack stack = output.resultStack();
            if (stack.isEmpty()) {
                continue;
            }
            if (!player.getInventory().add(stack.copy())) {
                player.drop(stack.copy(), false);
            }
        }
    }

    public static int countMatching(Player player, IngredientRef input) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (input.matches(stack, true)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static List<String> displayNames(IngredientRef input) {
        List<String> names = new ArrayList<>();
        ItemStack stack = input.resultStack();
        if (!stack.isEmpty()) {
            names.add(stack.getHoverName().getString());
        } else if (input.ore() != null) {
            names.add(input.ore());
        } else if (input.item() != null) {
            names.add(input.item());
        }
        return names;
    }
}
