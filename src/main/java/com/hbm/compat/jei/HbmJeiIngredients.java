package com.hbm.compat.jei;

import com.hbm.inventory.recipes.GenericMachineRecipe;
import com.hbm.inventory.recipes.GenericRecipeMatch;
import com.hbm.inventory.recipes.IngredientRef;
import com.hbm.inventory.recipes.OreDictMatch;
import com.hbm.inventory.recipes.StampType;
import com.hbm.items.machine.ItemStamp;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

final class HbmJeiIngredients {
    private HbmJeiIngredients() {
    }

    static List<ItemStack> stacks(IngredientRef ref) {
        if (ref == null) {
            return List.of();
        }
        return OreDictMatch.allStacks(ref);
    }

    static List<ItemStack> stacks(GenericMachineRecipe.ItemInput input) {
        if (input == null) {
            return List.of();
        }
        return stacks(GenericRecipeMatch.toRef(input));
    }

    static List<ItemStack> stamps(StampType type) {
        List<ItemStack> list = new ArrayList<>();
        if (type == null) {
            return list;
        }
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof ItemStamp stamp && stamp.getStampType() == type) {
                list.add(new ItemStack(item));
            }
        }
        return list;
    }

    static FluidStack fluid(String id, int amount) {
        var fluid = GenericRecipeMatch.fluid(id);
        if (fluid == null || amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    static void item(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, IngredientRef ref) {
        List<ItemStack> stacks = stacks(ref);
        if (stacks.isEmpty()) {
            return;
        }
        var slot = builder.addSlot(role, x, y).addItemStacks(stacks);
        if (ref != null && ref.chance() > 0.0F && ref.chance() < 1.0F) {
            int pct = Math.round(ref.chance() * 100.0F);
            slot.addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.hbm.chance", pct)));
        }
    }

    static void item(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y,
                     GenericMachineRecipe.ItemInput input) {
        if (input == null) {
            return;
        }
        item(builder, role, x, y, GenericRecipeMatch.toRef(input));
    }

    static void item(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        builder.addSlot(role, x, y).addItemStack(stack);
    }

    static void items(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return;
        }
        builder.addSlot(role, x, y).addItemStacks(stacks);
    }

    static void fluid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        builder.addSlot(role, x, y)
                .addIngredients(ForgeTypes.FLUID_STACK, List.of(stack))
                .setFluidRenderer(Math.max(stack.getAmount(), 1), false, 16, 16);
    }

    static void fluid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, String id, int amount) {
        fluid(builder, role, x, y, fluid(id, amount));
    }

    static List<Component> powerTime(int duration, long power) {
        List<Component> lines = new ArrayList<>();
        if (duration > 0) {
            lines.add(Component.translatable("gui.recipe.duration")
                    .append(": " + (duration / 20.0D) + "s"));
        }
        if (power > 0) {
            lines.add(Component.translatable("gui.recipe.consumption")
                    .append(": " + power + " FE/t"));
        }
        return lines;
    }
}
